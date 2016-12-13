package com.navinfo.dataservice.engine.fcc.tips;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;

public class TipsOperator {

	private SolrController solr = new SolrController();

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




}
