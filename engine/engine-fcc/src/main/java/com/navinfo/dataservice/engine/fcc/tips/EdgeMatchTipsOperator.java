package com.navinfo.dataservice.engine.fcc.tips;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;

public class EdgeMatchTipsOperator {

	private SolrController solr = new SolrController();
	
	static String S_SOURCETYPE="8002";//接边标识tips类型

	private static final Logger logger = Logger
			.getLogger(EdgeMatchTipsOperator.class);

	public EdgeMatchTipsOperator() {

	}

	/**
	 * @Description:创建一个tips
	 * @param sourceType
	 * @param g_location
	 * @param g_guide
	 * @param content
	 *            :feedback.content
	 * @param deep
	 * @author: y
	 * @param user
	 * @param memo 
	 * @param type
	 * @throws Exception
	 * @time:2016-11-15 上午11:03:20
	 */
	public String create( JSONObject g_location, String content, int user, String memo) throws Exception {

		Connection hbaseConn;
		try {
			hbaseConn = HBaseConnector.getInstance().getConnection();
			Table htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));

			// 1.rowkey
			String rowkey = TipsUtils.getNewRowkey(S_SOURCETYPE);

			// 2.feedback
			String currentDate = DateUtils.dateToString(new Date(),
					DateUtils.DATE_COMPACTED_FORMAT);

			JSONObject feedbackObj = new JSONObject();

			JSONArray f_array = new JSONArray();
			
			//geo
			int type=6;
			JSONObject newFeedback = TipsUtils.newFeedback(user, content, type,
						currentDate);
			f_array.add(newFeedback);
			
			//memo,如果有，则增加一个备注
			if(StringUtils.isNotEmpty(memo)){
				type=3;
				JSONObject newFeedback2 = TipsUtils.newFeedback(user, memo, type,
							currentDate);
				f_array.add(newFeedback2);
			}
			
			feedbackObj.put("f_array", f_array);
			
			
			// 3.track
			int stage = 2;

			int t_lifecycle = 3;
			int t_command = 0;
			int t_cStatus = 0;
			int t_dStatus = 0;
			int t_mStatus = 0;
			int t_inStatus = 0;
			int t_inMeth = 1;

			JSONObject jsonTrack = TipsUtils.generateTrackJson(t_lifecycle,stage,
					user, t_command, null, currentDate,currentDate,t_cStatus, t_dStatus,
					t_mStatus, t_inStatus, t_inMeth);

			// 4.geometry
			JSONObject jsonGeom = new JSONObject();
			JSONObject g_guide=g_location; //g_guide和g_location值一样
			jsonGeom.put("g_location", g_location);
			jsonGeom.put("g_guide",g_guide ); 

			// source
			int s_sourceCode = 15;
			int s_reliability = 100;
			JSONObject source = new JSONObject();
			source.put("s_featureKind", 2);
			source.put("s_project", TipsUtils.STRING_NULL_DEFAULT_VALUE);
			source.put("s_sourceCode", s_sourceCode);
			source.put("s_sourceId", TipsUtils.STRING_NULL_DEFAULT_VALUE);
			source.put("s_sourceType", S_SOURCETYPE);
			source.put("s_reliability", 100);
			source.put("s_sourceProvider", 0);

			// deep;
			/*Object deepObj = null;
			if (!StringUtils.isEmpty(deep)) {
				deepObj = JSONObject.fromObject(deep);
			}*/

			// put
			Put put = new Put(rowkey.getBytes());

			put.addColumn("data".getBytes(), "track".getBytes(), jsonTrack
					.toString().getBytes());

			put.addColumn("data".getBytes(), "geometry".getBytes(), jsonGeom
					.toString().getBytes());

			put.addColumn("data".getBytes(), "feedback".getBytes(), feedbackObj
					.toString().getBytes());

			put.addColumn("data".getBytes(), "source".getBytes(), source
					.toString().getBytes());

			/*if (!StringUtils.isEmpty(deep)) {
				put.addColumn("data".getBytes(), "deep".getBytes(), deepObj
						.toString().getBytes());
			}*/

			// solr index json

			JSONObject solrIndex = TipsUtils.generateSolrIndex(rowkey, stage,
					currentDate, currentDate, t_lifecycle, t_command, user,
					t_cStatus, t_dStatus, t_mStatus, S_SOURCETYPE, s_sourceCode,
					g_guide, g_location, null, f_array, s_reliability,t_inStatus,t_inMeth);

			solr.addTips(solrIndex);

			List<Put> puts = new ArrayList<Put>();

			puts.add(put);

			htab.put(puts);

			htab.close();
			
			htab.close();
			
			return rowkey;

		} catch (IOException e) {
			logger.error("新增tips出错：原因：" + e.getMessage());
			throw new Exception("新增tips出错：原因：" + e.getMessage(), e);
		}

	}

	/**
	 * @Description:TOOD
	 * @param rowkey
	 * @param user
	 * @param memo
	 * @author: y
	 * @throws Exception
	 * @time:2016-11-16 上午11:29:03
	 */
	public void updateFeedbackMemo(String rowkey, int user, String memo,
			int stage) throws Exception {

		try {

			Connection hbaseConn = HBaseConnector.getInstance().getConnection();

			Table htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));

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

			// track.put("t_lifecycle", lifeCycle);//不需要需改 ????

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
			JSONObject solrIndex = solr.getById(rowkey);

			//solrIndex.put("stage", stage);

			solrIndex.put("t_date", date);

			// ???????????lifyCycle要不要更新?已确认，不需要修改。作业不关心
			// solrIndex.put("t_lifecycle", 2);

			solrIndex.put("handler", user);

			solrIndex.put("feedback", f_array);

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
	private JSONObject getOldTips(String rowkey, Table htab) throws Exception {
		JSONObject oldTip = null;
		List<Get> gets = new ArrayList<Get>();

		Get get = new Get(rowkey.getBytes());

		get.addColumn("data".getBytes(), "track".getBytes());

		get.addColumn("data".getBytes(), "feedback".getBytes());

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
				oldTip = jo;
			} catch (Exception e) {
				logger.error("根据rowkey查询tips信息出错：" + rowkey + "\n" + e.getMessage(), e.getCause());
				
				throw new Exception(
						"根据rowkey查询tips信息出错：" + rowkey + "\n" + e.getMessage(), e);
			}
		}
		return oldTip;
	}

	/**
	 * @Description:删除tips
	 * @param rowkey
	 * @author: y
	 * @throws Exception
	 * @time:2016-11-16 下午5:21:09
	 */
	public void deleteByRowkey(String rowkey) throws Exception {
		Connection hbaseConn;
		try {
			// delete solr
			solr.deleteByRowkey(rowkey);

			// delete hbase
			hbaseConn = HBaseConnector.getInstance().getConnection();
			
			Table htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));
			
			List list = new ArrayList();
			Delete d1 = new Delete(rowkey.getBytes());
			list.add(d1);
			
			htab.delete(list);
			
			htab.close();
		} catch (SolrServerException e) {

			logger.error("删除tips失败，rowkey：" + rowkey + "\n" + e.getMessage(), e);
			
			throw new Exception(
					"删除tips失败，rowkey：" + rowkey + "\n" + e.getMessage(), e);
		}

	}

}
