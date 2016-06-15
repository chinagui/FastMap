package com.navinfo.dataservice.engine.statics.poidaily;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.vividsolutions.jts.geom.Geometry;

public class PoiDailyStat implements Runnable {
	private Connection conn;
	private Logger log;
	private CountDownLatch latch;
	private String db_name;
	private String col_name;
	private String stat_date;

	public PoiDailyStat(CountDownLatch cdl, int db_id, String db_name, String col_name, String stat_date) {
		this.latch = cdl;
		this.db_name = db_name;
		this.col_name = col_name;
		this.stat_date = stat_date;

		this.log = LogManager.getLogger(String.valueOf(db_id));
		try {
			this.conn = DBConnector.getInstance().getConnectionById(db_id);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public JSONArray getPois() throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();

			String sql = "select ip.row_id, pes.status, ip.geometry from ix_poi ip, poi_edit_status pes where ip.row_id = pes.row_id and rownum<100";
			return run.query(conn, sql, new ResultSetHandler<JSONArray>() {

				@Override
				public JSONArray handle(ResultSet rs) throws SQLException {

					JSONArray json_list = new JSONArray();
					while (rs.next()) {
						try {
							JSONObject json = new JSONObject();
							STRUCT struct = (STRUCT) rs.getObject("geometry");
							Geometry geo = GeoTranslator.struct2Jts(struct);

							String grid_id = CompGridUtil.point2Grids(geo.getCoordinate().x, geo.getCoordinate().y)[0];
							json.put("grid_id", grid_id);
							json.put("status", String.valueOf(rs.getInt("status")));
							json_list.add(json);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
					return json_list;
				}
			}

			);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public Document getJsonTemplet(String grid_id) {
		Document doc = new Document();
		doc.append("grid_id", grid_id);
		doc.append("stat_date", stat_date);

		Document poi = new Document();
		poi.append("total", 1);
		poi.append("finish", 0);
		poi.append("percent", 0);
		doc.append("poi", poi);

		return doc;
	}

	public List<Document> doStatPoi(JSONArray ja) {
		Document doc =new Document();
		for (int i = 0; i < ja.size(); i++) {
			JSONObject json = ja.getJSONObject(i);
			String grid_id =json.getString("grid_id");
			String status =json.getString("status");
			
			
			if (doc.containsKey(grid_id)){
				Document d= (Document)((Document) doc.get(grid_id)).get("poi");
				d.put("total", d.getInteger("total")+1);
				if (status.equals("3")){
					d.put("finish", d.getInteger("finish")+1);
				}
				
			}else{
				doc.append(grid_id, getJsonTemplet(grid_id));
			}
			
			
		}
		
		
		
		List<Document> backList=new ArrayList<Document>();

		 for (Iterator iter = doc.keySet().iterator(); iter.hasNext();) {
			  String key = (String)iter.next();
			  Document dd=(Document)doc.get(key);
			  
			  Document poi= (Document) dd.get("poi");
			  
			  poi.put("percent", poi.getInteger("finish")/poi.getInteger("total"));
			  backList.add(dd);
			 }
					
	
		return backList;
	}

	public void run() {
		log.info("-- begin do sub_task");
		try {
			log.info("-- begin do sub_task" + conn);
			JSONArray ja = getPois();
			new MongoDao(db_name).insertMany(col_name, doStatPoi(ja));

			System.out.println("--------------" + ja.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		latch.countDown();

	}

}
