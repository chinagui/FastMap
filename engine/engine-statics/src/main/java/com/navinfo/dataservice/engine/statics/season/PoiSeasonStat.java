package com.navinfo.dataservice.engine.statics.season;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

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

public class PoiSeasonStat implements Runnable {
	private Connection conn;
	private Logger log;
	private CountDownLatch latch;
	private String db_name;
	private String col_name;
	private String stat_date;

	public PoiSeasonStat(CountDownLatch cdl, int db_id, String db_name, String col_name, String stat_date) {
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

	public List<Document> getPois() throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();

			String sql = "select ip.geometry from ix_poi ip";
			return run.query(conn, sql, new ResultSetHandler<List<Document>>() {

				@Override
				public List<Document> handle(ResultSet rs) throws SQLException {
					Map<String, Integer> map = new HashMap<String, Integer>();

					while (rs.next()) {
						try {
							STRUCT struct = (STRUCT) rs.getObject("geometry");
							Geometry geo = GeoTranslator.struct2Jts(struct);

							String grid_id = CompGridUtil.point2Grids(geo.getCoordinate().x, geo.getCoordinate().y)[0];
							if (map.containsKey(grid_id)) {
								map.put(grid_id, map.get(grid_id) + 1);
							} else {
								map.put(grid_id, 1);
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
					List<Document> json_list = new ArrayList<Document>();
					for (Entry<String, Integer> entry : map.entrySet()) {
						Document json = new Document();
						// ------------------------------
						json.put("grid_id", entry.getKey());
						json.put("total", entry.getValue());
						json.put("stat_date", stat_date);
						json_list.add(json);
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

	public void run() {
		log.info("-- begin do sub_task");
		try {
			log.info("-- begin do sub_task" + conn);
			new MongoDao(db_name).insertMany(col_name, getPois());

		} catch (Exception e) {
			e.printStackTrace();
		}
		latch.countDown();

	}

}
