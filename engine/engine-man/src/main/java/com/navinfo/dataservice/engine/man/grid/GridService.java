package com.navinfo.dataservice.engine.man.grid;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.navinfo.dataservice.api.man.model.Grid;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Polygon;

import net.sf.json.JSONObject;

public class GridService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	private GridService() {
	}

	private static class SingletonHolder {
		private static final GridService INSTANCE = new GridService();
	}

	public static GridService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public Set<Integer> queryGrid(int limit) throws SQLException {
		String sql = "select grid_id from grid g where rownum<?";
		QueryRunner queryRunner = new QueryRunner();
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			ResultSetHandler<Set<Integer>> rsh = new ResultSetHandler<Set<Integer>>() {

				@Override
				public Set<Integer> handle(ResultSet rs) throws SQLException {
					if (rs != null) {
						Set<Integer> grids = new HashSet<Integer>();
						while (rs.next()) {
							int gridId = rs.getInt("grid_id");
							grids.add(gridId);
						}
						return grids;
					}
					return null;
				}
			};
			return queryRunner.query(conn, sql, limit, rsh);

		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	public List<Grid> list() throws Exception {
		String sql = "SELECT GRID_ID,REGION_ID,CITY_ID,BLOCK_ID FROM GRID";
		QueryRunner run = new QueryRunner();
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			List<Grid> results = run.query(conn, sql, new GridResultSetHandler());
			return results;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	/**
	 * @param gridList
	 *            <br/>
	 *            <b>注意：如果参数gridList太长(不能超过1000)，会导致oracle sql太长而出现异常；</b>
	 * @return 根据给定的gridlist，查询获取regioin和grid的映射；key:RegionId；value：grid列表<br/>
	 * @throws Exception
	 * 
	 */
	public Map queryRegionGridMapping(List<Integer> gridList) throws Exception {
		String sql = "select grid_id,region_id from grid g where 1=1 ";
		QueryRunner queryRunner = new QueryRunner();
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			ResultSetHandler<MultiValueMap> rsh = new ResultSetHandler<MultiValueMap>() {

				@Override
				public MultiValueMap handle(ResultSet rs) throws SQLException {
					if (rs != null) {
						MultiValueMap mvMap = new MultiValueMap();
						while (rs.next()) {
							int gridId = rs.getInt("grid_id");
							int regionId = rs.getInt("region_id");
							mvMap.put(regionId, gridId);
						}
						return mvMap;
					}
					return null;
				}
			};
			String InClause = buildInClause("g.grid_id", gridList);
			sql = sql + InClause;
			return queryRunner.query(conn, sql, rsh);

		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	private String buildInClause(String columName, List inValuesList) {
		int size = inValuesList.size();
		if (size == 0)
			return null;
		return " and " + columName + " in (" + org.apache.commons.lang.StringUtils.join(inValuesList, ",") + ")";
	}

	/**
	 * @param taskList
	 *            subTaskId的列表 <b>注意：如果参数taskList太长（不能超过1000个），会导致oracle
	 *            sql太长而出现异常；</b>
	 * @return MultiValueMap key是regionId，value是大区中满足条件的grid的列表
	 * @throws Exception
	 */
	public Map queryRegionGridMappingOfSubtasks(List<Integer> taskList) throws Exception {
		String sql = "select distinct g.grid_id,g.region_id  from grid g,subtask t ,subtask_grid_mapping m "
				+ " where t.subtask_id=m.subtask_id and m.grid_id=g.grid_id ";
		QueryRunner queryRunner = new QueryRunner();
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			ResultSetHandler<MultiValueMap> rsh = new ResultSetHandler<MultiValueMap>() {

				@Override
				public MultiValueMap handle(ResultSet rs) throws SQLException {
					if (rs != null) {
						MultiValueMap mvMap = new MultiValueMap();
						while (rs.next()) {
							int gridId = rs.getInt("grid_id");
							int regionId = rs.getInt("region_id");
							mvMap.put(regionId, gridId);
						}
						return mvMap;
					}
					return null;
				}
			};
			String InClause = buildInClause(" t.subtask_id ", taskList);
			sql = sql + InClause;
			if (StringUtils.isEmpty(InClause)) {
				return queryRunner.query(conn, sql, rsh);
			} else {
				return queryRunner.query(conn, sql, rsh);
			}

		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	class GridResultSetHandler implements ResultSetHandler<List<Grid>> {

		@Override
		public List<Grid> handle(ResultSet rs) throws SQLException {
			List<Grid> results = new ArrayList<Grid>();
			if (rs.next()) {
				Grid g = new Grid();
				g.setGridId(rs.getInt("GRID_ID"));
				g.setRegionId(rs.getInt("REGION_ID"));
				g.setCityId(rs.getInt("CITY_ID"));
				g.setBlockId(rs.getInt("BLOCK_ID"));
				results.add(g);
			}
			return results;
		}

	}

	public List<HashMap> quryListByAlloc(JSONObject json) throws ServiceException {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getManConnection();
			// 根据输入的几何wkt，计算几何包含的gird，
			Polygon polygon = (Polygon) GeometryUtils.getPolygonByWKT(json.getString("wkt"));
			Set<?> grids = (Set<?>) CompGeometryUtil.geo2GridsWithoutBreak(polygon);

			String waitAssignSql = "select g.grid_id from grid g,block b where g.block_id=b.block_id and b.plan_status=1 and not exists "
					+ "(select s.subtask_id from subtask_grid_mapping s where s.grid_id=g.grid_id)";
			String alreadyAssignSql = "select distinct g.grid_id from subtask_grid_mapping g,subtask s where g.subtask_id=s.subtask_id "
					+ "and s.stage=" + json.getInt("stage");

			List<String> gridList = new ArrayList<String>();
			gridList.addAll((Collection<? extends String>) grids);
			String InClause = buildInClause("g.grid_id", gridList);

			//获取待分配的grid
			List<HashMap> waitAssignGrids=new ArrayList<HashMap>();
			waitAssignGrids=GridOperation.queryGirdBySql(conn, waitAssignSql+InClause);
			
			//获取已分配的grid
			List<HashMap> alreadyAssignGrids=new ArrayList<HashMap>();
			alreadyAssignGrids=GridOperation.queryGirdBySql(conn, alreadyAssignSql+InClause,json.getInt("stage"));
			
			
			List<HashMap> allGrids=new ArrayList<HashMap>();
			allGrids.addAll(waitAssignGrids);
			allGrids.addAll(alreadyAssignGrids);

			return allGrids;
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询grid失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
