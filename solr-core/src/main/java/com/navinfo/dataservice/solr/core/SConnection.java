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

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;

public class SConnection {

	private SolrClient solrClient;
	
	private List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
	
	private int flushCnt = 5000;
	
	private int fetchNum = 1000;
	
	public SConnection(String url){
		solrClient = new HttpSolrClient(url);
	}
	
	public SConnection(String url,int flushCnt){
		solrClient = new HttpSolrClient(url);
		
		this.flushCnt = flushCnt;
	}
	
	
	private void flushData() throws SolrServerException, IOException{
		
		solrClient.add(docs);
		
		docs.clear();
		
	}
	
	public void addTips(JSONObject json) throws JSONException, SolrServerException, IOException {
		
		String id = json.getString("i");
		
		JSONObject geojson = json.getJSONObject("g");
		
		String wkt = GeoTranslator.jts2Wkt(GeoTranslator.geojson2Jts(geojson));
		
		String stage = json.getString("stage");
		
		SolrInputDocument doc = new SolrInputDocument();
		
		doc.addField("id", id);
		
		doc.addField("wkt", wkt);
		
		doc.addField("stage", stage);
		
		doc.addField("snapshot", json.toString());
		
		docs.add(doc);
		
		if (docs.size() >= flushCnt){
			this.flushData();
			
		}
	}
	
	
	public List<String> queryTipsMobile(String wkt) throws SolrServerException, IOException{
		List<String> rowkeys = new ArrayList<String>();
		
		String param = "wkt:\"intersects("+wkt+")\"";
		
		SolrQuery query = new SolrQuery();  
		
		query.set("q", param);
		
		query.addField("id");
		
		query.set("start", 0);
		
		query.set("rows", fetchNum);
		
        QueryResponse response = solrClient.query(query);
        
        SolrDocumentList sdList = response.getResults();
        
        long totalNum = sdList.getNumFound();
        
        if (totalNum <= fetchNum){
        	for(int i=0;i<totalNum;i++){
        		SolrDocument doc = sdList.get(i);
        		
        		rowkeys.add(doc.get("id").toString());
        	}
        }else{
        	//暂先不处理
        }
		
		return rowkeys;
	}
	
	
	public List<JSONObject> queryTipsWeb(String wkt,int z,double px,double py) throws SolrServerException, IOException{
		List<JSONObject> snapshots = new ArrayList<JSONObject>();
		
		String param = "wkt:\"intersects("+wkt+")\"";
		
		SolrQuery query = new SolrQuery();  
		
		query.set("q", param);
		
		query.addField("snapshot");
		
		query.set("start", 0);
		
		query.set("rows", fetchNum);
		
        QueryResponse response = solrClient.query(query);
        
        SolrDocumentList sdList = response.getResults();
        
        long totalNum = sdList.getNumFound();
        
        if (totalNum <= fetchNum){
        	for(int i=0;i<totalNum;i++){
        		SolrDocument doc = sdList.get(i);
        		
        		JSONObject snapshot = JSONObject.fromObject(doc.get("snapshot"));
        		
        		String location = snapshot.getString("g");
        		
        		snapshot.put("g", this.covertLonLat2Piexls(location, z, px, py));
        		
        		snapshots.add(snapshot);
        	}
        }else{
        	//暂先不处理
        }
		
		return snapshots;
	}
	
	public void persistentData() throws SolrServerException, IOException{
		
		this.flushData();
		
		solrClient.commit();
	}
	
	public void closeConnection() throws IOException{
		solrClient.close();
	}
	
	private String covertLonLat2Piexls(String location,int z,double px,double py){
		StringBuilder sb = new StringBuilder();
		
		String regex = "[0-9.]+,[0-9.]+";
		
		String[] splits = location.split(regex);
		
		sb.append(splits[0]);
		
		int pos = 1;
		
		Pattern pattern = Pattern.compile(regex);
		
		Matcher matcher = pattern.matcher(location);
		
		while(matcher.find()){
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
