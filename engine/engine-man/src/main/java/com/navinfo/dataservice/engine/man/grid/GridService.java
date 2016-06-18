package com.navinfo.dataservice.engine.man.grid;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import net.sf.json.JSONObject;

public class GridService {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private GridService(){}
	private static class SingletonHolder{
		private static final GridService INSTANCE =new GridService();
	}
	public static GridService getInstance(){
		return SingletonHolder.INSTANCE;
	}
	
	public List<Grid> list()throws Exception{
		String sql = "SELECT GRID_ID,REGION_ID,CITY_ID,BLOCK_ID FROM GRID";
		QueryRunner run = new QueryRunner();
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			List<Grid> results = run.query(conn, sql, new GridResultSetHandler());
			return results;
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	/**
	 * @param gridList  <br/>
	 * <b>注意：如果参数gridList太长，会导致oracle sql太长而出现异常；</b>
	 * @return 根据给定的gridlist，查询获取regioin和grid的映射；key:RegionId；value：grid列表<br/>
	 * @throws Exception 
	 * 
	 */
	public Map queryRegionGridMapping(List<Integer> gridList) throws Exception{
		String sql = "select grid_id,r.* from grid g,region r where g.region_id=r.region_id ";
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
//							int regionDailyDbId = rs.getInt("daily_db_id");
//							int regionMongthlyDbId = rs.getInt("mongthly_db_id");
//							String regionName = rs.getString("regionName");
//							Region region = new Region(Integer.valueOf(regionId),regionName,Integer.valueOf(regionDailyDbId),Integer.valueOf(regionMongthlyDbId));
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
				whereClaus.append("and "+columName+" in (?");//grid_id 
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
	
	class GridResultSetHandler implements ResultSetHandler<List<Grid>>{

		@Override
		public List<Grid> handle(ResultSet rs) throws SQLException {
			List<Grid> results = new ArrayList<Grid>();
			if(rs.next()){
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
			//根据输入的几何wkt，计算几何包含的gird，目前只有方法，小文在实现中。。。
			List<?> grids=(List<?>) CompGeometryUtil.geo2GridsWithoutBreak(GeometryUtils.getMulPointByWKT(json.getString("wkt")));

			String selectSql = "select t.grid_id,s.status from subtask_grid_mapping t,subtask s where t.subtask_id=s.subtask_id "
					+ "and s.stage="+json.getInt("stage")+"and s.type="+json.getInt("type");
			StringBuffer InClause = buildInClause("t..grid_id",grids);
			String sql=selectSql+InClause;
	
			return GridOperation.queryGirdBySql(conn, sql,grids);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询grid失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
