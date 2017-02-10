package com.navinfo.dataservice.engine.fcc.tips;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;

/** 
 * @ClassName: BaseTipsOperate.java
 * @author y
 * @date 2016-12-27 下午8:00:31
 * @Description: tips操作基础类（放一些公共方法）
 *  
 */
public class BaseTipsOperate {
	
	protected SolrController solr = new SolrController();
	
	private static final Logger logger = Logger
			.getLogger(BaseTipsOperate.class);
	
	
	/**
	 * @Description:更新备注信息（feebacks type=3(文字)）
	 * @param rowkey
	 * @param user
	 * @param memo
	 * @author: y
	 * @throws Exception
	 * @time:2016-11-16 上午11:29:03
	 */
	public void updateFeedbackMemo(String rowkey, int user, String memo) throws Exception {

		try {

			Connection hbaseConn = HBaseConnector.getInstance().getConnection();

			Table htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));
			//获取solr数据
			JSONObject solrIndex = solr.getById(rowkey);
			
			int stage=2;
			//如果是预处理的tips则stage=5
			
			if(solrIndex.getString("s_sourceType").equals(PretreatmentTipsOperator.FC_SOURCE_TYPE)){
				stage=5; 
			}
			

			// 获取到改钱的 feddback和track
			JSONObject oldTip = getOldTips(rowkey, htab);

			// 1.更新feddback和track
			JSONObject track = oldTip.getJSONObject("track");

			JSONArray trackInfoArr = track.getJSONArray("t_trackInfo");

			String date = DateUtils.dateToString(new Date(), "yyyyMMddHHmmss");

			// 新增一个trackInfo
			JSONObject jsonTrackInfo = new JSONObject();

			jsonTrackInfo.put("stage", stage);

			jsonTrackInfo.put("date", date);

			jsonTrackInfo.put("handler", user);

			trackInfoArr.add(jsonTrackInfo);

			track.put("t_trackInfo", trackInfoArr);
			
			track.put("t_lifecycle", 2);

			// 2.更新feedback

			// 新增一个f_array type=3的是文字
			JSONObject feedBack = oldTip.getJSONObject("feedback");

			JSONArray f_array = feedBack.getJSONArray("f_array");


			for (Object object : f_array) {

				JSONObject obj = JSONObject.fromObject(object);
				
				//先删掉

				if (obj.getInt("type") == 3) {
					
					f_array.remove(obj);

					break;
				}
			}
			// 如果count=0,则说明原来没有备注，则，增加一条

			int type = 3; // 文字

			JSONObject newFeedback = TipsUtils.newFeedback(user, memo, type,
					date);

			f_array.add(newFeedback);

			// 更新feedback
			feedBack.put("f_array", f_array);

			Put put = new Put(rowkey.getBytes());

			put.addColumn("data".getBytes(), "track".getBytes(), track
					.toString().getBytes());

			put.addColumn("data".getBytes(), "feedback".getBytes(), feedBack
					.toString().getBytes());

			// 同步更新solr

			solrIndex.put("stage", stage); 

			solrIndex.put("t_date", date);

			//
			solrIndex.put("t_lifecycle", 2);

			solrIndex.put("handler", user);

			solrIndex.put("feedback", newFeedback);

			solr.addTips(solrIndex);

			htab.put(put);
			
			htab.close();

		} catch (IOException e) {
			
			logger.error(e.getMessage(), e);
			
			throw new Exception("改备注信息出错：rowkey:"+rowkey+"原因：" + e.getMessage(), e);
		}

	}
	
	
	

	
	/**
	 * @Description:获取到tips改前的信息
	 * @param rowkey
	 * @param htab
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2016-11-16 下午2:16:44
	 */
	protected JSONObject getOldTips(String rowkey, Table htab) throws Exception {
		JSONObject oldTip = null;
		List<Get> gets = new ArrayList<Get>();

		Get get = new Get(rowkey.getBytes());

		get.addColumn("data".getBytes(), "track".getBytes());

		get.addColumn("data".getBytes(), "feedback".getBytes());
		
		get.addColumn("data".getBytes(), "deep".getBytes());

		gets.add(get);

		Result[] results = htab.get(gets);

		for (Result result : results) {

			if (result.isEmpty()) {
				continue;
			}

			// String rowkey = new String(result.getRow());

			try {
				JSONObject jo = new JSONObject();
			
				String track = new String(result.getValue("data".getBytes(),
						"track".getBytes()));
				jo.put("track",track);

				
				if (result.containsColumn("data".getBytes(),
						"feedback".getBytes())) {
					JSONObject feedback = JSONObject.fromObject(new String(
							result.getValue("data".getBytes(),
									"feedback".getBytes())));

					jo.put("feedback", feedback);
				} else {
					jo.put("feedback", TipsUtils.OBJECT_NULL_DEFAULT_VALUE);
				}
				
				
				byte[] deepByte=result.getValue("data".getBytes(),
						"deep".getBytes());
				
				String deep=null;
				
				if(deepByte!=null){
					 deep = new String(deepByte);
				}
				
				jo.put("deep",deep);
				
				oldTip = jo;
			} catch (Exception e) {
				logger.error("根据rowkey查询tips信息出错：" + rowkey + "\n" + e.getMessage(), e.getCause());
				
				throw new Exception(
						"根据rowkey查询tips信息出错：" + rowkey + "\n" + e.getMessage(), e);
			}
		}
		return oldTip;
	}

	

}
