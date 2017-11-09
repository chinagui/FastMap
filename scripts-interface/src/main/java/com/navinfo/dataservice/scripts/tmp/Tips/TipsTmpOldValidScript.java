package com.navinfo.dataservice.scripts.tmp.Tips;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.TaskType;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOracleOperator;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangjunfang on 2017/10/31.
 */
public class TipsTmpOldValidScript {
    private static final Logger log = Logger.getLogger(TipsTmpOldValidScript.class);
    private String tableName = HBaseConstant.tipTab;

    public static void initContext(){
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[] { "dubbo-app-scripts.xml", "dubbo-scripts.xml" });
        context.start();
        new ApplicationContextUtil().setApplicationContext(context);
    }

    private List<String> queryCollectTaskTips(String[] collectTaskIds, int taskType)
            throws Exception {
        StringBuilder builder = new StringBuilder();
        String solrIndexFiled = null;
        if (taskType == TaskType.PROGRAM_TYPE_Q) {
            solrIndexFiled = "s_qTaskId";
        } else if (taskType == TaskType.PROGRAM_TYPE_M) {
            solrIndexFiled = "s_mTaskId";
        }
        if (collectTaskIds.length > 0) {
            builder.append(solrIndexFiled);
            builder.append(" in (");
            int index = 0;
            for (String collectTaskId : collectTaskIds) {
                if (index != 0)
                    builder.append(",");
                builder.append(collectTaskId);
                index++;
            }
            builder.append(")");
        }
        log.info("queryCollectTaskTips:" + builder.toString());
        Connection conn = null;
        try {
            conn = DBConnector.getInstance().getTipsIdxConnection();
            TipsIndexOracleOperator operator = new TipsIndexOracleOperator(conn);
            List<TipsDao> sdList = operator.query("select * from tips_index where "+builder);
            List<String> snapshots = new ArrayList<>();
            for (TipsDao tipsDao:sdList) {
                snapshots.add(tipsDao.getId());
            }
            return snapshots;
        }finally{
            DbUtils.closeQuietly(conn);
        }
    }

    private void processTipsOldByTask(String taskTypeStr, String taskIds) throws Exception{

        Table htab = null;
        String errorRowkey = "";
        try {
            int taskType = Integer.valueOf(taskTypeStr);
            String[] collectTaskIds = taskIds.split(",");
            List<String> rowkeyList = this.queryCollectTaskTips(collectTaskIds, taskType);

            htab = HBaseConnector.getInstance().getConnection()
                    .getTable(TableName.valueOf(tableName));

            for (String rowkey : rowkeyList) {
                errorRowkey = rowkey;
log.info("*********************process " + rowkey);
                Get get = new Get(rowkey.getBytes());
                //get.addColumn("data".getBytes(), "geometry".getBytes());
                get.addColumn("data".getBytes(), "old".getBytes());
                Result result = htab.get(get);
                if (!result.isEmpty()) {

//                    // 当前geometery
//                    JSONObject gLocation = JSONObject.fromObject(new String(result.getValue("data".getBytes(), "geometry".getBytes())));
//                    Geometry curGeo = GeoTranslator.geojson2Jts(gLocation.getJSONObject("g_location"));
//                    Set<Integer> curMeshSet = this.calculateGeometeryMesh(curGeo);
//                    if (curMeshSet != null && curMeshSet.size() > 0) {
//                        meshSet.addAll(curMeshSet);
//                    }

                    JSONObject oldTip = JSONObject
                            .fromObject(new String(result.getValue("data".getBytes(), "old".getBytes())));
                    JSONArray oldArray = oldTip.getJSONArray("old_array");
                    if(oldArray != null && oldArray.size() > 0) {
                        JSONObject lastOld = oldArray.getJSONObject(oldArray.size() - 1);
                        String oLocationStr = lastOld.getString("o_location");
                        log.info("@@@@@@@@@@@@@@@@@@@@@error " + oLocationStr);
                        if(StringUtils.isBlank(oLocationStr) || oLocationStr.equals("null")
                                || oLocationStr.equals("\"null\"")) {
                            log.info("@@@@@@@@@@@@@@@@@@@@@error " + rowkey);
                            continue;
                        }
                        JSONObject oldGeoJson = JSONObject.fromObject(oLocationStr);
                        Geometry oldGeo = GeoTranslator.geojson2Jts(oldGeoJson);
//                        Set<Integer> olcMeshSet = this.calculateGeometeryMesh(oldGeo);
//                        if (olcMeshSet != null && olcMeshSet.size() > 0) {
//                            meshSet.addAll(olcMeshSet);
//                        }
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw new Exception("error " + errorRowkey + ": " + e.getMessage());
        } finally {
            htab.close();
        }
    }


    public static void main(String[] args) {
        try {
            log.info("......................start TipsTmpOldValidScript......................");
            initContext();
            TipsTmpOldValidScript script = new TipsTmpOldValidScript();
            script.processTipsOldByTask(args[0], args[1]);
            log.info("......................all TipsTmpOldValidScript Over......................");
        } catch (Exception e) {
            e.printStackTrace();
            log.error(" excute  error "+e.getMessage(), e);
        } finally {
            log.info("......................all TipsTmpOldValidScript Over......................");
            System.exit(0);
        }

    }
}
