package com.navinfo.dataservice.engine.fcc.tips;

import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.commons.db.HBaseAddress;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.fcc.SolrBulkUpdater;

public class TipsReshaper {

	private int cache = 10000;

	private SolrBulkUpdater solr;

	public TipsReshaper() {

		// solr = new SolrBulkUpdater(TipsImportUtils.QueueSize,
		// TipsImportUtils.ThreadCount);

		solr = new SolrBulkUpdater(cache, 5);
	}

	public int run() throws Exception {

		Connection hbaseConn = HBaseAddress.getHBaseConnection();

		Table htab = hbaseConn.getTable(TableName.valueOf("tips"));

		Scan scan = new Scan();

		scan.setCaching(cache);

		ResultScanner scanner = htab.getScanner(scan);

		int count = 0;

		Iterator<Result> iter = scanner.iterator();

		while (iter.hasNext()) {

			Result result = iter.next();

			JSONObject solrIndex = new JSONObject();

			String rowkey = new String(result.getRow());

			solrIndex.put("id", rowkey);

			// geometry

			String geometry = new String(result.getValue("data".getBytes(),
					"geometry".getBytes()));

			JSONObject geojo = JSONObject.fromObject(geometry);

			JSONObject g_location = geojo.getJSONObject("g_location");

			solrIndex.put("g_location", g_location);

			JSONObject g_guide = geojo.getJSONObject("g_guide");

			solrIndex.put("g_guide", g_guide);

			solrIndex.put("wkt", GeoTranslator.jts2Wkt(GeoTranslator
					.geojson2Jts(g_location)));

			// track
			String track = new String(result.getValue("data".getBytes(),
					"track".getBytes()));

			JSONObject trackjo = JSONObject.fromObject(new String(track));

			int t_lifecycle = trackjo.getInt("t_lifecycle");

			solrIndex.put("t_lifecycle", t_lifecycle);

			int t_command = trackjo.getInt("t_command");

			solrIndex.put("t_command", t_command);

			solrIndex.put("t_date", trackjo.getString("t_date"));

			JSONArray tTrackInfo = trackjo.getJSONArray("t_trackInfo");

			JSONObject lastTrack = tTrackInfo
					.getJSONObject(tTrackInfo.size() - 1);

			String lastDate = lastTrack.getString("date");

			solrIndex.put("t_operateDate", lastDate);

			int stage = lastTrack.getInt("stage");

			solrIndex.put("stage", stage);

			int handler = lastTrack.getInt("handler");

			solrIndex.put("handler", handler);

			// source
			String source = new String(result.getValue("data".getBytes(),
					"source".getBytes()));

			JSONObject sourcejo = JSONObject.fromObject(source);

			String sourceType = sourcejo.getString("s_sourceType");

			solrIndex.put("s_sourceType", sourceType);

			solrIndex.put("s_sourceCode", sourcejo.getInt("s_sourceCode"));

			// deep
			String deep = new String(result.getValue("data".getBytes(),
					"deep".getBytes()));

			JSONObject deepjo = JSONObject.fromObject(deep);

			solrIndex.put("deep", deep);

			// feedback
			JSONArray feedbacks = new JSONArray();

			if (result.containsColumn("data".getBytes(), "feedback".getBytes())) {
				String feedback = new String(result.getValue("data".getBytes(),
						"feedback".getBytes()));
				JSONObject feedbackjo = JSONObject.fromObject(feedback);

				feedbacks = feedbackjo.getJSONArray("f_array");
			}

			solrIndex.put("feedback", feedbacks.toString());

			// wkt
			solrIndex.put("wkt", TipsImportUtils.generateSolrWkt(sourceType,
					deepjo, g_location, feedbacks));

			solr.addTips(solrIndex);

			count += 1;

			if (count % cache == 0) {
				System.out.println(count);
			}
		}

		solr.commit();

		solr.close();

		return count;
	}

	public static void main(String[] args) throws Exception {

		long s = System.currentTimeMillis();

		HBaseAddress.initHBaseAddress("192.168.3.156");

		TipsReshaper sa = new TipsReshaper();

		System.out.println(sa.run());

		System.out.println(System.currentTimeMillis() - s);
	}
}
