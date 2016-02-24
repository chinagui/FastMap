package com.navinfo.dataservice.FosEngine.tips.bridge;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.FosEngine.tips.TipsImportUtils;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.solr.core.SConnection;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class BridgeTipsBuilder {

	private static final WKT wkt = new WKT();
	
	private static String sql = "with tmp1 as  "
			+ "(select link_pid,    decode(direct, 1, s_node_pid, 2, s_node_pid, e_node_pid) "
			+ "s_node_pid,    decode(direct, 1, e_node_pid, 2, e_node_pid, s_node_pid) e_node_pid    "
			+ " from rd_link a    where "
			+ "a.link_pid in          (select link_pid from rd_link_form where form_of_way = 30)),"
			+ " tmp2 as  (select *     from tmp1    where s_node_pid not in (select                  "
			+ "            e_node_pid                               from tmp1)), tmp3 as (              "
			+ "                 select link_pid,s_node_pid,e_node_pid, connect_by_root(link_pid) root_link_pid, "
			+ "connect_by_isleaf isleaf, level lvl   from tmp1 "
			+ "connect by nocycle s_node_pid = prior e_node_pid "
			+ " start with link_pid in (select link_pid from tmp2))  "
			+ "select   a.*,b.geometry link_geom,b.length link_len,c.geometry s_point_geom,d.geometry e_point_geom,e.name  "
			+ " from tmp3 a,rd_link b,rd_node c,rd_node d,"
			+ "(  select e.link_pid,f.name from  "
			+ " rd_link_name e,rd_name f where  e.seq_num = 1 and e.name_class = 1 and "
			+ "  e.name_groupid = f.name_groupid  and f.LANG_CODE in ('CHI','CHT')  ) e  "
			+ "where a.link_pid = b.link_pid  and a.s_node_pid = c.node_pid "
			+ " and a.e_node_pid = d.node_pid  and a.link_pid = e.link_pid(+) "
			+ "order by root_link_pid,lvl";

	private static String type = "1510";

	/**
	 * 导入入口程序块
	 * 
	 * @param fmgdbConn
	 * @param htab
	 */
	public static void importTips(java.sql.Connection fmgdbConn, Table htab,String solrUrl)
			throws Exception {

		SConnection solrConn = new SConnection(solrUrl,5000);
		
		Statement stmt = fmgdbConn.createStatement();

		ResultSet resultSet = stmt.executeQuery(sql);

		resultSet.setFetchSize(5000);

		List<Put> puts = new ArrayList<Put>();

		int num = 0;

		String uniqId = null;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String date = sdf.format(new Date());

		while (resultSet.next()) {
			num++;

			STRUCT struct = (STRUCT) resultSet.getObject("s_point_geom");

			JGeometry geom = JGeometry.load(struct);

			String sPointGeom = new String(wkt.fromJGeometry(geom));

			int isleaf = resultSet.getInt("isleaf");

			if (isleaf == 1) {
				struct = (STRUCT) resultSet.getObject("e_point_geom");

				geom = JGeometry.load(struct);

				String ePointGeom = new String(wkt.fromJGeometry(geom));

				uniqId = resultSet.getString("link_pid");

				JSONObject geometry = new JSONObject();

//				geometry.put("g_location", Geojson.wkt2Geojson(sPointGeom));
				
				

				geometry.put("g_guide", geometry.getJSONObject("g_location"));

				JSONObject deep = new JSONObject();

				String name = resultSet.getString("name");

				if (name != null
						&& (name.endsWith("桥") || name.endsWith("交汇处"))) {
					deep.put("name", name);
				} else {
					deep.put("name", JSONNull.getInstance());
				}

				deep.put("gSLoc", Geojson.wkt2Geojson(sPointGeom));

				deep.put("gELoc", Geojson.wkt2Geojson(ePointGeom));

				struct = (STRUCT) resultSet.getObject("link_geom");

				geom = JGeometry.load(struct);

				String linkWkt = new String(wkt.fromJGeometry(geom));
				
				geometry.put("g_location", Geojson.wkt2Geojson(linkWkt));

//				deep.put("geo", Geojson.wkt2Geojson(linkWkt));

				JSONArray fArray = new JSONArray();

				JSONObject fJson = new JSONObject();

				fJson.put("id", uniqId);

				fJson.put("type", 1);
				
				fJson.put("flag", "1|2");

//				fJson.put("sOff", 0.0);
//
//				fJson.put("eOff", 1.0);

				fArray.add(fJson);

				deep.put("f_array", fArray);

				String rowkey = TipsImportUtils.generateRowkey(uniqId, type);

				String source = TipsImportUtils.generateSource(type);

				String track = TipsImportUtils.generateTrack(date);

				Put put = new Put(rowkey.getBytes());

				put.addColumn("data".getBytes(), "source".getBytes(),
						source.getBytes());

				put.addColumn("data".getBytes(), "track".getBytes(),
						track.getBytes());

				put.addColumn("data".getBytes(), "geometry".getBytes(),
						geometry.toString().getBytes());

				put.addColumn("data".getBytes(), "deep".getBytes(), deep
						.toString().getBytes());

				puts.add(put);
				
				JSONObject solrIndexJson = assembleSolrIndex(rowkey, geometry, 0, date, type, deep.toString());
				
				solrConn.addTips(solrIndexJson);

			} else {

				List<Integer> listLinkPid = new ArrayList<Integer>();

				List<String> listLinkWkt = new ArrayList<String>();

				List<String> listName = new ArrayList<String>();

				List<Double> listLinkLength = new ArrayList<Double>();
				
				listLinkPid.add(resultSet.getInt("link_pid"));
				
				struct = (STRUCT) resultSet.getObject("link_geom");

				geom = JGeometry.load(struct);

				String linkWkt = new String(wkt.fromJGeometry(geom));
				
				listLinkWkt.add(linkWkt);
				
				String name = resultSet.getString("name");
				
				listName.add(name);
				
				double linkLen = resultSet.getDouble("link_len");

				listLinkLength.add(linkLen);
				
				while(resultSet.next()){
					isleaf = resultSet.getInt("isleaf");

					listLinkPid.add(resultSet.getInt("link_pid"));
					
					struct = (STRUCT) resultSet.getObject("link_geom");

					geom = JGeometry.load(struct);

					linkWkt = new String(wkt.fromJGeometry(geom));
					
					listLinkWkt.add(linkWkt);
					
					name = resultSet.getString("name");
					
					listName.add(name);
					
					linkLen = resultSet.getDouble("link_len");

					listLinkLength.add(linkLen);
					
					if (isleaf == 1) {
						
						struct = (STRUCT) resultSet.getObject("e_point_geom");

						geom = JGeometry.load(struct);

						String ePointGeom = new String(wkt.fromJGeometry(geom));
						
						JSONObject geometry = new JSONObject();

//						geometry.put("g_location", Geojson.wkt2Geojson(sPointGeom));

						geometry.put("g_guide", geometry.getJSONObject("g_location"));
						
						JSONObject deep = new JSONObject();
						
						boolean isNameNull = false;
						
						for(String bridgeName : listName){
							if (bridgeName == null){
								isNameNull = true;
								break;
							}else{
								if (!bridgeName.endsWith("桥") && !bridgeName.equals("交汇处")){
									isNameNull = true;
									break;
								}
							}
						}
						
						if (!isNameNull){
							String tmpName = listName.get(0);
							
							for(int i=1;i<listName.size();i++){
								if (!tmpName.equals(listName.get(i))){
									isNameNull = true;
									break;
								}
							}
						}
						
						if (isNameNull){
							deep.put("name", JSONNull.getInstance());
						}else{
							deep.put("name", listName.get(0));
						}

						deep.put("gSLoc", Geojson.wkt2Geojson(sPointGeom));
						
						deep.put("gELoc", Geojson.wkt2Geojson(ePointGeom));
						
//						deep.put("geo", connectLinks(listLinkWkt));
						
						geometry.put("g_location",connectLinks(listLinkWkt));
						
						double sumLen = 0;
						
						for(double len : listLinkLength){
							sumLen += len;
						}
						
						if (sumLen == 0){
							sumLen = 1;
						}
						
						JSONArray fArray = new JSONArray();
						
//						double curLen = 0.0;
						
						for(int i=0;i<listLinkPid.size();i++){
							JSONObject fJson = new JSONObject();
							
							fJson.put("id", String.valueOf(listLinkPid.get(i)));
							
							fJson.put("type", 1);
							
//							fJson.put("sOff", curLen / sumLen);
//							
//							curLen += listLinkLength.get(i);
//							
//							fJson.put("eOff", curLen / sumLen);
							
							if (i ==0){
								fJson.put("flag", "1");
							}else if (i == listLinkPid.size()-1){
								fJson.put("flag", "2");
							}else{
								fJson.put("flag", "0");
							}
							
							fArray.add(fJson);
						}
						
						deep.put("f_array", fArray);
						
						int minLinkPid = listLinkPid.get(0);
						
						for(int i=1;i<listLinkPid.size();i++){
							if (minLinkPid > listLinkPid.get(i)){
								minLinkPid = listLinkPid.get(i);
							}
						}
						
						uniqId = String.valueOf(minLinkPid);
						
						String rowkey = TipsImportUtils.generateRowkey(uniqId, type);

						String source = TipsImportUtils.generateSource(type);

						String track = TipsImportUtils.generateTrack(date);


						Put put = new Put(rowkey.getBytes());

						put.addColumn("data".getBytes(), "source".getBytes(),
								source.getBytes());

						put.addColumn("data".getBytes(), "track".getBytes(),
								track.getBytes());

						put.addColumn("data".getBytes(), "geometry".getBytes(),
								geometry.toString().getBytes());

						put.addColumn("data".getBytes(), "deep".getBytes(), deep.toString().getBytes());

						puts.add(put);
						
						JSONObject solrIndexJson = assembleSolrIndex(rowkey, geometry, 0, date, type, deep.toString());
						
						solrConn.addTips(solrIndexJson);
						
						break;
						
						
					}
				}
				
				

			}

			

			if (num % 5000 == 0) {
				htab.put(puts);

				puts.clear();

			}
		}

		htab.put(puts);
		
		solrConn.persistentData();
		
		solrConn.closeConnection();
		

	}

	// 组装solr索引
	private static JSONObject assembleSolrIndex(String rowkey, JSONObject geom,
			int stage, String date, String type, String deep) throws Exception {
		JSONObject json = new JSONObject();

		json.put("id", rowkey);

		json.put("stage", stage);

		json.put("date", date);

		json.put("t_lifecycle", 0);

		json.put("t_command", 0);

		json.put("handler", 0);

		json.put("s_sourceType", type);

		json.put("s_sourceCode", 11);

		JSONObject geojson = geom.getJSONObject("g_location");

		json.put("g_location", geojson);

		json.put("g_guide", geom.getJSONObject("g_guide"));

		json.put("wkt",
				GeoTranslator.jts2Wkt(GeoTranslator.geojson2Jts(geojson)));
		
		json.put("deep", deep);

		return json;
	}
	
	private static JSONObject connectLinks(List<String> listLink) throws ParseException{
		JSONObject json = new JSONObject();
		
		json.put("type", "LineString");
		
		Geometry geom1= new WKTReader().read(listLink.get(0));
		
		for(int i=1;i<listLink.size();i++){
			Geometry geom = new WKTReader().read(listLink.get(i));
			
			geom1 = geom1.union(geom);
		}
		
		Coordinate[] cs = geom1.getCoordinates();
		
		List<double[]> ps = new ArrayList<double[]>();
		
		for(Coordinate c : cs){
			double[] p = new double[2];
			
			p[0] = c.x;
			
			p[1] = c.y;
			
			ps.add(p);
		}
		
		json.put("coordinates", ps);
		
		return json;
	}

}
