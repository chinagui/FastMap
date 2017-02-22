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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.AbstractMapDecorator;
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
			//返回已出品的快速更新grid列表
			String sql="SELECT DISTINCT G.GRID_ID"
					+ "  FROM PROGRAM P, PRODUCE PR, INFOR_GRID_MAPPING G"
					+ " WHERE P.PROGRAM_ID = PR.PROGRAM_ID"
					+ "   AND P.INFOR_ID = G.INFOR_ID"
					+ "   AND PR.PRODUCE_STATUS = 2"
					+ "   AND EXISTS (SELECT 1"
					+ "	          FROM TABLE(CLOB_TO_TABLE(?)) GRID_TABLE"
					+ "	         WHERE G.GRID_ID = GRID_TABLE.COLUMN_VALUE)";
			
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
	 * @param taskId 
	 * @return
	 * @throws ServiceException 
	 */
	public List<Integer> listTaskGrid(int taskId) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();

			String selectSql = "SELECT GRID_ID FROM TASK_GRID_MAPPING WHERE TASK_ID="+taskId;

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
			/*Set<Integer> gridsWithNeighbor = new HashSet<Integer>();
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
			}*/
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



	/**
	 * @param condition JSONObject对象格式的查询条件 {"cityId":"12312"}
	 * @return grid ID 的列表
	 */
	public List<Integer> queryListByCondition(JSONObject condition)throws Exception {
		String sql = "select grid_id,region_id from grid g where 1=1 ";
		QueryRunner queryRunner = new QueryRunner();
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			ResultSetHandler<List<Integer>> rsh = new ResultSetHandler<List<Integer>>() {

				@Override
				public List<Integer> handle(ResultSet rs) throws SQLException {
					if (rs != null) {
						List<Integer> gridList = new ArrayList<Integer>();
						while (rs.next()) {
							int gridId = rs.getInt("grid_id");
							gridList.add(gridId);
						}
						return gridList;
					}
					return null;
				}
			};
			if(condition==null){
				return queryRunner.query(conn, sql, rsh);
			}
			List<Object> values = new ArrayList<Object>();
			if(condition.get("cityId")!=null){
				sql = sql + " AND CITY_ID=?";
				values.add(condition.get("cityId"));
			}
			if(CollectionUtils.isEmpty(values)){
				return queryRunner.query(conn, sql, rsh);
			}else{
				return queryRunner.query(conn, sql, rsh,values.toArray());
			}
			
			
			
			

		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	/**
	 * 获取grid对应的taskid，若为多个返回0
	 * @param grid
	 * @return Map<String,Integer> key："quickTaskId"，"centreTaskId"
	 * @throws Exception
	 */
	public Map<String,Integer> queryTaskIdsByGrid(String grid) throws Exception {
		String sql = "SELECT K.TASK_ID, P.TYPE"
				+ "  FROM TASK K, PROGRAM P, TASK_GRID_MAPPING M"
				+ " WHERE K.PROGRAM_ID = P.PROGRAM_ID"
				+ "   AND K.TASK_ID = M.TASK_ID"
				+ "   AND K.TYPE = 0"
				+ "   AND M.GRID_ID = " + grid
				+ " ORDER BY P.TYPE";
		QueryRunner queryRunner = new QueryRunner();
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			ResultSetHandler<Map<String,Integer>> rsh = new ResultSetHandler<Map<String,Integer>>() {

				@Override
				public Map<String,Integer> handle(ResultSet rs) throws SQLException {
					if (rs != null) {
						Map<String,Integer> map = new HashMap<String,Integer>();
						int quickTaskId=-1;
						int centreTaskId=-1;
						while (rs.next()) {
							int taskId = rs.getInt("TASK_ID");
							int type=rs.getInt("TYPE");
							if(type==1){
								if(centreTaskId==-1){
									centreTaskId=taskId;
								}else{
									centreTaskId=0;
								}
							}
							if(type==4){
								if(quickTaskId==-1){
									quickTaskId=taskId;
								}else{
									quickTaskId=0;
								}
							}
						}
						if(quickTaskId==-1){
							quickTaskId = 0;
						}
						if(centreTaskId==-1){
							centreTaskId = 0;
						}
						map.put("quickTaskId", quickTaskId);
						map.put("centreTaskId", centreTaskId);
						return map;
					}
					return null;
				}
			};
			return queryRunner.query(conn, sql, rsh);
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	/**
	 * @param conn 
	 * @param blockId 
	 * @return
	 * @throws Exception 
	 */
	public List<Integer> getGridListByBlockId(Connection conn, Integer blockId) throws Exception {
		try {
			QueryRunner run = new QueryRunner();
						
			String selectSql = "SELECT G.GRID_ID FROM GRID G WHERE G.BLOCK_ID = " + blockId;
			
			ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>() {
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> gridIdlist = new ArrayList<Integer>();
					while (rs.next()) {
						gridIdlist.add(rs.getInt("GRID_ID"));
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



	/**
	 * 获取grid对应的taskid，若为多个返回0
	 * @param grid
	 * @return Map<String,Integer> key："quickTaskId"，"centreTaskId"
	 * @throws SQLException 
	 * @throws Exception
	 */
	public Map<Integer,Map<String, Integer>> queryCollectTaskIdsByGridIdList(List<Integer> gridIdList) throws SQLException {	
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner queryRunner = new QueryRunner();
			String sql = "SELECT M.GRID_ID,K.TASK_ID, P.TYPE"
					+ "  FROM TASK K, PROGRAM P, TASK_GRID_MAPPING M"
					+ " WHERE K.PROGRAM_ID = P.PROGRAM_ID"
					+ "   AND K.TASK_ID = M.TASK_ID"
					+ "   AND K.TYPE = 0"
					+ "   AND M.GRID_ID IN (" + StringUtils.join(gridIdList,",")  + ")"
					+ " ORDER BY P.TYPE";
			
			log.info("queryCollectTaskIdsByGridIdList sql:" + sql);
			
			ResultSetHandler<Map<Integer,Map<String, Integer>>> rsh = new ResultSetHandler<Map<Integer,Map<String, Integer>>>() {
				@Override
				public Map<Integer,Map<String, Integer>> handle(ResultSet rs) throws SQLException {
					Set<Integer> gridIdSet = new HashSet<Integer>();
					Map<Integer,Map<String, Integer>> result = new HashMap<Integer,Map<String, Integer>>();
					while (rs.next()) {
						int gridId = rs.getInt("GRID_ID");
						int taskId = rs.getInt("TASK_ID");
						int type=rs.getInt("TYPE");
						if(!gridIdSet.contains(gridId)){
							Map<String,Integer> map = new HashMap<String,Integer>();
							map.put("quickTaskId", -1);
							map.put("centreTaskId", -1);
							result.put(gridId, map);
							gridIdSet.add(gridId);
						}
						if(type==1){
							if(result.get(gridId).get("centreTaskId")==-1){
								result.get(gridId).put("centreTaskId",taskId);
							}else if(result.get(gridId).get("centreTaskId")>0){
								result.get(gridId).put("centreTaskId",0);
							}
						}
						if(type==4){
							if(result.get(gridId).get("quickTaskId")==-1){
								result.get(gridId).put("quickTaskId",taskId);
							}else if(result.get(gridId).get("quickTaskId")>0){
								result.get(gridId).put("quickTaskId",0);
							}
						}
					}
					for(Map.Entry<Integer, Map<String,Integer>> entry:result.entrySet()){
						for(Map.Entry<String, Integer> entryInner:entry.getValue().entrySet()){
							if(entryInner.getValue()==-1){
								entryInner.setValue(0);
							}
						}
					}
					return result;
				}
			};

			Map<Integer,Map<String, Integer>> result = queryRunner.query(conn, sql, rsh);
			for(Integer gridId:gridIdList){
				if(!result.containsKey(gridId)){
					Map<String,Integer> map = new HashMap<String,Integer>();
					map.put("quickTaskId", 0);
					map.put("centreTaskId", 0);
					result.put(gridId, map);
				}
			}
			return result;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

}
