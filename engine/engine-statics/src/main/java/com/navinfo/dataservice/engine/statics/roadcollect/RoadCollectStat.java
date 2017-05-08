package com.navinfo.dataservice.engine.statics.roadcollect;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.StatInit;
import com.navinfo.dataservice.engine.statics.tools.StatUtil;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.JtsGeometryConvertor;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class RoadCollectStat implements Runnable {
	private Connection conn;
	private Logger log;
	private CountDownLatch latch;
	private String db_name;
	private String col_name;
	private String stat_date;
	private String stat_time;
	private String col_name_tips_grid = "track_length_grid_stat";

	public RoadCollectStat(CountDownLatch cdl, int db_id, String db_name, String col_name, String stat_date, String stat_time) {
		this.latch = cdl;
		this.db_name = db_name;
		this.col_name = col_name;
		this.stat_date = stat_date;
		this.stat_time = stat_time;
		this.log = LogManager.getLogger(String.valueOf(db_id));

		try {
			this.conn = DBConnector.getInstance().getConnectionById(db_id);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public Map<String, Double> getRdlink() throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();
			// String sql =
			// "select rl.mesh_id,rl.multi_digitized, rl.geometry from rd_link rl where link_pid=261067";
			String sql = "select rl.mesh_id,rl.multi_digitized, rl.geometry from rd_link rl";

			return run.query(conn, sql, new ResultSetHandler<Map<String, Double>>() {

				@Override
				public Map<String, Double> handle(ResultSet rs) throws SQLException {
					// map 存放 rd_link表中，以grid_id为key，以多条grid内link长度之和为value的集合。
					Map<String, Double> map = new HashMap<String, Double>();

					while (rs.next()) {
						try {
							STRUCT struct = (STRUCT) rs.getObject("geometry");
							Geometry linkGeo = GeoTranslator.struct2Jts(struct);
							int mesh_id = rs.getInt("mesh_id");
							// 上下线分离：link*2. 20170321 by zxy上下线分离道路取消*2，于桐，李泽确认结果
							int md = rs.getInt("multi_digitized");
							// 根据 mesh_id 获得16个grid
							Set<String> grids = CompGridUtil.mesh2Grid(String.valueOf(mesh_id));
							// 不知道link跨越多少个grid，所以循环16个grid计算每个中的长度
							for (String grid_id : grids) {
								// 获取grid对应的矩形Polygon结构
								double[] loc = CompGridUtil.grid2Rect(grid_id);
								Polygon gridPolygon = JtsGeometryConvertor.convert(loc);

								// 根据 rd_link 获得的link计算在grid中相交的长度。
								double gridLineLength = GeometryUtils.getLinkLength(linkGeo.intersection(gridPolygon));

								if (map.containsKey(grid_id)) {
									map.put(grid_id, StatUtil.formatDouble(map.get(grid_id) + gridLineLength * md));
								} else {
									map.put(grid_id, StatUtil.formatDouble(gridLineLength * md));
								}
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
					return map;
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

	public List<Document> doStatPoi() throws Exception {
		List<Document> backList = new ArrayList<Document>();
		Map<String, Double> allLinks = getRdlink();
		
		Map<String, Double> mapTips = StatInit.getTrackTipsStat(db_name, col_name_tips_grid, "grid_id", stat_date);
		
		for (Entry<String, Double> entry : allLinks.entrySet()) {
			String grid_id = entry.getKey();
			double total = entry.getValue();
			double finish = (mapTips.get(grid_id) == null ? 0 : mapTips.get(grid_id));

			Document json = new Document();
			// ------------------------------
			json.put("grid_id", grid_id);
			json.put("stat_date", stat_date);
			json.put("stat_time", stat_time);
			// ------------------------------
			Document road = new Document();
			road.put("total", total);
			road.put("finish", finish);
			if (finish == 0 || total == 0) {
				road.put("percent", 0);
			} else {
				road.put("percent", StatUtil.formatDouble((double) finish / total * 100));
			}

			// ------------------------------
			json.put("road", road);
			backList.add(json);
		}
		return backList;
	}

	public void run() {
		//log.info("-- begin do sub_task");
		try {
			log.info("-- begin do sub_task:"+col_name+",conn:" + conn);
			List<Document> pois = doStatPoi();
			if(pois!=null&&pois.size()>0){
				new MongoDao(db_name).insertMany(col_name, pois);}
			log.info("-- end do sub_task:"+col_name+",conn:" + conn);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		latch.countDown();

	}

}
