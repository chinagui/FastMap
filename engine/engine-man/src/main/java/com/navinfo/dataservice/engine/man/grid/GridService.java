package com.navinfo.dataservice.engine.man.grid;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.springframework.util.StringUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.navicommons.database.QueryRunner;

public class GridService {
	private GridService(){}
	private static class SingletonHolder{
		private static final GridService INSTANCE =new GridService();
	}
	public static GridService getInstance(){
		return SingletonHolder.INSTANCE;
	}
	public Set<Integer> queryGrid(int limit) throws SQLException{
		String sql = "select grid_id from grid g where rownum<?";
		QueryRunner queryRunner = new QueryRunner();
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();	
			ResultSetHandler<Set<Integer>> rsh = new ResultSetHandler<Set<Integer>>(){

				@Override
				public Set<Integer> handle(ResultSet rs) throws SQLException {
					if (rs!=null){
						Set<Integer> grids = new HashSet<Integer>();
						while(rs.next()){
							int gridId = rs.getInt("grid_id");
							grids.add(gridId);
						}
						return grids;
					}
					return null;
				}};
			return queryRunner.query(conn, sql, limit, rsh);
			
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	/**
	 * @param gridList  <br/>
	 * <b>注意：如果参数gridList太长(不能超过1000)，会导致oracle sql太长而出现异常；</b>
	 * @return 根据给定的gridlist，查询获取regioin和grid的映射；key:RegionId；value：grid列表<br/>
	 * @throws Exception 
	 * 
	 */
	public Map queryRegionGridMapping(List<Integer> gridList) throws Exception{
		String sql = "select grid_id from grid g where 1=1 ";
		QueryRunner queryRunner = new QueryRunner();
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();	
			ResultSetHandler<MultiValueMap> rsh = new ResultSetHandler<MultiValueMap>(){

				@Override
				public MultiValueMap handle(ResultSet rs) throws SQLException {
					if (rs!=null){
						MultiValueMap mvMap = new MultiValueMap();
						while(rs.next()){
							int gridId = rs.getInt("grid_id");
							int regionId = rs.getInt("region_id");
							mvMap.put(regionId, gridId);
						}
						return mvMap;
					}
					return null;
				}};
			StringBuffer InClause = buildInClause("g.grid_id",gridList);
			sql=sql+InClause;
			if(StringUtils.isEmpty(InClause)){
				return queryRunner.query(conn, sql, rsh);
			}else{
				return queryRunner.query(conn, sql, gridList.toArray(), rsh);
			}
			
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	private StringBuffer buildInClause(String columName,List inValuesList){
		int size = inValuesList.size();
		if (size==0) return null;
		StringBuffer whereClaus= new StringBuffer();
		for (int i=0;i<size;i++){
			if (i==0){
				whereClaus.append(" and "+columName+" in (?");//grid_id 
			}else{
				if (i==size-1){
					whereClaus.append(",?)");
				}else{
					whereClaus.append(",?");
				}
				
			}
		}
		return whereClaus;
	}

	/**
	 * @param taskList subTaskId的列表
	 * <b>注意：如果参数taskList太长（不能超过1000个），会导致oracle sql太长而出现异常；</b>
	 * @return MultiValueMap key是regionId，value是大区中满足条件的grid的列表
	 * @throws Exception
	 */
	public Map queryRegionGridMappingOfSubtasks(List<Integer> taskList) throws Exception {
		String sql = "select g.*  from grid g,subtask t ,subtask_grid_mapping m " + 
				" where t.subtask_id=m.subtask_id and m.grid_id=g.grid_id ";
		QueryRunner queryRunner = new QueryRunner();
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();	
			ResultSetHandler<MultiValueMap> rsh = new ResultSetHandler<MultiValueMap>(){

				@Override
				public MultiValueMap handle(ResultSet rs) throws SQLException {
					if (rs!=null){
						MultiValueMap mvMap = new MultiValueMap();
						while(rs.next()){
							int gridId = rs.getInt("grid_id");
							int regionId = rs.getInt("region_id");
							mvMap.put(regionId, gridId);
						}
						return mvMap;
					}
					return null;
				}};
			StringBuffer InClause = buildInClause(" t.subtask_id ",taskList);
			sql=sql+InClause;
			if(StringUtils.isEmpty(InClause)){
				return queryRunner.query(conn, sql, rsh);
			}else{
				return queryRunner.query(conn, sql, taskList.toArray(), rsh);
			}
			
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	
}
