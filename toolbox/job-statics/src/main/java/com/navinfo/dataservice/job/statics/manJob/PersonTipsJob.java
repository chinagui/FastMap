package com.navinfo.dataservice.job.statics.manJob;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.tips.selector.HbaseTipsQuery;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 人天任务-道路-新增里程- tips：统计当天该作业员，该任务的新增测线tips的里程
 *人天任务-道路- Tips量-day/tips：统计当天该作业员，该任务的所有测线tips的里程
 *只统计中线子任务，一个子任务对应一个作业员，按照子任务统计即可
 * 有测线的子任务才进行统计
 * Created by zhangjunfang on 2017/8/9.
 */
public class PersonTipsJob extends AbstractStatJob {

    public PersonTipsJob(JobInfo jobInfo) {
        super(jobInfo);
    }

    @Override
    public String stat() throws JobException {
    	PersonTipsJobRequest statReq = (PersonTipsJobRequest)request;
        try {
        	String workDay = statReq.getWorkDay();
        	log.info("start stat PersonTipsJob");
            //1.获取需要统计的子任务号
            Map<Integer, JSONObject> resultMap = getSubTaskLineStat(workDay);

            Map<String,List<Map<String,Object>>> result = new HashMap<String,List<Map<String,Object>>>();
            List<Map<String,Object>> resultMapList = new ArrayList<>();
            for(Integer mSubTaskId: resultMap.keySet()) {
                JSONObject statObj = resultMap.get(mSubTaskId);
                Map<String, Object> subTaskMap = new HashMap<>();
                subTaskMap.put("subtaskId", mSubTaskId);
                subTaskMap.put("tipsAddLen", statObj.getDouble("tipsAddLen"));
                subTaskMap.put("tipsAllNum", statObj.getDouble("tipsAllNum"));
                subTaskMap.put("workDay", workDay);
                resultMapList.add(subTaskMap);
            }
            result.put("person_tips", resultMapList);
            JSONObject identifyJson=new JSONObject();
			identifyJson.put("timestamp", statReq.getTimestamp());
			identifyJson.put("workDay", statReq.getWorkDay());
			statReq.setIdentify(identifyJson.toString());
            log.info("end stat PersonTipsJob");
            return JSONObject.fromObject(result).toString();
        }catch (Exception e) {
            throw new JobException("PersonTipsJob执行报错", e);
        }
    }

    /**
     * 统计有测线Tips的子任务
     * @return
     * @throws Exception
     */
    private Map<Integer, JSONObject> getSubTaskLineStat(final String timestamp) throws Exception {
        java.sql.Connection orclConn = null;
        try {
            String sqlLineQuery = "SELECT T.S_MSUBTASKID, T.ID, T.WKTLOCATION, T.T_LIFECYCLE\n" +
                    "  FROM TIPS_INDEX T\n" +
                    " WHERE T.S_MSUBTASKID <> 0\n" +
                    "   AND T.S_SOURCETYPE = '2001'\n" +
                    " ORDER BY T.S_MSUBTASKID";
            orclConn = DBConnector.getInstance().getTipsIdxConnection();

            QueryRunner run = new QueryRunner();
            return run.query(orclConn, sqlLineQuery, new ResultSetHandler<Map<Integer, JSONObject>>() {

                @Override
                public Map<Integer, JSONObject> handle(ResultSet rs)
                        throws SQLException {
                    Map<Integer, JSONObject> subtaskTipsMap = new HashMap<>();
                    Connection hbaseConn = null;
                    Table htab = null;
                    try{
                        hbaseConn = HBaseConnector.getInstance().getConnection();
                        htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));
                        while(rs.next()){
                            //中线任务号
                            int s_mSubTaskId = rs.getInt("S_MSUBTASKID");
                            String rowkey = rs.getString("ID");
                            long tipsAllNum = 0;
                            double newLength = 0;
                            JSONObject statObj = null;
                            if(subtaskTipsMap.containsKey(s_mSubTaskId)) {//已有
                                statObj = subtaskTipsMap.get(s_mSubTaskId);
                                tipsAllNum = statObj.getLong("tipsAllNum");
                                newLength = statObj.getDouble("tipsAddLen");
                            }else {
                                statObj = new JSONObject();
                                subtaskTipsMap.put(s_mSubTaskId, statObj);
                            }
                            //判断是否当天的tips
                            JSONObject hbaseTip = HbaseTipsQuery.getHbaseTipsByRowkey(htab, rowkey, new String[]{"track"});
                            if(hbaseTip.containsKey("track")) {
                                JSONObject track = hbaseTip.getJSONObject("track");
                                if(track.containsKey("t_trackInfo")) {
                                    JSONArray trackInfoArr = track.getJSONArray("t_trackInfo");
                                    if(trackInfoArr != null && trackInfoArr.size() > 0) {//查看track履历，外业当天是否提交
                                        for (int i = trackInfoArr.size() - 1; i > -1; i--) {
                                            JSONObject trackInfo = trackInfoArr.getJSONObject(i);
                                            int stage = trackInfo.getInt("stage");
                                            String date = trackInfo.getString("date");
                                            if(stage == 1 && date.startsWith(timestamp)) {//当天外业新增
                                            	tipsAllNum += 1;
                                                statObj.put("tipsAllNum", tipsAllNum);
                                              //是否当天新增
                                                int lifecycle = rs.getInt("T_LIFECYCLE");
                                                if(lifecycle == 3) {//Tips状态是新增
                                                	//测线显示坐标
                                                    STRUCT wktLocation = (STRUCT) rs.getObject("WKTLOCATION");
                                                    //测线里程计算
                                                    double lineLength = GeometryUtils.getLinkLength(GeoTranslator.struct2Jts(wktLocation));
                                                    newLength += lineLength;
                                                    statObj.put("tipsAddLen", newLength);
                                                } 
                                                break;
                                            }
                                        }
                                    }
                                }
                            }                            
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new SQLException("PersonTipsJob报错: ", e);
                    }finally {
                        if(rs != null) {
                            rs.close();
                        }
                        if(htab != null) {
                            try {
                                htab.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    return subtaskTipsMap;
                }
            });
        }catch (Exception e){
            DbUtils.rollbackAndCloseQuietly(orclConn);
            throw  e;
        } finally {
            DbUtils.commitAndCloseQuietly(orclConn);
        }
    }
}
