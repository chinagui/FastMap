package com.navinfo.dataservice.engine.man.grid;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.man.model.Grid;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.api.statics.model.GridStatInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.subtask.SubtaskOperation;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Polygon;

import net.sf.json.JSONArray;
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

//	public Set<Integer> queryGrid(int limit) throws SQLException {
//		String sql = "select grid_id from grid g where rownum<?";
//		QueryRunner queryRunner = new QueryRunner();
//		Connection conn = null;
//		try {
//			conn = DBConnector.getInstance().getManConnection();
//			ResultSetHandler<Set<Integer>> rsh = new ResultSetHandler<Set<Integer>>() {
//
//				@Override
//				public Set<Integer> handle(ResultSet rs) throws SQLException {
//					if (rs != null) {
//						Set<Integer> grids = new HashSet<Integer>();
//						while (rs.next()) {
//							int gridId = rs.getInt("grid_id");
//							grids.add(gridId);
//						}
//						return grids;
//					}
//					return null;
//				}
//			};
//			return queryRunner.query(conn, sql, limit, rsh);
//
//		} finally {
//			DbUtils.closeQuietly(conn);
//		}
//	}
//	
//	/*
//	 * 根据taskId获取gridId list
//	 */
//	public List<Integer> getGridListByTaskId(Integer taskId) throws ServiceException {
//		Connection conn = null;
//		try {
//			conn = DBConnector.getInstance().getManConnection();
//			QueryRunner run = new QueryRunner();
//			
//			String selectSql = "select g.grid_id"
//					+ " from task t, grid g"
//					+ " where t.city_id = g.city_id"
//					+ " and t.task_id = " + taskId;;
//
//			ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>() {
//				public List<Integer> handle(ResultSet rs) throws SQLException {
//					List<Integer> gridList = new ArrayList<Integer>();
//					while (rs.next()) {
//						gridList.add(rs.getInt("grid_id"));
//					}
//					return gridList;
//				}
//	
//			};
//
//			return run.query(conn, selectSql,rsHandler);
//			
//		} catch (Exception e) {
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new ServiceException("根据taskId查询grid失败，原因为:" + e.getMessage(), e);
//		} finally {
//			DbUtils.commitAndCloseQuietly(conn);
//		}
//	}

//	public List<Grid> list() throws Exception {
//		String sql = "SELECT GRID_ID,REGION_ID,CITY_ID,BLOCK_ID FROM GRID";
//		QueryRunner run = new QueryRunner();
//		Connection conn = null;
//		try {
//			conn = DBConnector.getInstance().getManConnection();
//			List<Grid> results = run.query(conn, sql, new GridResultSetHandler());
//			return results;
//		} finally {
//			DbUtils.closeQuietly(conn);
//		}
//	}
	

	/**
	 * @param gridList
	 *            <br/>
	 *            <b>注意：如果参数gridList太长(不能超过1000)，会导致oracle sql太长而出现异常；</b>
	 * @return 根据给定的gridlist，查询获取regioin和grid的映射；key:RegionId；value：grid列表<br/>
	 * @throws Exception
	 * 
	 */
	public MultiValueMap queryRegionGridMapping(List<Integer> gridList) throws Exception {
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
//	public Map queryRegionGridMappingOfSubtasks(List<Integer> taskList) throws Exception {
//		String sql = "select distinct g.grid_id,g.region_id  from grid g,subtask t ,subtask_grid_mapping m "
//				+ " where t.subtask_id=m.subtask_id and m.grid_id=g.grid_id ";
//		QueryRunner queryRunner = new QueryRunner();
//		Connection conn = null;
//		try {
//			conn = DBConnector.getInstance().getManConnection();
//			ResultSetHandler<MultiValueMap> rsh = new ResultSetHandler<MultiValueMap>() {
//
//				@Override
//				public MultiValueMap handle(ResultSet rs) throws SQLException {
//					if (rs != null) {
//						MultiValueMap mvMap = new MultiValueMap();
//						while (rs.next()) {
//							int gridId = rs.getInt("grid_id");
//							int regionId = rs.getInt("region_id");
//							mvMap.put(regionId, gridId);
//						}
//						return mvMap;
//					}
//					return null;
//				}
//			};
//			String InClause = buildInClause(" t.subtask_id ", taskList);
//			sql = sql + InClause;
//			if (StringUtils.isEmpty(InClause)) {
//				return queryRunner.query(conn, sql, rsh);
//			} else {
//				return queryRunner.query(conn, sql, rsh);
//			}
//
//		} finally {
//			DbUtils.closeQuietly(conn);
//		}
//	}
//
//	class GridResultSetHandler implements ResultSetHandler<List<Grid>> {
//
//		@Override
//		public List<Grid> handle(ResultSet rs) throws SQLException {
//			List<Grid> results = new ArrayList<Grid>();
//			if (rs.next()) {
//				Grid g = new Grid();
//				g.setGridId(rs.getInt("GRID_ID"));
//				g.setRegionId(rs.getInt("REGION_ID"));
//				g.setCityId(rs.getInt("CITY_ID"));
//				g.setBlockId(rs.getInt("BLOCK_ID"));
//				results.add(g);
//			}
//			return results;
//		}
//
//	}

	public List<HashMap<String, Object>> queryListByAlloc(JSONObject json) throws ServiceException {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getManConnection();
			// 根据输入的几何wkt，计算几何包含的gird，
			Polygon polygon = (Polygon) GeometryUtils.getPolygonByWKT(json.getString("wkt"));
			Set<?> grids = (Set<?>) CompGeometryUtil.geo2GridsWithoutBreak(polygon);
			int stage = json.getInt("stage");
			if(json.containsKey("type")){
				
			}
//			int type = json.getInt("type");

			String waitAssignSql = "select g.grid_id"
					+ " from block_grid_mapping g,block b"
					+ " where g.block_id=b.block_id"
					+ " and b.plan_status=1"
					+ " and not exists "
					+ "(select s.subtask_id"
					+ " from subtask_grid_mapping sgm,subtask s"
					+ " where sgm.grid_id = g.grid_id"
					+ " and sgm.subtask_id = s.subtask_id";
			if(json.containsKey("type")){
				waitAssignSql +=  " and s.stage=" + stage
						+ " and s.type=" + json.getInt("type") + ")";
			}else{
				waitAssignSql +=  " and s.stage=" + stage + ")";
			}
			
			String alreadyAssignSql = "select distinct g.grid_id"
					+ " from subtask_grid_mapping g,subtask s"
					+ " where g.subtask_id=s.subtask_id ";
			if(json.containsKey("type")){
				alreadyAssignSql +=  " and s.stage=" + stage
						+ " and s.type=" + json.getInt("type");
			}else{
				alreadyAssignSql +=  " and s.stage=" + stage;
			}

			List<String> gridList = new ArrayList<String>();
			gridList.addAll((Collection<? extends String>) grids);
			int size=gridList.size();
			//当size超过1000时，才转clob，提高效率
			String InClause = null;
			Clob clobGrids=null;
			if (size>1000){
//				clobGrids=conn.createClob();
				clobGrids=ConnectionUtil.createClob(conn);
				clobGrids.setString(1, StringUtils.join(gridList, ","));
				InClause = " and g.grid_id IN (select to_number(column_value) from table(clob_to_table(?)))";
			}else{
				InClause = buildInClause("g.grid_id", gridList);
			}
			

			//获取待分配的grid
			List<HashMap<String, Object>> waitAssignGrids=new ArrayList<HashMap<String, Object>>();
			//获取已分配的grid
			List<HashMap<String, Object>> alreadyAssignGrids=new ArrayList<HashMap<String, Object>>();
			if (size>1000){
				waitAssignGrids=GridOperation.queryGirdBySql(conn, waitAssignSql+InClause,clobGrids,1);
				alreadyAssignGrids=GridOperation.queryGirdBySql(conn, alreadyAssignSql+InClause,clobGrids,2);
//				alreadyAssignGrids=GridOperation.queryGirdBySql(conn, alreadyAssignSql+InClause,json.getInt("stage"),clobGrids);			
			}else{
				waitAssignGrids=GridOperation.queryGirdBySql(conn, waitAssignSql+InClause,null,1);
				alreadyAssignGrids=GridOperation.queryGirdBySql(conn, alreadyAssignSql+InClause,null,2);
//				alreadyAssignGrids=GridOperation.queryGirdBySql(conn, alreadyAssignSql+InClause,json.getInt("stage"),null);
			}
			
			List<HashMap<String, Object>> allGrids=new ArrayList<HashMap<String, Object>>();
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
	
	
	public List<String> queryListProduce(JSONObject json) throws ServiceException {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getManConnection();
			// 根据输入的几何wkt，计算几何包含的gird，
			Polygon polygon = (Polygon) GeometryUtils.getPolygonByWKT(json.getString("wkt"));
			Set<String> grids = CompGeometryUtil.geo2GridsWithoutBreak(polygon);
			String gridStr = "";
			for(String grid:grids){
				if(!gridStr.isEmpty()){gridStr+=",";}
				gridStr+=grid;
			}
			Clob pidsClob = ConnectionUtil.createClob(conn);
			pidsClob.setString(1, gridStr);
			//仅返回可出品状态的grid。判断原则：1.按区域子任务范围出品（已关闭区域子任务）
			//2.出品条件：
			//(1)当前任务的区域粗编子任务范围中所有grid涉及的其它日编子任务(排除POI类型子任务)全部关闭，该区域才可以出品，
			//不支持无子任务范围出品
			//(2)一级情报已经反馈
			String sql="WITH GRID_LIST AS"
					+ " (SELECT M.GRID_ID"
					+ "    FROM SUBTASK S, BLOCK_GRID_MAPPING M, BLOCK_MAN B"
					+ "   WHERE S.BLOCK_MAN_ID = B.BLOCK_MAN_ID"
					+ "     AND B.BLOCK_ID = M.BLOCK_ID"
					+ "     AND S.STATUS = 1"
					+ "     AND S.TYPE IN (3, 4)"
					+ "     AND S.STAGE = 1"
					+ "  UNION ALL"
					+ "  SELECT MM.GRID_ID"
					+ "    FROM SUBTASK SS, SUBTASK_GRID_MAPPING MM"
					+ "   WHERE SS.SUBTASK_ID = MM.SUBTASK_ID"
					+ "     AND SS.STATUS = 1"
					+ "     AND SS.TYPE IN (3, 4)"
					+ "     AND SS.STAGE = 1"
					+ "  UNION ALL"
					+ "  SELECT M.GRID_ID"
					+ "    FROM INFOR I, SUBTASK_GRID_MAPPING M, BLOCK_MAN B"
					+ "   WHERE I.TASK_ID = B.TASK_ID"
					+ "     AND B.BLOCK_MAN_ID = M.SUBTASK_ID"
					+ "     AND I.FEEDBACK_TYPE = 0)"
					+ "SELECT DISTINCT M.GRID_ID"
					+ "  FROM SUBTASK S, BLOCK_GRID_MAPPING M, BLOCK_MAN B"
					+ " WHERE S.BLOCK_MAN_ID = B.BLOCK_MAN_ID"
					+ "   AND B.BLOCK_ID = M.BLOCK_ID"
					+ "   AND S.TYPE = 4"
					+ "   AND S.STATUS = 0"
					+ "   AND EXISTS (SELECT 1"
					+ "	          FROM TABLE(CLOB_TO_TABLE(?)) GRID_TABLE"
					+ "	         WHERE M.GRID_ID = GRID_TABLE.COLUMN_VALUE)"
					+ "   AND NOT EXISTS"
					+ " (SELECT 1 FROM GRID_LIST G WHERE M.GRID_ID = G.GRID_ID)";
			
			return new QueryRunner().query(conn, sql, new ResultSetHandler<List<String>>(){
				@Override
				public List<String> handle(ResultSet rs) throws SQLException {
					List<String> gridList =new ArrayList<String>();
					while(rs.next()){
						gridList.add(rs.getString("GRID_ID"));
					}
					return gridList;
				}
			},pidsClob);
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询grid失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	
	public List<String> queryMergeGrid(JSONObject json) throws ServiceException {
		Connection conn = null;
		try {

			conn = DBConnector.getInstance().getManConnection();
			// 根据输入的几何wkt，计算几何包含的gird，
			Polygon polygon = (Polygon) GeometryUtils.getPolygonByWKT(json.getString("wkt"));
			Set<?> grids = (Set<?>) CompGeometryUtil.geo2GridsWithoutBreak(polygon);


			List<String> gridList = new ArrayList<String>();
			gridList.addAll((Collection<? extends String>) grids);
		
			//获取的gird,只要road 日编完成100%，就可融合
			StaticsApi statics = (StaticsApi) ApplicationContextUtil
					.getBean("staticsApi");
			List<GridStatInfo> GridStatList=statics.getLatestDailyEditStatByGrids(gridList);
			
			List<String> gridMerge = new ArrayList<String>();
			
			for(int i=0;i<GridStatList.size();i++){
				GridStatInfo statInfo=GridStatList.get(i);
	
				if (statInfo.getPercentRoad()==100){
					gridMerge.add(statInfo.getGridId());
				}
			}

			return gridMerge;
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询可融合grid失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public Page subtaskList(int gridId,int stage,final int curPageNum,final int pageSize) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();

			String selectSql = "select s.SUBTASK_ID, s.NAME, s.TYPE,s.STATUS from SUBTASK s, SUBTASK_GRID_MAPPING sgm"
					+ " where sgm.SUBTASK_ID = s.SUBTASK_ID"
					+ " and sgm.GRID_ID = " + gridId
					+ " and s.STAGE = " + stage;
			

			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					List<HashMap<Object, Object>> list = new ArrayList<HashMap<Object, Object>>();
					Page page = new Page(curPageNum);
				    page.setPageSize(pageSize);
				    int total = 0;
					while (rs.next()) {
						if(total==0){
							total=rs.getInt("TOTAL_RECORD_NUM_");
						}
						HashMap<Object, Object> map = new HashMap<Object, Object>();
						map.put("subtaskId", rs.getInt("subtask_id"));
						map.put("name", rs.getString("name"));
						map.put("type", rs.getInt("type"));
						map.put("status", rs.getInt("status"));
						list.add(map);
					}
					page.setTotalCount(total);
					page.setResult(list);
					return page;
				}
			};

			return run.query(curPageNum, pageSize,conn, selectSql,rsHandler);
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询grid所在子任务:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * @param blockManId
	 * @param neighbor 
	 * @return
	 * @throws ServiceException 
	 */
	public List<Integer> listByInforBlockManId(int blockManId, int neighbor) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();

			String selectSql = "SELECT DISTINCT I.INFOR_ID, IGM.GRID_ID,BM.BLOCK_ID"
					+ " FROM BLOCK_MAN          BM,"
					+ " TASK               T,"
					+ " INFOR              I,"
					+ " INFOR_GRID_MAPPING IGM,"
					+ " BLOCK_GRID_MAPPING BGM"
					+ " WHERE BM.TASK_ID = T.TASK_ID"
					+ " AND T.TASK_TYPE = 4"
					+ " AND T.TASK_ID = I.TASK_ID"
					+ " AND I.INFOR_ID = IGM.INFOR_ID"
					+ " AND BGM.GRID_ID = IGM.GRID_ID"
					+ " AND BGM.BLOCK_ID = BM.BLOCK_ID"
					+ " AND BM.BLOCK_MAN_ID = " + blockManId;	

			ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>() {
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> list = new ArrayList<Integer>();
					while (rs.next()) {
						list.add(rs.getInt("GRID_ID"));
					}
					return list;
				}
			};
			
			List<Integer> grids = run.query(conn, selectSql,rsHandler);
			Set<Integer> gridsWithNeighbor = new HashSet<Integer>();
			//grid扩圈
			if(1==neighbor){
				for(int j=0;j<grids.size();j++)  
			        {              
						String gridId = String.valueOf(grids.get(j));
						String[] gridAfter = GridUtils.get9NeighborGrids(gridId);
						List<String> gridIdlist = gridsFilter(conn,gridId,gridAfter);					
						for(int i=0;i<gridIdlist.size();i++){
							gridsWithNeighbor.add(Integer.valueOf(gridIdlist.get(i)));
						}           
			        } 
				grids.clear();
				grids.addAll(gridsWithNeighbor);
			}
			return grids;
//			return run.query(conn, selectSql,rsHandler);
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询大区BLOCK下grid:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	private List<String> gridsFilter(Connection conn,String gridId,String[] gridIds) throws Exception{
		try {
			QueryRunner run = new QueryRunner();
			
			String gridIdsStr = StringUtils.join(gridIds, ",");
			
			String selectSql = "SELECT BGM2.GRID_ID"
					+ " FROM BLOCK_GRID_MAPPING BGM2"
					+ " WHERE BGM2.GRID_ID IN (" + gridIdsStr + ")"
					+ " AND BGM2.BLOCK_ID = (SELECT BGM.BLOCK_ID FROM BLOCK_GRID_MAPPING BGM,BLOCK B WHERE BGM.BLOCK_ID = B.BLOCK_ID AND B.CITY_ID = 100002 AND BGM.GRID_ID = " + gridId +")";
			
			ResultSetHandler<List<String>> rsHandler = new ResultSetHandler<List<String>>() {
				public List<String> handle(ResultSet rs) throws SQLException {
					List<String> gridIdlist = new ArrayList<String>();
					while (rs.next()) {
						gridIdlist.add(rs.getString("GRID_ID"));
					}
					return gridIdlist;
				}

			};

			return run.query(conn, selectSql, rsHandler);
			
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询grid失败:" + e.getMessage(), e);
		}
	}
}
