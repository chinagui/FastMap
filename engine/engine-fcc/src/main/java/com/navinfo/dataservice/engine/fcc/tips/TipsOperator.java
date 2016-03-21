package com.navinfo.dataservice.engine.fcc.tips;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.commons.db.HBaseAddress;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.SConnection;

public class TipsOperator {

	private SConnection solrConn;

	public TipsOperator(String solrUrl) {
		solrConn = new SConnection(solrUrl);
	}

	/**
	 * 修改tips
	 * 
	 * @param rowkey
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public boolean update(String rowkey, int stage, int handler, int pid)
			throws Exception {

		Connection hbaseConn = HBaseAddress.getHBaseConnection();

		Table htab = hbaseConn.getTable(TableName.valueOf("tips"));

		Get get = new Get(rowkey.getBytes());

		get.addColumn("data".getBytes(), "track".getBytes());

		if (pid > 0) {
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

		JSONObject jo = new JSONObject();

		jo.put("stage", stage);

		String date = StringUtils.getCurrentTime();

		jo.put("date", date);

		jo.put("handler", handler);

		trackInfo.add(jo);

		track.put("t_trackInfo", trackInfo);
		
		track.put("t_date", date);

		put.addColumn("data".getBytes(), "track".getBytes(), track.toString()
				.getBytes());

		String newDeep=null;
		
		if (pid > 0) {

			JSONObject deep = JSONObject.fromObject(new String(result.getValue(
					"data".getBytes(), "deep".getBytes())));
			if (deep.containsKey("id")) {
				deep.put("id", String.valueOf(pid));
				
				newDeep = deep.toString();

				put.addColumn("data".getBytes(), "deep".getBytes(), deep
						.toString().getBytes());
			}
		}

		JSONObject solrIndex = solrConn.getById(rowkey);

		solrIndex.put("stage", stage);

		solrIndex.put("t_date", date);

		if (0 == lifecycle) {
			solrIndex.put("t_lifecycle", 2);
		}

		solrIndex.put("handler", handler);
		
		if(newDeep != null){
			solrIndex.put("deep", newDeep);
		}
		
		solrConn.addTips(solrIndex);

		solrConn.persistentData();

		solrConn.closeConnection();
		
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
