package com.navinfo.dataservice.engine.fcc.check;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.dataservice.dao.fcc.check.model.CheckWrong;
import com.navinfo.dataservice.dao.fcc.check.selector.CheckResultSelector;
import com.navinfo.dataservice.dao.fcc.check.selector.CheckWrongSelector;
import com.navinfo.dataservice.engine.fcc.tips.TipsSelector;
import com.navinfo.dataservice.engine.fcc.tips.solrquery.TipsRequestParam;

/** 
 * @ClassName: TipsCheckSelector.java
 * @author y
 * @date 2017-5-26 下午4:50:04
 * @Description: 质检-查询
 *  
 */
public class TipsCheckSelector {
	
	private SolrController solrConn = new SolrController();
	
	private static final Logger logger = Logger
			.getLogger(TipsCheckSelector.class);


	/**
	 * @Description:按状态，查询质检tips
	 * @param worker
	 * @param checker
	 * @param checkTaskId
	 * @param workStatus
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2017-5-26 下午5:29:53
	 */
	public List<JSONObject> queryAllHashExtractTipsByTask(int worker,int checker, int checkTaskId, int workStatus) throws Exception {
		
		return queryAllHashExtractTipsByTask(worker, checker, checkTaskId, workStatus,null);
		
	}
	
	
	/**
	 * @Description:按状态，查询质检tips（按类型）
	 * @param worker
	 * @param checker
	 * @param checkTaskId
	 * @param workStatus
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2017-5-26 下午5:29:53
	 */
	public List<JSONObject> queryAllHashExtractTipsByTask(int worker,int checker, int checkTaskId, int workStatus,String type) throws Exception {
		
		try{
			
			JSONArray checkRowkeyList=new CheckResultSelector().queryByTipsRowkey(checkTaskId);
			
			if(checkRowkeyList==null||checkRowkeyList.size()==0){
				
				throw new Exception("没有查询到当前任务的抽检记录："+checkTaskId+"请先进行抽检！");
			}
			
			String solrQuery=new TipsRequestParam ().assambleSqlForCheckQuery(worker, checker, workStatus, checkRowkeyList);
			
			if(StringUtils.isEmpty(type)){
				
				solrQuery=solrQuery+" and s_sourceType: "+type;
			}
			
			List<JSONObject> tips = solrConn.queryTips(solrQuery, null);
			
			return tips;
			
		}catch (Exception e) {
			
			throw e;
			
		}
		
	}



	/**
	 * @Description:质检统计
	 * @param checkerId
	 * @param workerId
	 * @param checkTaskId
	 * @param workStatus
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-5-26 下午5:33:20
	 */
	public JSONObject getStats(int checkerId, int workerId, int checkTaskId,
			int workStatus) throws Exception {
		
		 Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		 
		 JSONObject jsonData=new JSONObject();
		
		 List<JSONObject> tips=queryAllHashExtractTipsByTask(workerId, checkerId, checkTaskId, workStatus);
		 
			for (JSONObject json : tips) {
				int type = Integer.valueOf(json.getInt("s_sourceType"));

				if (map.containsKey(type)) {
					map.put(type, map.get(type) + 1);
				} else {
					map.put(type, 1);
				}
			}

			JSONArray data = new JSONArray();

			Set<Entry<Integer, Integer>> set = map.entrySet();

			int num = 0;

			Iterator<Entry<Integer, Integer>> it = set.iterator();

			while (it.hasNext()) {
				Entry<Integer, Integer> en = it.next();

				num += en.getValue();

				JSONObject jo = new JSONObject();

				jo.put(en.getKey(), en.getValue());

				data.add(jo);
			}

			jsonData.put("total", num);

			jsonData.put("rows", data);

			return jsonData;
	}


	/**
	 * @Description:质检获取列表
	 * @param checkerId
	 * @param workerId
	 * @param checkTaskId
	 * @param workStatus
	 * @param dbId
	 * @param type
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-5-26 下午6:07:46
	 */
	public JSONArray getSnapot(int checkerId, int workerId, int checkTaskId,
			int workStatus, int dbId, String type) throws Exception {
		
		JSONArray snapotArr=new JSONArray();
		
		try{
		
			List<JSONObject> tips=queryAllHashExtractTipsByTask(workerId, checkerId, checkTaskId, workStatus,type);
			
			TipsSelector sel=new TipsSelector();
			
			snapotArr=sel.convert2Snapshot(tips, dbId, Integer.valueOf(type));
			
		}catch (Exception e) {
			
			logger.error("质检查询tips列表出错："+e.getMessage(), e);
			
			throw new Exception("质检查询tips列表出错："+e.getMessage(), e);
			
		}
		 
		return snapotArr;
	}


	/**
	 * @Description:根据tips的rowkey查询质检问题记录 
	 * @param checkTaskId
	 * @param rowkey
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-5-26 下午6:44:39
	 */
	public JSONObject queryWrongByRowkey(int checkTaskId, String rowkey) throws Exception {

		try{
			
			CheckWrongSelector wrongSelector=new CheckWrongSelector();
			
			CheckWrong wrong=wrongSelector.queryByTipsRowkey(checkTaskId, rowkey);
			
			return JSONObject.fromObject(wrong);
			
		}catch (Exception e) {
			
			logger.error("加载质检问题记录出错,rowkey:"+rowkey+"出错原因："+e.getMessage(),e);
			
			throw new Exception("加载质检问题记录出错,rowkey:"+rowkey+"出错原因："+e.getMessage(),e);
			
		}
	}
	
	

}
