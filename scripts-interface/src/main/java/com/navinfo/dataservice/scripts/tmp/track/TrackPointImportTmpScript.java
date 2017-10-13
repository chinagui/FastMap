package com.navinfo.dataservice.scripts.tmp.track;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.dataservice.api.es.iface.EsApi;
import com.navinfo.dataservice.api.es.model.TrackPoint;
import com.navinfo.dataservice.commons.constant.EsConstant;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * 普通轨迹重新上传入库（因周秒重复导致入库失败引起）
 * 将2017.9.18日以前处理过的轨迹文件重新入库
 * 即取该条记录的GPS时间（recordTime），
 * 根据此时间重新计算周秒——时间转换周秒的方法请王磊补充
 * Created by zhangjunfang on 2017/9/27.
 * 临时脚本，执行一次
 */
public class TrackPointImportTmpScript {
    private static final Logger log = Logger.getLogger(TrackPointImportTmpScript.class);

    public static int total = 0;
    private static int failed = 0;

    public static void initContext(){
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[] { "dubbo-app-scripts.xml","dubbo-scripts.xml" });
        context.start();
        new ApplicationContextUtil().setApplicationContext(context);
    }

    /**
     * 轨迹重新入库
     * @param startDate 格式yyyyMMdd
     * @param endDate 格式yyyyMMdd
     */
    public static void reImport(String startDate, String endDate) {
        //1.根据起止时间 sys库查询track上传记录
        Connection sysConn = null;
        PreparedStatement prepSysStmt = null;
        ResultSet sysRs = null;

        org.apache.hadoop.hbase.client.Connection hbaseConn = null;
        Table htab = null;
        try{

            hbaseConn = HBaseConnector.getInstance().getConnection();
            htab = hbaseConn.getTable(TableName
                    .valueOf(HBaseConstant.trackLineTab));

            sysConn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
            String sql = "SELECT UPLOAD_ID, FILE_PATH\n" +
                    "  FROM DROPBOX_UPLOAD T\n" +
                    " WHERE T.END_DATE IS NOT NULL\n" +
                    "   AND T.END_DATE >= TO_DATE(?, 'yyyyMMdd')\n" +
                    "   AND T.END_DATE < TO_DATE(?, 'yyyyMMdd')\n" +
                    "   AND T.FILE_NAME LIKE 'track%'";
            prepSysStmt = sysConn.prepareStatement(sql);
            prepSysStmt.setString(1, startDate);
            prepSysStmt.setString(2, endDate);
            sysRs = prepSysStmt.executeQuery();
            while(sysRs.next()) {
                int uploadId = sysRs.getInt("UPLOAD_ID");
                String filePath = sysRs.getString("FILE_PATH");
                filePath += "/" + uploadId;
                log.info("***********************start import filepath " + filePath);
                total = 0;
                failed = 0;
                doImportPerFile(filePath, uploadId, htab);
                log.info("***********************end import filepath " + filePath);
            }
        }catch (Exception e){
            log.error("reImport error", e);
        }finally {
            DbUtils.closeQuietly(sysRs);
            DbUtils.closeQuietly(prepSysStmt);
            DbUtils.closeQuietly(sysConn);
            if(htab != null) {
                try {
                    htab.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void doImportPerFile(String filePath, int uploadId, Table htab) {
        filePath = filePath + "/"+ "track_collection.json";
        File fileLine = new File(filePath);
        //filePath = "F:\\FCC\\trackpoint\\780" + "\\"+ "track_collection.json";
        //File fileLine = new File(filePath);

        if(fileLine.exists()) {
            try {
                runImportTrackTab(htab, filePath);
                log.info("********doImportPerFile success :" + uploadId + ",total: " + total + ",faild: " + failed);
            }catch (Exception e) {
                log.error("********doImportPerFile error :" + uploadId, e);
            }
        }else{
            log.error("********file not found error :" + uploadId);
        }
    }

    public static void runImportTrackTab(Table htab, String filePath) throws Exception {
        try{
            loadFileContent(filePath, htab);
        }catch (Exception e) {
            log.error("**************** runImportTrackTab ", e);
        }
    }

    public static void loadFileContent(String filePath, Table htab) throws Exception {
        FileInputStream fis = null;
        Map<String, List<String>> newRowkeyMap = null;
        Set<String> realImpSet = new HashSet<>();
        try{
            fis = new FileInputStream(filePath);
            newRowkeyMap = new HashMap<>();
            Scanner scanner = new Scanner(fis);
            List<Put> puts = new ArrayList<Put>();
            List trackIdxList = new ArrayList();
            EsApi apiService = (EsApi) ApplicationContextUtil.getBean("esApi");
            while (scanner.hasNextLine()) {
                total++;
                String rowkey = null;
                try{
                    String line = scanner.nextLine();
                    JSONObject json = JSONObject.parseObject(line);
                    //获取文件rowkey
                    rowkey = getSourceRowkey(json);
                    if(newRowkeyMap.containsKey(rowkey)) {//rowkey重复，需要重新赋值周秒，重新获取rowkey
                        String recordTime = json.getString("recordTime").substring(0,14);
                        double newWS = TrackUtils.zoneUTCTime2GPSTime(recordTime);
                        json.put("weekSeconds", newWS);
                        rowkey = getSourceRowkey(json);
                        if(newRowkeyMap.containsKey(rowkey)) {//重新赋值的rowkey也冲突了。。。。悲剧啊
                            List<String> idList = newRowkeyMap.get(rowkey);
                            idList.add(json.getString("id"));
                            newWS = TrackUtils.zoneUTCTime2GPSTime(recordTime) + 0.1 * (idList.size() - 1);
                            json.put("weekSeconds", newWS);
                            rowkey = getSourceRowkey(json);
                            //throw new Exception("********new rowkey repeat, this upload stop :" + rowkey + "id: " + json.getString("id"));
                        }else{
                            List<String> idList = new ArrayList<>();
                            idList.add(json.getString("id"));
                            newRowkeyMap.put(rowkey, idList);
                        }
                    }else{
                        List<String> idList = new ArrayList<>();
                        idList.add(json.getString("id"));
                        newRowkeyMap.put(rowkey, idList);
                    }

                    //通过id判断数据在hbase库中是否已经存在，存在则使用库中的rowkey
                    Put put = generatePut(json, rowkey, trackIdxList);
                    puts.add(put);
                    realImpSet.add(rowkey);
                    if (puts.size() % 5000 == 0) {
                        htab.put(puts);
                        puts.clear();

                        if(trackIdxList != null && trackIdxList.size() > 0) {
                            apiService.insert(trackIdxList);
                            trackIdxList.clear();
                        }
                    }
                }catch (Exception e) {
                    failed ++;
                    throw new Exception(rowkey + " error", e);
                }
            }
            if(puts.size() > 0) {
                htab.put(puts);
            }
            if(trackIdxList.size() > 0) {
                apiService.insert(trackIdxList);
                trackIdxList.clear();
            }
            log.info("###############newrowkey" +newRowkeyMap.size() + " total: " + total + " realImpSet " + realImpSet.size());
            for(String rowkey : newRowkeyMap.keySet()) {
                List<String> idList =  newRowkeyMap.get(rowkey);
                if(idList.size() > 1) {
                    log.info("###############rowkey" + rowkey + " zjf " +idList.size());
                }

            }
        }catch (Exception e) {
            log.error("error ！", e);
        }finally{
            if(fis!=null)fis.close();
        }

    }

    public static String getSourceRowkey(JSONObject json) {
        String a_prjName=json.getString("prjName");
        String a_weekSeconds=json.getString("weekSeconds");
        return a_prjName + a_weekSeconds;
    }

    public static Put generatePut(JSONObject json, String rowkey, List trackIdxList) throws Exception {
        Put put = getPut(json, rowkey);
        TrackPoint point = getTrackIdx(json, rowkey);
        trackIdxList.add(point);
        return put;
    }

    public static Put getPut(JSONObject json, String rowkey){
        Put put = new Put(rowkey.getBytes());
        put.addColumn("attribute".getBytes(), "a_id".getBytes(),
                json.getString("id").getBytes());
        put.addColumn("attribute".getBytes(), "a_weekSeconds".getBytes(),
                Bytes.toBytes(json.getDouble("weekSeconds")));
        put.addColumn("attribute".getBytes(), "a_direction".getBytes(),
                Bytes.toBytes(json.getDouble("direction")));
        put.addColumn("attribute".getBytes(), "a_speed".getBytes(),
                Bytes.toBytes(json.getDouble("speed")));
        put.addColumn("attribute".getBytes(), "a_recordTime".getBytes(),
                json.getString("recordTime").getBytes());
        put.addColumn("attribute".getBytes(), "a_user".getBytes(),
                Bytes.toBytes(json.getInteger("userId")));
        put.addColumn("attribute".getBytes(), "a_deviceNum".getBytes(),
                json.getString("deviceNum").getBytes());
        put.addColumn("attribute".getBytes(), "a_hdop".getBytes(),
                Bytes.toBytes(json.getDouble("hdop")));
        put.addColumn("attribute".getBytes(), "a_height".getBytes(),
                Bytes.toBytes(json.getDouble("altitude")));
        put.addColumn("attribute".getBytes(), "a_posType".getBytes(),
                Bytes.toBytes(json.getInteger("posType")));
        put.addColumn("attribute".getBytes(), "a_satNum".getBytes(),
                Bytes.toBytes(json.getInteger("satNum")));
        put.addColumn("attribute".getBytes(), "a_mediaFlag".getBytes(),
                Bytes.toBytes(json.getInteger("mediaFlag")));
        put.addColumn("attribute".getBytes(), "a_linkId".getBytes(),
                Bytes.toBytes(json.getInteger("linkId")));
        put.addColumn("attribute".getBytes(), "a_prjName".getBytes(),
                json.getString("prjName").getBytes());
        put.addColumn("attribute".getBytes(), "a_geometry".getBytes(),
                json.getString("geometry").getBytes());
        return put;
    }

    public static TrackPoint getTrackIdx(JSONObject json, String rowkey) throws Exception {
        TrackPoint point = new TrackPoint();
        point.setId(rowkey);
        String wkt = json.getString("geometry");
        point.setA_geometry(GeoTranslator.jts2Geojson(GeoTranslator.wkt2Geometry(wkt)));
        point.setA_linkId(json.getInteger("linkId"));
        point.setA_user(json.getInteger("userId"));
        point.setA_recordTime(json.getString("recordTime").substring(0,14));
        return point;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {

            if(args.length != 2) {
                log.error("please enter the start date and end date");
                System.exit(0);
            }
            String startDateString = args[0];
            String endDateString = args[1];
            Date endMax = DateUtils.stringToDate("20170919","yyyyMMdd");
            Date startDate = DateUtils.stringToDate(startDateString, "yyyyMMdd");
            Date endDate = DateUtils.stringToDate(endDateString, "yyyyMMdd");
            if(startDate.getTime() >= endDate.getTime()) {
                log.error("start date must < end date");
            }
            if(endDate.getTime() > endMax.getTime()) {
                log.error("endDate date must < 20170920");
            }

            initContext();
            reImport(startDateString, endDateString);
            log.info("......................Over......................");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }finally {
            System.exit(0);
        }

    }

}
