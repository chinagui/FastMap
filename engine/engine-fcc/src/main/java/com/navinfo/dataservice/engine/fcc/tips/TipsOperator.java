package com.navinfo.dataservice.engine.fcc.tips;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;

public class TipsOperator {

	private SolrController solr = new SolrController();

	private static final Logger logger = Logger.getLogger(TipsOperator.class);

	public TipsOperator() {

	}

	/**
	 * 修改tips
	 * 
	 * @param rowkey
	 * @param mdFlag
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public boolean update(String rowkey, int handler, String pid, String mdFlag)
			throws Exception {

		Connection hbaseConn = HBaseConnector.getInstance().getConnection();

		Table htab = hbaseConn
				.getTable(TableName.valueOf(HBaseConstant.tipTab));

		Get get = new Get(rowkey.getBytes());

		get.addColumn("data".getBytes(), "track".getBytes());

		if (StringUtils.isNotEmpty(pid)) {
			get.addColumn("data".getBytes(), "deep".getBytes());
		}

		Result result = htab.get(get);

		if (result.isEmpty()) {
			return false;
		}

		Put put = new Put(rowkey.getBytes());

		JSONObject track = JSONObject.fromObject(new String(result.getValue(
				"data".getBytes(), "track".getBytes())));

		int lifecycle = track.getInt("t_lifecycle");

		if (0 == lifecycle) {
			track.put("t_lifecycle", 2);
		}

		JSONArray trackInfo = track.getJSONArray("t_trackInfo");

		int stage = 1;

		int tDStatus = track.getInt("t_dStatus");

		int tMStatus = track.getInt("t_mStatus");

		JSONObject jo = new JSONObject();

		if ("d".equals(mdFlag)) {

			stage = 2;

			tDStatus = 1;

		}

		else if ("m".equals(mdFlag)) {

			stage = 3;

			tMStatus = 1;

		}

		jo.put("stage", stage);

		track.put("t_mStatus", tMStatus);

		track.put("t_dStatus", tDStatus);

		String date = StringUtils.getCurrentTime();

		jo.put("date", date);

		jo.put("handler", handler);

		trackInfo.add(jo);

		track.put("t_trackInfo", trackInfo);

		track.put("t_date", date);

		put.addColumn("data".getBytes(), "track".getBytes(), track.toString()
				.getBytes());

		String newDeep = null;

		if (StringUtils.isNotEmpty(pid)) {

			JSONObject deep = JSONObject.fromObject(new String(result.getValue(
					"data".getBytes(), "deep".getBytes())));
			if (deep.containsKey("id")) {
				deep.put("id", String.valueOf(pid));

				newDeep = deep.toString();

				put.addColumn("data".getBytes(), "deep".getBytes(), deep
						.toString().getBytes());
			}
		}

		JSONObject solrIndex = solr.getById(rowkey);

		solrIndex.put("stage", stage);

		solrIndex.put("t_date", date);

		solrIndex.put("t_dStatus", tDStatus);

		solrIndex.put("t_mStatus", tMStatus);

		if (0 == lifecycle) {
			solrIndex.put("t_lifecycle", 2);
		}

		solrIndex.put("handler", handler);

		if (newDeep != null) {
			solrIndex.put("deep", newDeep);
		}

		solr.addTips(solrIndex);

		htab.put(put);

		return true;
	}

	/**
	 * 删除tips
	 * 
	 * @param rowkey
	 * @return
	 */
	public boolean delete(String rowkey) {
		return false;
	}

	/**
	 * @Description:批量更新tips状态
	 * @param data
	 *            ：数据 JSON格式：{rowkey:'',status:''}
	 * @param handler
	 *            ：作业员id
	 * @param mdFlag
	 *            ：m 月编；d：日编
	 * @author: y
	 * @throws Exception
	 * @time:2017-2-8 下午3:17:20
	 */
	public void batchUpdateStatus(JSONArray data, int handler, String mdFlag)
			throws Exception {

		for (Object object : data) {

			JSONObject json = JSONObject.fromObject(object);

			String rowkey = json.getString("rowkey");

			int status = json.getInt("status");

			JSONObject updateKeyValues = new JSONObject(); // 被修改的tips字段的值

			// new 一个trackInfo

			String date = DateUtils.dateToString(new Date(), "yyyyMMddHHmmss");

			JSONObject jsonTrackInfo = new JSONObject();

			JSONObject value = new JSONObject();

			// 日编
			if ((status == 0 || status == 1) && "d".equals(mdFlag)) {

				if (status == 0) {

					jsonTrackInfo.put("stage", 1);

					jsonTrackInfo.put("date", date);

					jsonTrackInfo.put("handler", handler);
					
					value.put("t_dInProc", 0); 
				}
				if (status == 1) {

					jsonTrackInfo.put("stage", 2);

					jsonTrackInfo.put("date", date);

					jsonTrackInfo.put("handler", handler);
					
					value.put("t_dInProc", 0); 

				}

				value.put("t_dStatus", status);

				value.put("t_date", date);

			}
			// 月编
			else if ((status == 0 || status == 1) && "m".equals(mdFlag)) {

				if (status == 0) {

					jsonTrackInfo.put("stage", 2);

					jsonTrackInfo.put("date", date);

					jsonTrackInfo.put("handler", handler);
					
					value.put("t_mInProc", 0);

				}
				if (status == 1) {

					jsonTrackInfo.put("stage", 3);

					jsonTrackInfo.put("date", date);

					jsonTrackInfo.put("handler", handler);
					
					value.put("t_mInProc", 0);

				}

				value.put("t_mStatus", status);

			}
			// 有问题待确认
			else if (status == 2) {

				if ("d".equals(mdFlag)) {

					jsonTrackInfo.put("stage", 1);// 有问题 则stage需要改为1 未作业(日编)

					jsonTrackInfo.put("date", date);

					jsonTrackInfo.put("handler", handler);

					value.put("t_dStatus", 0); // 未作业
					
					value.put("t_dInProc", 1); // 有问题待确认
					
				} else if ("m".equals(mdFlag)) {

					jsonTrackInfo.put("stage", 2);// 有问题 则stage需要改为2未作业(月编)

					jsonTrackInfo.put("date", date);

					jsonTrackInfo.put("handler", handler);

					value.put("t_mStatus", 0); // 未作业
					
					value.put("t_mInProc", 1); // 有问题待确认
				}


			}

			value.put("t_lifecycle", 2);

			value.put("t_date", date);

			value.put("t_trackInfo", jsonTrackInfo);

			updateKeyValues.put("track", value);

			updateTips(rowkey, updateKeyValues);

		}

	}

	/**
	 * @Description:修改tips的属性
	 * @param rowkey
	 *            ：tips的rowkey
	 * @param updateKeyValues
	 *            JSONJoject:被修改的属性的键值对
	 * @author: y
	 * @throws Exception
	 * @time:2017-2-8 下午3:44:46
	 */
	private void updateTips(String rowkey, JSONObject updateKeyValues)
			throws Exception {

		Set<String> updateAttKeys = updateKeyValues.keySet(); // 被修改的属性

		Connection hbaseConn;
		try {
			hbaseConn = HBaseConnector.getInstance().getConnection();

			Table htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));

			// 1.获取到改前的 feddback和track （还有deep）
			JSONObject oldTip = getOldTips(rowkey, htab);

			JSONObject track = oldTip.getJSONObject("track");

			Put put = new Put(rowkey.getBytes());

			// 更新字段
			for (String key : updateAttKeys) {

				// 1.如果是track，则需要特殊处理
				if ("track".equals(key)) {

					JSONObject trackValues = updateKeyValues
							.getJSONObject("track");

					Set<String> updateTrackFiledsName = trackValues.keySet(); // 被修改的属性

					// 1.1trackInfo特殊处理
					JSONObject jsonTrackInfo = trackValues
							.getJSONObject("t_trackInfo");

					JSONArray trackInfoArr = track.getJSONArray("t_trackInfo");

					// 更新hbase 增一个trackInfo
					trackInfoArr.add(jsonTrackInfo);

					track.put("t_trackInfo", trackInfoArr);

					// 1.2更新track的其他字段
					for (String filedName : updateTrackFiledsName) {

						if ("t_trackInfo".equals(filedName)) {
							continue;
						}

						track.put(filedName, trackValues.get(filedName));

					}

					// 1.3hbase 更新track
					put.addColumn("data".getBytes(), "track".getBytes(), track
							.toString().getBytes());

				}

				// track和feedback外的其他字段直接更新（这个地方需要补充呢，如果是feebback？？）
				// 且source_type：solr怎么更新
				else {
					put.addColumn("data".getBytes(), key.getBytes(),
							updateKeyValues.get(key).toString().getBytes());
				}

				// （这个地方需要补充呢，如果是feebback？？）
				// 且source_type：solr怎么更新,如果更新的是坐标。。。那么wkt要维护

				// 根据修改的字段，更新solr
				updateSorlIndex(rowkey, updateKeyValues);

				htab.put(put);

				htab.close();

			}
		} catch (Exception e) {

			logger.error(
					"根据rowkey修改tips信息出错：" + rowkey + "\n" + e.getMessage(),
					e.getCause());

			throw new Exception("根据rowkey修改tips信息出错:" + rowkey + "\n"
					+ e.getMessage(), e);
		}

	}

	/**
	 * @Description:根据修改后的字段，更新solr信息
	 * @param rowkey
	 * @param updateKeyValues
	 *            ，修改了的字段。json对象
	 * @author: y
	 * @throws Exception 
	 * @time:2017-2-9 上午10:02:10
	 */
	private void updateSorlIndex(String rowkey, JSONObject updateKeyValues) throws Exception {

		// 获取solr数据
		JSONObject solrIndex = solr.getById(rowkey);

		// 更新字段
		Set<String> updateAttKeys = updateKeyValues.keySet(); // 被修改的属性
		for (String key : updateAttKeys) {

			// 1.track相关字段的更新
			if ("track".equals(key)) {

				JSONObject trackValues = updateKeyValues.getJSONObject("track");

				JSONObject jsonTrackInfo = trackValues
						.getJSONObject("t_trackInfo");

				solrIndex.put("stage", jsonTrackInfo.getInt("stage"));

				solrIndex.put("handler", jsonTrackInfo.getInt("handler"));

				Set<String> updateTrackFiledsName = trackValues.keySet(); // 被修改的track属性

				// 1.2更新track的其他字段
				for (String filedName : updateTrackFiledsName) {

					if ("t_trackInfo".equals(filedName)) {
						continue;
					}

					solrIndex.put(filedName, trackValues.get(filedName));

				}
			}

			// 暂时不修改，待补充 ??????????????????
			if ("source".equals(key)) {

			}

			// 暂时不修改，待完善，编辑端给的是不是编辑后合并好的，如果不是需要在这里用旧的tips信息进行合并 ?????????
			if ("feedback".equals(key)) {

				solrIndex.put("feedback", updateKeyValues.get(key));
			}

			if ("deep".equals(key)) {

				solrIndex.put("deep", updateKeyValues.get(key));
			}
			
			//?????????有没有其他要改的？
			if ("geometry".equals(key)) {

				JSONObject geoJson = updateKeyValues.getJSONObject("geometry");

				if (geoJson.containsKey("g_location")) {

					solrIndex.put("g_location", geoJson.get("g_location"));
				}

				if (geoJson.containsKey("g_guide")) {

					solrIndex.put("g_guide", geoJson.get("g_guide"));
				}

				solrIndex.put("wkt", TipsImportUtils.generateSolrWkt(solrIndex
						.getString("s_sourceType"),
						JSONObject.fromObject(solrIndex.get("deep")),
						JSONObject.fromObject(solrIndex.get("g_location")),
						JSONObject.fromObject(solrIndex.get("feedback"))));
			}
			
		}

		solr.addTips(solrIndex);

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
				jo.put("track", track);

				if (result.containsColumn("data".getBytes(),
						"feedback".getBytes())) {
					JSONObject feedback = JSONObject.fromObject(new String(
							result.getValue("data".getBytes(),
									"feedback".getBytes())));

					jo.put("feedback", feedback);
				} else {
					jo.put("feedback", TipsUtils.OBJECT_NULL_DEFAULT_VALUE);
				}

				byte[] deepByte = result.getValue("data".getBytes(),
						"deep".getBytes());

				String deep = null;

				if (deepByte != null) {
					deep = new String(deepByte);
				}

				jo.put("deep", deep);

				oldTip = jo;
			} catch (Exception e) {
				logger.error(
						"根据rowkey查询tips信息出错：" + rowkey + "\n" + e.getMessage(),
						e.getCause());

				throw new Exception("根据rowkey查询tips信息出错：" + rowkey + "\n"
						+ e.getMessage(), e);
			}
		}
		return oldTip;
	}

}
