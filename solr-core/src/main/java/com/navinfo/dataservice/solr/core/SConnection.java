package com.navinfo.dataservice.solr.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.Geojson;

public class SConnection {

	private SolrClient solrClient;

	private List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();

	private int flushCnt = 5000;

	private int fetchNum = Integer.MAX_VALUE;

	public SConnection(String url) {
		solrClient = new HttpSolrClient(url);
	}

	public SConnection(String url, int flushCnt) {
		solrClient = new HttpSolrClient(url);

		this.flushCnt = flushCnt;
	}

	private void flushData() throws SolrServerException, IOException {

		if (docs.size() > 0) {
			solrClient.add(docs);

			docs.clear();
		}
	}

	public void addTips(JSONObject json) throws JSONException,
			SolrServerException, IOException {

		SolrInputDocument doc = new SolrInputDocument();

		doc.addField("id", json.getString("id"));

		doc.addField("wkt", json.getString("wkt"));

		doc.addField("stage", json.getInt("stage"));

		doc.addField("date", json.getString("date"));

		doc.addField("t_lifecycle", json.getInt("t_lifecycle"));

		doc.addField("t_command", json.getInt("t_command"));

		doc.addField("handler", json.getInt("handler"));

		doc.addField("s_sourceCode", json.getInt("s_sourceCode"));

		doc.addField("s_sourceType", json.getString("s_sourceType"));

		doc.addField("g_location", json.getString("g_location"));

		doc.addField("g_guide", json.getString("g_guide"));
		
		doc.addField("deep", json.getString("deep"));

		docs.add(doc);

		if (docs.size() >= flushCnt) {
			this.flushData();

		}
	}

	public boolean checkTipsMobile(String wkt, String date)
			throws SolrServerException, IOException {

		String param = "wkt:\"intersects(" + wkt + ")\" AND date:[" + date
				+ " TO *]";

		SolrQuery query = new SolrQuery();

		query.set("q", param);

		query.addField("id");

		query.set("start", 0);

		query.set("rows", 1);

		QueryResponse response = solrClient.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum > 0) {
			return true;
		} else {
			return false;
		}
	}

	public List<String> queryTipsMobile(String wkt, String date)
			throws SolrServerException, IOException {
		List<String> rowkeys = new ArrayList<String>();

		String param = "wkt:\"intersects(" + wkt + ")\"";

		if (date != null && !date.equals("")) {
			param += " AND date:[" + date + " TO *]";
		}

		SolrQuery query = new SolrQuery();

		query.set("q", param);

		query.addField("id");

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = solrClient.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		for (int i = 0; i < totalNum; i++) {
			SolrDocument doc = sdList.get(i);

			rowkeys.add(doc.get("id").toString());
		}

		return rowkeys;
	}

	public List<JSONObject> queryTipsWeb(String wkt, int z, double px, double py)
			throws SolrServerException, IOException {
		List<JSONObject> snapshots = new ArrayList<JSONObject>();

		String param = "wkt:\"intersects(" + wkt + ")\"";

		SolrQuery query = new SolrQuery();

		query.set("q", param);

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = solrClient.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum <= fetchNum) {
			for (int i = 0; i < totalNum; i++) {
				SolrDocument doc = sdList.get(i);

				JSONObject snapshot = JSONObject.fromObject(doc);

				String location = snapshot.getString("g");

				snapshot.put("g", this.covertLonLat2Piexls(location, z, px, py));

				snapshots.add(snapshot);
			}
		} else {
			// 暂先不处理
		}

		return snapshots;
	}

	public List<JSONObject> queryTipsWeb(String wkt, int type, JSONArray stages)
			throws SolrServerException, IOException {
		List<JSONObject> snapshots = new ArrayList<JSONObject>();

		StringBuilder builder = new StringBuilder();
		
		builder.append("wkt:\"intersects(");
		
		builder.append(wkt);
		
		builder.append(")\" AND s_sourceType:");
		
		builder.append(type);

		if(stages.size()>0){
			
			builder.append(" AND stage:(");

			for (int i = 0; i < stages.size(); i++) {
				int stage = stages.getInt(i);
	
				if(i>0){
					builder.append(" ");
				}
				builder.append(stage);
			}
			
			builder.append(")");
		}

		SolrQuery query = new SolrQuery();

		query.set("q", builder.toString());

		query.set("sort", "date desc");

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = solrClient.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum <= fetchNum) {
			for (int i = 0; i < totalNum; i++) {
				SolrDocument doc = sdList.get(i);

				JSONObject snapshot = JSONObject.fromObject(doc);

				snapshots.add(snapshot);
			}
		} else {
			// 暂先不处理
		}

		return snapshots;
	}

	public List<JSONObject> queryTipsWeb(String wkt, JSONArray stages)
			throws SolrServerException, IOException {
		List<JSONObject> snapshots = new ArrayList<JSONObject>();

		String param = "wkt:\"intersects(" + wkt + ")\"";

		String fq = "";

		for (int i = 0; i < stages.size(); i++) {
			int stage = stages.getInt(i);

			fq += "stage:" + stage;

			if (i != stages.size() - 1) {
				fq += " OR ";
			}
		}

		SolrQuery query = new SolrQuery();

		query.set("q", param);

		query.set("fq", fq);

		query.set("start", 0);

		query.set("rows", fetchNum);

		query.addField("s_sourceType");

		QueryResponse response = solrClient.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum <= fetchNum) {
			for (int i = 0; i < totalNum; i++) {
				SolrDocument doc = sdList.get(i);

				JSONObject snapshot = JSONObject.fromObject(doc);

				snapshots.add(snapshot);
			}
		} else {
			// 暂先不处理
		}

		return snapshots;
	}

	public List<JSONObject> queryTipsWeb(String wkt)
			throws SolrServerException, IOException {
		List<JSONObject> snapshots = new ArrayList<JSONObject>();

		String param = "wkt:\"intersects(" + wkt + ")\"";

		SolrQuery query = new SolrQuery();

		query.set("q", param);

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = solrClient.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum <= fetchNum) {
			for (int i = 0; i < totalNum; i++) {
				SolrDocument doc = sdList.get(i);

				JSONObject snapshot = JSONObject.fromObject(doc);

				snapshots.add(snapshot);
			}
		} else {
			// 暂先不处理
		}

		return snapshots;
	}
	
	public List<JSONObject> queryTipsWebType(String wkt, JSONArray types)
			throws SolrServerException, IOException {
		List<JSONObject> snapshots = new ArrayList<JSONObject>();
		
		StringBuilder builder = new StringBuilder();
		
		builder.append("wkt:\"intersects(" + wkt + ")\"  AND stage:(1 3)");
		
		if(types.size()>0){
			
			builder.append(" AND s_sourceType:(");
			
			for(int i=0;i<types.size();i++){
				String type = types.getString(i);
				
				if(i>0){
					builder.append(" ");
				}
				builder.append(type);
			}
			
			builder.append(")");
		}
		
		SolrQuery query = new SolrQuery();

		query.set("q", builder.toString());

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = solrClient.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum <= fetchNum) {
			for (int i = 0; i < totalNum; i++) {
				SolrDocument doc = sdList.get(i);

				JSONObject snapshot = JSONObject.fromObject(doc);

				snapshots.add(snapshot);
			}
		} else {
			// 暂先不处理
		}

		return snapshots;
	}


	public JSONObject getById(String id) throws Exception {

		String param = "id:" + id;

		SolrQuery query = new SolrQuery();

		query.set("q", param);

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = solrClient.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum == 0) {
			return null;
		}

		SolrDocument doc = sdList.get(0);

		JSONObject snapshot = JSONObject.fromObject(doc);

		return snapshot;
	}

	public void persistentData() throws SolrServerException, IOException {

		this.flushData();

		solrClient.commit();
	}

	public void closeConnection() throws IOException {
		solrClient.close();
	}

	private String covertLonLat2Piexls(String location, int z, double px,
			double py) {
		StringBuilder sb = new StringBuilder();

		String regex = "[0-9.]+,[0-9.]+";

		String[] splits = location.split(regex);

		sb.append(splits[0]);

		int pos = 1;

		Pattern pattern = Pattern.compile(regex);

		Matcher matcher = pattern.matcher(location);

		while (matcher.find()) {
			String lonlatStr = matcher.group();

			String[] lonlat = lonlatStr.split(",");

			double lon = Double.parseDouble(lonlat[0]);

			double lat = Double.parseDouble(lonlat[1]);

			JSONArray ja = Geojson.lonlat2Pixel(lon, lat, z, px, py);

			sb.append(ja.getInt(0));

			sb.append(",");

			sb.append(ja.getInt(1));

			sb.append(splits[pos++]);
		}

		return sb.toString();
	}
}
