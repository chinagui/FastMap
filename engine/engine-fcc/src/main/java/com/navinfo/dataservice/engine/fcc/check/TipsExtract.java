package com.navinfo.dataservice.engine.fcc.check;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
import com.navinfo.dataservice.dao.fcc.TipsWorkStatus;
import com.navinfo.dataservice.dao.fcc.check.model.CheckTask;
import com.navinfo.dataservice.dao.fcc.check.operate.CheckTaskOperator;
import com.navinfo.dataservice.dao.fcc.check.selector.CheckPercentConfig;
import com.navinfo.dataservice.dao.fcc.tips.selector.HbaseTipsQuery;
//import com.navinfo.dataservice.engine.fcc.tips.TipsUtils;
import com.navinfo.dataservice.engine.fcc.tips.solrquery.TipsRequestParam;

/** 
 * @ClassName: TipsExtract.java
 * @author y
 * @date 2017-5-26 上午10:58:58
 * @Description: 质检-tips抽检
 *  
 */
public class TipsExtract {

	
	private SolrController solrConn = new SolrController();
	
	private static final Logger logger = Logger
			.getLogger(SolrController.class);
	
	
	/**
	 * @Description:tips抽取：抽取当前质检任务所对应的作业任务的，已作业完成、handler=作业员、作业子任务范围内（grid+task过滤）
	 * @param checkTaskId
	 * @param checkerId
	 * @param checkerName
	 * @param grids
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-5-26 上午11:02:48
	 */
	public JSONObject doExtract(int checkTaskId, int checkerId,
			String checkerName, JSONArray grids) throws Exception {
		
		JSONObject result=new JSONObject();
		
		int total=0;
		
		//1.先查询范围内的tips
		int workStatus=TipsWorkStatus.PREPARED_CHECKING; //待质检
		
		try {
			
			// 调用 manapi 获取 任务类型、及任务号
			ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
			
			JSONObject  jsonReq=new JSONObject(); //?????
			
			int subTaskId=jsonReq.getInt("subTaskId");//作业任务号
			
			int workerId=jsonReq.getInt("workerId");//作业员编号
			
			String workerName=jsonReq.getString("workerName");//作业员姓名
			
			String taskName=jsonReq.getString("taskName");//任务名称
			
			String subTaskName=jsonReq.getString("subTaskName");//子任务名称

			
			if (grids==null||grids.size()==0) {
                throw new IllegalArgumentException("参数错误:grids不能为空。");
            }
			
			Map<String,Integer> finishedMap=queryhasWorkTipsCount(grids, workStatus, checkTaskId, subTaskId, workerId, 0, null);
			
			//2.查询抽取配置表.计算出每类tips需要抽取的数量.并进行tips抽取
			Map<String,Integer> extactCountMap=new HashMap<String, Integer>();//每个tips抽取数量映射
			
			CheckPercentConfig configClass=new CheckPercentConfig();
			
			Map<String,Integer> percentConfig=configClass.getConfig();
			
			Set<String> allType=finishedMap.keySet();
			
			for (String type : allType) {
				
				int typeAllCount=finishedMap.get(type);
				
				int exPercent=percentConfig.get(type);
				
				Double exCout=Math.ceil(typeAllCount*exPercent/100);
				
				extactCountMap.put(type,exCout.intValue());
				
			}
			
			//3.进行tips抽取
			List<JSONObject> allExpTipsList=new ArrayList<JSONObject>();
					
			for (String type : allType) {
				
				int extactLimit=extactCountMap.get(type);
				
				TipsRequestParam param = new TipsRequestParam();
				
		        String solrQuery = param.getQueryFilterSqlForCheck(grids,workStatus,subTaskId,workerId,0,null);
		        
		        solrQuery=solrQuery+" and s_sourceType:"+ type; //指定类型
		        
				List<JSONObject> tips = solrConn.queryTips(solrQuery, null,extactLimit);
				
				allExpTipsList.addAll(tips);
			}
			//4.更新tips的状态，更新 stage=7,t_dEditStatus=0,t_dEditMeth=0
			
			updateTipsStatus2Check(allExpTipsList,checkerId);
			
			//4.保存tips抽取的tips结果
		/*	CheckResultOperator operate=new CheckResultOperator();
			
			operate.save(checkTaskId, total, allExpTipsList);*/
			
			CheckTask task=new CheckTask();
			
			task.setTaskId(checkTaskId);
			
			task.setTaskName(taskName);
			
			task.setSubTaskName(subTaskName);
			
			task.setWokerInfo(workerName+workerId);
			
			task.setCheckInfo(checkerName+checkerId);
			
			// 调用 manapi 获取 任务类型、及任务号
			
			String workGroup=null;//manApi.getGroupNameBySubtaskId(subTaskId);
			
			int workTotalCount=0;//manApi.getFinishedRoadNumBySubtaskId(subTaskId) ;
			
			task.setWorkGroup(workGroup);
			
			task.setWorkTotalCount(workTotalCount);
			
			task.setCheckTotalCount(total);
			
			task.setCheckStatus(0); //待质检
			
			CheckTaskOperator taskOperate=new CheckTaskOperator();
			
			taskOperate.save(task);
			
			result.put("total", total);
			
			result.put("typeCount", allType.size());
			
			return result;
			
		} catch (Exception e) {
			
			logger.error("tips抽取出错："+e.getMessage(),e);
			
			throw new Exception("tips抽取出错："+e.getMessage(),e);
		}
	}
	
	
	
	/**
	 * @Description:更新抽检后的tips的状态 ：更新 stage=7,t_dEditStatus=0,t_dEditMeth=0
	 * @param allExpTipsList
	 * @author: y
	 * @time:2017-5-27 下午6:02:23
	 */
	private void updateTipsStatus2Check(List<JSONObject> allExpTipsList,int checkerId)throws Exception {
		
		 Connection hbaseConn = null;
	        Table htab = null;
	        try {
	            hbaseConn = HBaseConnector.getInstance().getConnection();

	            htab = hbaseConn.getTable(TableName
	                    .valueOf(HBaseConstant.tipTab));
	            
	        	int t_dEditStatus=0;
	        	
	        	int t_dEditMeth=0;
	        	
	         	int stage=7;
	        	
	        	String date=StringUtils.getCurrentTime();
	        	
	        	for (JSONObject jsonObject : allExpTipsList) {
	        		
	    			String rowkey=jsonObject.getString("id");
	    			
	    			// 获取solr数据
		    		JSONObject solrIndex = solrConn.getById(rowkey);
		    		solrIndex.put("t_dEditStatus", t_dEditStatus);
		    		solrIndex.put("t_dEditMeth", t_dEditMeth);
		    		solrIndex.put("t_date", date);
		    		solrIndex.put("stage", stage);
		    		solrConn.addTips(solrIndex);
	
		            String[] queryColNames={"track"};
		            
		            JSONObject  oldTip=HbaseTipsQuery.getHbaseTipsByRowkey(htab, rowkey, queryColNames);
		            
		        	JSONObject track = oldTip.getJSONObject("track");
		        	
		        	JSONArray trackInfoArr=track.getJSONArray("t_trackInfo");
		        	
	        	
	        	//更新hbase
		        JSONObject newTrackInfo=null;//TipsUtils.newTrackInfo(stage, date, checkerId);
	        	trackInfoArr.add(newTrackInfo);
	        	
	        	track.put("t_dEditStatus", t_dEditStatus);
	        	track.put("t_dEditMeth", t_dEditMeth);
	        	track.put("t_trackInfo", trackInfoArr);
	        	
	        	Put put = new Put(rowkey.getBytes());
	        	
	        	put.addColumn("data".getBytes(), "track".getBytes(), track.toString()
						.getBytes());

	 		    htab.put(put);

	        }
	        }catch (Exception e) {
	        	
	        	logger.error("更细质检状态出错："+e.getMessage(), e);
	        	
	        	throw new Exception("更新质检状态出错："+e.getMessage(), e);
			}	
		
	}





/**
 * 
 * @Description：按照tips作业昨天查询tips
 * @param grids
 * @param workStatus
 * @param checkTaskId
 * @param subTaskId
 * @param workerId
 * @param checkerId
 * @param rowkeyList
 * @return
 * @throws Exception
 * @author: y
 * @time:2017-5-26 下午1:45:09
 */
	private List<JSONObject> queryTipsByWorkState(JSONArray grids,int workStatus,int checkTaskId, int subTaskId, int workerId,int checkerId,JSONArray rowkeyList
			)
			throws Exception {
		TipsRequestParam param = new TipsRequestParam();
		
        String solrQuery = param.getQueryFilterSqlForCheck(grids,workStatus,subTaskId,workerId,checkerId,rowkeyList);
        
        System.out.println(solrQuery);
        
		List<JSONObject> tips = solrConn.queryTips(solrQuery, null);

		return tips;
      
	}
	
	
	/**
	 * @Description:获取到每种tips的个数
	 * @param grids
	 * @param workStatus
	 * @param checkTaskId
	 * @param subTaskId
	 * @param workerId
	 * @param checkerId
	 * @param rowkeyList
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-5-26 下午1:50:35
	 */
	private Map<String,Integer> queryhasWorkTipsCount(JSONArray grids,int workStatus,int checkTaskId, int subTaskId, int workerId,int checkerId,JSONArray rowkeyList) throws Exception{
		
		List<JSONObject> tips = queryTipsByWorkState(grids, workStatus, checkTaskId, subTaskId, workerId, checkerId, rowkeyList);
		
		  Map<String, Integer> map = new HashMap<String, Integer>();
			for (JSONObject json : tips) {
				
				String type =json.getString("s_sourceType");
				
				if (map.containsKey(type)) {
					map.put(type, map.get(type) + 1);
				} else {
					map.put(type, 1);
				}
			}

			return map;

	}
	
	
	
	
	
	
	
	
	

}
