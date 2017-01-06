package com.navinfo.dataservice.dao.fcc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.Geojson;

public class SolrController {

	private int fetchNum = Integer.MAX_VALUE;

	private HttpSolrClient client;

	public SolrController() {
		client = SolrConnector.getInstance().getClient();
	}

	public void addTips(JSONObject json) throws JSONException,
			SolrServerException, IOException {

		SolrInputDocument doc = new SolrInputDocument();

		doc.addField("id", json.getString("id"));

		doc.addField("wkt", json.getString("wkt"));

		doc.addField("stage", json.getInt("stage"));

		doc.addField("t_operateDate", json.getString("t_operateDate"));

		doc.addField("t_date", json.getString("t_date"));

		doc.addField("t_lifecycle", json.getInt("t_lifecycle"));

		doc.addField("t_command", json.getInt("t_command"));

		doc.addField("t_cStatus", json.getInt("t_cStatus"));
		
		doc.addField("t_dStatus", json.getInt("t_dStatus"));
		
        //doc.addField("t_inStatus", json.getInt("t_inStatus"));
		
		doc.addField("t_inMeth", json.getInt("t_inMeth"));
		
		doc.addField("t_pStatus", json.getInt("t_pStatus"));
		
		doc.addField("t_dInProc", json.getInt("t_dInProc"));
		
		doc.addField("t_mInProc", json.getInt("t_mInProc"));
		
		doc.addField("handler", json.getInt("handler"));
		
		doc.addField("t_mStatus", json.getInt("t_mStatus"));

		doc.addField("s_sourceCode", json.getInt("s_sourceCode"));

		doc.addField("s_sourceType", json.getString("s_sourceType"));

		doc.addField("g_location", json.getString("g_location"));

		doc.addField("g_guide", json.getString("g_guide"));

		doc.addField("deep", json.getString("deep"));
		
		doc.addField("feedback", json.getString("feedback"));
		
		doc.addField("s_reliability", json.getInt("s_reliability"));

		client.add(doc);

		client.commit();
		
	}

	public boolean checkTipsMobile(String wkt, String date, int[] notExpSourceType)
			throws SolrServerException, IOException {

	   String param = "wkt:\"intersects(" + wkt + ")\"";

        if (date != null && !date.equals("")) {
            param += " AND t_date:[" + date + " TO *]";
        }
        
    	//过滤的类型
		//  1. 示例：TITLE:(* NOT "上网费用高" NOT "宽带收费不合理" )  
		if(notExpSourceType!=null&&notExpSourceType.length!=0){
			String typeStr="( *";
			for (int type : notExpSourceType) {
				typeStr+=" NOT \""+type+"\"";
			}
			typeStr+=")";
			
			param += " AND s_sourceType:"+typeStr;
			
			//System.out.println(param);
			
		}

		SolrQuery query = new SolrQuery();

		query.set("q", param);

		query.addField("id");

		query.set("start", 0);

		query.set("rows", 1);

		QueryResponse response = client.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum > 0) {
			return true;
		} else {
			return false;
		}
	}

	public List<String> queryTipsMobile(String wkt, String date, int[] notExpSourceType)
			throws SolrServerException, IOException {
		List<String> rowkeys = new ArrayList<String>();

		String param = "wkt:\"intersects(" + wkt + ")\"";

		if (date != null && !date.equals("")) {
			param += " AND t_date:[" + date + " TO *]";
		}
		
		//过滤的类型
		//  1. 示例：TITLE:(* NOT "上网费用高" NOT "宽带收费不合理" )  
		if(notExpSourceType!=null&&notExpSourceType.length!=0){
			String typeStr="( *";
			for (int type : notExpSourceType) {
				typeStr+=" NOT \""+type+"\"";
			}
			typeStr+=")";
			
			param += " AND s_sourceType:"+typeStr;
			
		    System.out.println(param);
			
		}

		SolrQuery query = new SolrQuery();

		query.set("q", param);

		query.addField("id");

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = client.query(query);

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

		QueryResponse response = client.query(query);

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

	/**
	 * @Description:按照wkt  ,tip类型 \stage查询tips
	 * @param wkt
	 * @param type
	 * @param stages
	 * @param isPre 是否是预处理平台，默认不是
	 * @return
	 * @throws SolrServerException
	 * @throws IOException
	 * @author: y
	 * @time:2017-1-5 下午3:25:50
	 */
	public List<JSONObject> queryTipsWeb(String wkt, int type, JSONArray stages,boolean isPre)
			throws SolrServerException, IOException {
		List<JSONObject> snapshots = new ArrayList<JSONObject>();

		StringBuilder builder = new StringBuilder();

		builder.append("wkt:\"intersects(");

		builder.append(wkt);

		builder.append(")\" AND s_sourceType:");

		builder.append(type);

		if (stages.size() > 0) {

			builder.append(" AND stage:(");

			for (int i = 0; i < stages.size(); i++) {
				int stage = stages.getInt(i);

				if (i > 0) {
					builder.append(" ");
				}
				builder.append(stage);
			}

			builder.append(")");
		}
		
		//不是预处理，则需要过滤预处理没提交的tips,t_pStatus=0是没有提交的
		
		if(!isPre){
			builder.append("AND -(t_pStatus:0 AND s_sourceType:8001)");
		}

		SolrQuery query = new SolrQuery();

		query.set("q", builder.toString());

		query.set("sort", "t_operateDate desc");

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = client.query(query);

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
	
	
	
	/**
	 * @Description:查询满足条件的tips
	 * @param wkt
	 * @param stage
	 * @param t_dStatus
	 * @return
	 * @throws SolrServerException
	 * @throws IOException
	 * @author: y
	 * @time:2016-10-25 下午3:17:22
	 */
	public List<JSONObject> queryTips(String wkt, int stage, int t_dStatus)
			throws SolrServerException, IOException {
		List<JSONObject> snapshots = new ArrayList<JSONObject>();

		StringBuilder builder = new StringBuilder();

		builder.append("wkt:\"intersects(");

		builder.append(wkt);

		builder.append(")\" ");

		builder.append(" AND stage:"+stage);
		
		builder.append(" AND t_dStatus:"+t_dStatus);


		SolrQuery query = new SolrQuery();

		query.set("q", builder.toString());

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = client.query(query);

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

		if(stages!=null){
			for (int i = 0; i < stages.size(); i++) {
				int stage = stages.getInt(i);

				fq += "stage:" + stage;

				if (i != stages.size() - 1) {
					fq += " OR ";
				}
			}
		}

		SolrQuery query = new SolrQuery();

		query.set("q", param);

		query.set("fq", fq);

		query.set("start", 0);

		query.set("rows", fetchNum);

		query.addField("s_sourceType");

		QueryResponse response = client.query(query);

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

		QueryResponse response = client.query(query);

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

	public List<JSONObject> queryTipsWebType(String wkt, JSONArray types,JSONArray stages,boolean filterDelete)
			throws SolrServerException, IOException {
		//默认不是预处理的tips
		return queryTipsWebType(wkt, types, stages, filterDelete,false);
	}

	public JSONObject getById(String id) throws Exception {

		String param = "id:" + id;

		SolrQuery query = new SolrQuery();

		query.set("q", param);

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = client.query(query);

		SolrDocumentList sdList = response.getResults();

		long totalNum = sdList.getNumFound();

		if (totalNum == 0) {
			return null;
		}

		SolrDocument doc = sdList.get(0);

		JSONObject snapshot = JSONObject.fromObject(doc);

		return snapshot;
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

	/**
	 * @Description:TOOD
	 * @param rowkey
	 * @author: y
	 * @throws IOException 
	 * @throws SolrServerException 
	 * @time:2016-11-16 下午5:26:52
	 */
	public void deleteByRowkey(String rowkey) throws SolrServerException, IOException {
		
		client.deleteById(rowkey);
		
		client.commit();
		
	}

	/**
	 * @Description:渲染接口
	 * @param wkt
	 * @param types
	 * @param stages
	 * @param b
	 * @param isPre
	 * @return
	 * @author: y
	 * @throws IOException 
	 * @throws SolrServerException 
	 * @time:2017-1-5 下午2:03:57
	 */
	public List<JSONObject> queryTipsWebType(String wkt, JSONArray types,
			JSONArray stages, boolean filterDelete, boolean isPre) throws SolrServerException, IOException {
		List<JSONObject> snapshots = new ArrayList<JSONObject>();

		StringBuilder builder = new StringBuilder();
		
		//builder.append("wkt:\"intersects(" + wkt + ")\"  AND stage:(1 2 3)");
		
		builder.append("wkt:\"intersects(" + wkt + ")\" " );

		if(filterDelete) {
            //过滤删除的数据
			builder.append(" AND -t_lifecycle:1 " );
		}
		
		if (stages.size() > 0) {

			builder.append(" AND stage:(");

			for (int i = 0; i < stages.size(); i++) {
				int stage = stages.getInt(i);

				if (i > 0) {
					builder.append(" ");
				}
				builder.append(stage);
			}

			builder.append(")");
		}

		if (types.size() > 0) {

			builder.append(" AND s_sourceType:(");

			for (int i = 0; i < types.size(); i++) {
				String type = types.getString(i);

				if (i > 0) {
					builder.append(" ");
				}
				builder.append(type);
			}

			builder.append(")");
		}
		//不是预处理，则需要过滤预处理没提交的tips,t_pStatus=0是没有提交的
		
		if(!isPre){
			builder.append("AND -(t_pStatus:0 AND s_sourceType:8001)");
		}

		SolrQuery query = new SolrQuery();

		query.set("q", builder.toString());

		query.set("start", 0);

		query.set("rows", fetchNum);

		QueryResponse response = client.query(query);

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

}
