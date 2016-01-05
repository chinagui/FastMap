package com.navinfo.dataservice.FosEngine.tips;

import java.util.ArrayList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.hbase.async.GetRequest;
import org.hbase.async.KeyValue;
import org.hbase.async.PutRequest;

import com.navinfo.dataservice.FosEngine.comm.util.StringUtils;
import com.navinfo.dataservice.commons.db.HBaseAddress;
import com.navinfo.dataservice.solr.core.SConnection;

public class TipsOperator {
	
	private SConnection solrConn;
	
	public TipsOperator(String solrUrl){
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
	public boolean update(String rowkey, int stage, int handler)
			throws Exception {

		final GetRequest get = new GetRequest("tips", rowkey, "data", "track");

		ArrayList<KeyValue> list = HBaseAddress.getHBaseClient().get(get)
				.joinUninterruptibly();
		
		for (KeyValue kv : list) {
			JSONObject track = JSONObject.fromObject(new String(kv.value()));

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
			
			PutRequest put = new PutRequest("tips", rowkey, "data", "track", track.toString());

			HBaseAddress.getHBaseClient().put(put);
			
			JSONObject solrIndex = solrConn.getById(rowkey);
			
			solrIndex.put("stage", stage);
			
			solrIndex.put("date", date);
			
			if (0 == lifecycle) {
				solrIndex.put("t_lifecycle", 2);
			}
			
			solrIndex.put("handler", handler);
			
			solrConn.addTips(solrIndex);
			
			solrConn.persistentData();
			
			solrConn.closeConnection();
		}

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
