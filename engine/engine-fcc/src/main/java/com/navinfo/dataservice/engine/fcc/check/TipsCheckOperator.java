package com.navinfo.dataservice.engine.fcc.check;



import java.util.Map;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOracleOperator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.dataservice.dao.fcc.check.model.CheckWrong;
import com.navinfo.dataservice.dao.fcc.check.operate.CheckTaskOperator;
import com.navinfo.dataservice.dao.fcc.check.operate.CheckWrongOperator;
import com.navinfo.dataservice.dao.fcc.check.selector.CheckWrongSelector;
import com.navinfo.dataservice.dao.fcc.tips.selector.HbaseTipsQuery;

/** 
 * @ClassName: TipsCheckOperator.java
 * @author y
 * @date 2017-5-26 下午8:04:47
 * @Description: TODO
 *  
 */
public class TipsCheckOperator {
	
	
	private SolrController solrConn = new SolrController();
	
	private static final Logger logger = Logger
			.getLogger(TipsCheckOperator.class);
	
	/**
	 * @Description:新增质检问题记录（一个tips只能增加一条）
	 * @param jsonWrong
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2017-5-26 下午8:32:54
	 */
	public CheckWrong saveCheckWrong(JSONObject jsonWrong) throws Exception{
		
		CheckWrong result=null;
		
		try{
			CheckWrong wrong=(CheckWrong) JSONObject.toBean(jsonWrong,CheckWrong.class);
			
			wrong.setWorkTime(getTipsWorkTime(wrong.getTipsRowkey()));
			
			
			// 调用 manapi 获取 任务类型、及任务号
			ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
			
			Map<String,String> taskInfoMap=manApi.getCommonSubtaskByQualitySubtask(wrong.getCheckTaskId());
			Integer workerId=Integer.valueOf(taskInfoMap.get("exeUserId"));//作业员编号
			String workerName=taskInfoMap.get("exeUserName");//作业员姓名
			wrong.setWorker(workerName+workerId);
			
			//1.先判断，该rowkey是否已经存在质检问题记录
			CheckWrongSelector se=new CheckWrongSelector();
			if(se.rowkeyHasExtract(wrong.getCheckTaskId(), wrong.getTipsRowkey())){
				
				throw new Exception("该tips已存在问题记录，不能重复增加");
			}
			//2.保存质检问题记录
			CheckWrongOperator op=new CheckWrongOperator();
			result=op.save(wrong);
			
		}catch (Exception e) {
			
			logger.error("新增质检问题记录出错："+e.getMessage(), e);
			
			throw new Exception("新增质检问题记录出错："+e.getMessage(), e);
		}
		
		return result;
		
	}
	
	


	/**
	 * @Description:查询tips的作业时间
	 * @param rowkey
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-5-31 下午8:31:05
	 */
	private String getTipsWorkTime(String rowkey) throws Exception {
			String workDate="";
			Connection hbaseConn = null;
	        Table htab = null;
	        try {
	    		
	            hbaseConn = HBaseConnector.getInstance().getConnection();

	            htab = hbaseConn.getTable(TableName
	                    .valueOf(HBaseConstant.tipTab));
	            String[] queryColNames={"track"};
	            
	            JSONObject  oldTip=HbaseTipsQuery.getHbaseTipsByRowkey(htab, rowkey, queryColNames);
	            
	        	JSONObject track = oldTip.getJSONObject("track");
	        	
	        	JSONArray trackInfoArr=track.getJSONArray("t_trackInfo");
	        	
	        	for (int i = trackInfoArr.size()-1; i >-1; i--) {
					
	        		JSONObject trackInfo=trackInfoArr.getJSONObject(i);
	        		
	        		int stage=trackInfo.getInt("stage");
	        		
	        		if(stage==2){
	        			
	        			workDate=trackInfo.getString("date");
	        			
	        			break;
	        		}
				}
	        	
	        	return workDate;
	        }catch (Exception e) {
	        	logger.error("查询tips出错,rowkey:"+rowkey+e.getMessage(), e);
				
				throw new Exception("查询tips出错,rowkey:"+rowkey+e.getMessage(), e);
			}
	        
	}




	/**
	 * @Description:质检问题记录-修改
	 * @param jsonWrong
	 * @return
	 * @author: y
	 * @param logId 
	 * @throws Exception 
	 * @time:2017-5-27 上午9:24:23
	 */
	public void updateCheckWrong(String logId, JSONObject jsonWrong) throws Exception {
		
		try{
			CheckWrong wrong=(CheckWrong) JSONObject.toBean(jsonWrong,CheckWrong.class);
			
			//1.先判断，该rowkey是否已经存在质检问题记录
			CheckWrongSelector se=new CheckWrongSelector();
			if(!se.isExists(logId)){
				throw new Exception("该记录已不存在");
			}
			//2.保存质检问题记录
			CheckWrongOperator op=new CheckWrongOperator();
			
			op.update(logId,wrong);
			
		}catch (Exception e) {
			
			logger.error("更新质检问题记录出错："+e.getMessage(), e);
			
			throw new Exception("更新质检问题记录出错："+e.getMessage(), e);
		}
		
	}



	/**
	 * @Description:删除质检问题记录
	 * @param logId
	 * @author: y
	 * @throws Exception 
	 * @time:2017-5-27 上午10:18:46
	 */
	public void deleteWrong(String logId) throws Exception {
		
		CheckWrongOperator op=new CheckWrongOperator();
		try {
			//1.先判断，该rowkey是否已经存在质检问题记录
			CheckWrongSelector se=new CheckWrongSelector();
			if(!se.isExists(logId)){
				throw new Exception("该记录已不存在");
			}
			op.deleteByLogId(logId);
		}catch (Exception e) {
			
			logger.error("删除质检问题记录出错："+e.getMessage(), e);
			
			throw new Exception("删除质检问题记录出错："+e.getMessage(), e);
		}
		
	}




	/**
	 * @Description:修改tips质检状态
	 * @param rowkey
	 * @param workStatus
	 * @author: y
	 * @param rowkey 
	 * @throws Exception 
	 * @time:2017-5-27 上午10:57:37
	 */
	public void updateTipsCheckStatus(String rowkey, int workStatus) throws Exception {
		
		
	    Connection hbaseConn = null;
	    java.sql.Connection conn = null;
        Table htab = null;
        try {
    		// 获取solr数据
			conn = DBConnector.getInstance().getTipsIdxConnection();
			TipsIndexOracleOperator operator = new TipsIndexOracleOperator(conn);
    		TipsDao tipsDao = operator.getById(rowkey);

    		if(tipsDao==null){
    			throw new Exception("数据不存在");
			}

            hbaseConn = HBaseConnector.getInstance().getConnection();

            htab = hbaseConn.getTable(TableName
                    .valueOf(HBaseConstant.tipTab));
            String[] queryColNames={"track"};
            
            JSONObject  oldTip=HbaseTipsQuery.getHbaseTipsByRowkey(htab, rowkey, queryColNames);
            
        	JSONObject track = oldTip.getJSONObject("track");
        	
        	JSONArray trackInfoArr=track.getJSONArray("t_trackInfo");
        	
        	JSONObject lastTrack=trackInfoArr.getJSONObject(trackInfoArr.size()-1);//删除后的最后一条 一定是stage=7的
        	
        	int t_dEditStatus=0;
        	
        	String date=StringUtils.getCurrentTime();
        	
        	int t_dEditMeth=0;
        	
        	//日编待质检tips：
        	
        	if(workStatus==0){
        		
        		t_dEditStatus=0;//0 未开始；1 问题待确认；2 已完成；
        		
        		t_dEditMeth=0;//0 不应用；1 人工作业；2 交互式作业；3 自动化；
        		
        	}
        	//日编有问题待确认
        	else if(workStatus==1){
        		
        		t_dEditStatus=1; //0 未开始；1 问题待确认；2 已完成；
        		
        		t_dEditMeth=1;//0 不应用；1 人工作业；2 交互式作业；3 自动化；
        		
			}
			//日编质检完成
        	else if(workStatus==2){
				
        		t_dEditStatus=2; //0 未开始；1 问题待确认；2 已完成；
        		
        		t_dEditMeth=1;//0 不应用；1 人工作业；2 交互式作业；3 自动化；
			}

			tipsDao.setT_dEditStatus(t_dEditStatus);
        	
        	tipsDao.setT_dEditMeth(t_dEditMeth);
        	
        	tipsDao.setT_date(date);
        	
        	operator.updateOne(tipsDao);
        	
        	//更新hbase
        	lastTrack.put("date", date);
            trackInfoArr.remove(trackInfoArr.size()-1);//先删除最后一条stage是7
        	trackInfoArr.add(lastTrack);//增加修改后的
        	
        	track.put("t_dEditStatus", t_dEditStatus);
        	track.put("t_dEditMeth", t_dEditMeth);
        	track.put("t_trackInfo", trackInfoArr);
        	
        	Put put = new Put(rowkey.getBytes());
        	
        	put.addColumn("data".getBytes(), "track".getBytes(), track.toString()
					.getBytes());

 		    htab.put(put);

        }catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
        	logger.error("修改质检状态出错：rowkey:"+rowkey+e.getMessage(), e);
        	throw new Exception("修改质检状态出错：rowkey:"+rowkey+e.getMessage(), e);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}




	/**
	 * @Description:关闭质检任务
	 * @param checkTaskId
	 * @author: y
	 * @throws Exception 
	 * @time:2017-5-31 下午3:58:25
	 */
	public void closeTask(int checkTaskId) throws Exception {
		CheckTaskOperator operator=new CheckTaskOperator();
		try {
			operator.closeTask(checkTaskId);
		} catch (Exception e) {

        	logger.error("修改任务质检状态出错：rowkey:"+e.getMessage(), e);
        	
        	throw new Exception("修改任务质检状态出错："+e.getMessage(), e);
		}
		
	}
        
        
     
}
        	

