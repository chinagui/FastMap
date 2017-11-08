package com.navinfo.dataservice.engine.man.region;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.api.man.model.RegionMesh;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.sql.SqlClause;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

/**
 * @ClassName: RegionService
 * @author code generator
 * @date 2016-06-08 02:32:17
 * @Description: 
 */
public class RegionService {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private RegionService(){}
	private static class SingletonHolder{
		private static final RegionService INSTANCE =new RegionService();
	}
	public static RegionService getInstance(){
		return SingletonHolder.INSTANCE;
	}
	
	public List<Region> list() throws Exception{
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();

			String selectSql = "select * from Region where 1=1 ";
			
			ResultSetHandler<List<Region>> rsHandler = new ResultSetHandler<List<Region>>() {
				public List<Region> handle(ResultSet rs) throws SQLException {
					List<Region> list = new ArrayList<Region>();
					while (rs.next()) {
						Region region = new Region();
						region.setRegionId(rs.getInt("REGION_ID"));
						region.setRegionName(rs.getString("REGION_NAME"));
						region.setDailyDbId(rs.getInt("DAILY_DB_ID"));
						region.setMonthlyDbId(rs.getInt("MONTHLY_DB_ID"));
						list.add(region);
					}
					return list;
				}

			};
			return run.query(conn, selectSql, rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public List<Region> list(Region bean) throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();

			String selectSql = "select * from Region where 1=1 ";
			List<Object> values = new ArrayList<Object>();
			if (bean != null && bean.getRegionId() != null
					&& StringUtils.isNotEmpty(bean.getRegionId().toString())) {
				selectSql += " and REGION_ID=? ";
				values.add(bean.getRegionId());
			}
			;
			if (bean != null && bean.getRegionName() != null
					&& StringUtils.isNotEmpty(bean.getRegionName().toString())) {
				selectSql += " and REGION_NAME=? ";
				values.add(bean.getRegionName());
			}
			;
			if (bean != null && bean.getDailyDbId() != null
					&& StringUtils.isNotEmpty(bean.getDailyDbId().toString())) {
				selectSql += " and DAILY_DB_ID=? ";
				values.add(bean.getDailyDbId());
			}
			;
			if (bean != null && bean.getMonthlyDbId() != null
					&& StringUtils.isNotEmpty(bean.getMonthlyDbId().toString())) {
				selectSql += " and MONTHLY_DB_ID=? ";
				values.add(bean.getMonthlyDbId());
			}
			;
			ResultSetHandler<List<Region>> rsHandler = new ResultSetHandler<List<Region>>() {
				public List<Region> handle(ResultSet rs) throws SQLException {
					List<Region> list = new ArrayList<Region>();
					while (rs.next()) {
						Region region = new Region();
						region.setRegionId(rs.getInt("REGION_ID"));
						region.setRegionName(rs.getString("REGION_NAME"));
						region.setDailyDbId(rs.getInt("DAILY_DB_ID"));
						region.setMonthlyDbId(rs.getInt("MONTHLY_DB_ID"));
						list.add(region);
					}
					return list;
				}

			};
			if (values.size() == 0) {
				return run.query(conn, selectSql, rsHandler);
			}
			return run.query(conn, selectSql, rsHandler, values.toArray());
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public Region query(Region bean ) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			return query(conn,bean.getRegionId());			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public Region query(Connection conn,int regionId) throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();
			String selectSql = "select * from Region where REGION_ID=?";
			ResultSetHandler<Region> rsHandler = new ResultSetHandler<Region>() {
				public Region handle(ResultSet rs) throws SQLException {
					while (rs.next()) {
						Region region = new Region();
						region.setRegionId(rs.getInt("REGION_ID"));
						region.setRegionName(rs.getString("REGION_NAME"));
						region.setDailyDbId(rs.getInt("DAILY_DB_ID"));
						region.setMonthlyDbId(rs.getInt("MONTHLY_DB_ID"));
						
						return region;
					}
					return null;
				}

			};
			return run.query(conn, selectSql, rsHandler, regionId);			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} 
	}
	public Region queryByDbId(int dbId) throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "SELECT REGION_ID,REGION_NAME,DAILY_DB_ID,MONTHLY_DB_ID FROM REGION WHERE DAILY_DB_ID=? OR MONTHLY_DB_ID=? AND ROWNUM=1";
			
			return run.query(conn, selectSql, new SingleRegionRsHander() , dbId,dbId);
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public int queryDbIdByAdminId(int adminId) throws ServiceException{
		Connection conn = null;
		
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "SELECT DAILY_DB_ID FROM REGION R, CITY C WHERE R.REGION_ID=C.REGION_ID AND C.ADMIN_ID=?";
			
			ResultSetHandler<Integer> rsHandler = new ResultSetHandler<Integer>() {
				public Integer handle(ResultSet rs) throws SQLException {
					if (rs.next()) {
						return rs.getInt("DAILY_DB_ID");
					}
					return 0;
				}

			};
			
			int dbId = run.query(conn, selectSql, rsHandler, adminId);
			
			if(dbId == 0){
				throw new ServiceException("未找到adminId "+adminId+" 对应的大区");
			}

			return dbId;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public List<Region> queryRegionWithGrids(List<Integer> grids) throws Exception{
		assert CollectionUtils.isNotEmpty(grids);
//		String sql = "select grid_id,region_id from grid g where 1=1 ";
		String sql = "SELECT DISTINCT R.REGION_ID,BGM.GRID_ID"
				+ " FROM REGION R, BLOCK B, BLOCK_GRID_MAPPING BGM"
				+ " WHERE R.REGION_ID = B.REGION_ID"
				+ " AND B.BLOCK_ID = BGM.BLOCK_ID ";
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
			SqlClause inClause = SqlClause.genInClauseWithMulInt(conn,grids,"grid_id");
			if (inClause!=null)
				sql = sql + " AND "+ inClause.getSql();
			this.log.debug("sql:"+sql);
			MultiValueMap regionGridMapping =null;
			if (inClause!=null && CollectionUtils.isNotEmpty(inClause.getValues())){
				regionGridMapping = queryRunner.query(conn, sql, rsh,inClause.getValues().toArray());
			}else{
				regionGridMapping = queryRunner.query(conn, sql, rsh);
			}
			
			Set<Integer> regionIds = regionGridMapping.keySet();
			List<Region> regionList = new ArrayList<Region>();
			for(Integer regionId:regionIds){
				Region condition =new Region();
				condition.setRegionId(regionId);
				Region region = this.query(condition );
				region.setGrids((List<Integer>) regionGridMapping.get(regionId));
				regionList.add(region);
			}
			return regionList;

		} finally {
			DbUtils.closeQuietly(conn);
		}
		
	}
	
	public List<RegionMesh> queryRegionWithMeshes(Collection<String> meshes)throws Exception{
		assert CollectionUtils.isNotEmpty(meshes);
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			Clob clob=ConnectionUtil.createClob(conn);
			clob.setString(1, StringUtils.join(meshes, ","));
			
			StringBuilder sb = new StringBuilder();
			sb.append("with temp as (\n");
			sb.append("SELECT R.REGION_ID,R.DAILY_DB_ID,M.GN/100 MESH FROM REGION R,GRID G,(SELECT TO_NUMBER(COLUMN_VALUE)*100 GN FROM TABLE(clob_to_table(?))) M WHERE R.REGION_ID=G.REGION_ID AND G.GRID_ID=M.GN\n");
			sb.append(" ) select REGION_ID,DAILY_DB_ID,LISTAGG(MESH,',') WITHIN GROUP (ORDER BY MESH) MESHES FROM TEMP GROUP BY REGION_ID,DAILY_DB_ID");
			
			return new QueryRunner().query(conn, sb.toString(), new ResultSetHandler<List<RegionMesh>>(){

				@Override
				public List<RegionMesh> handle(ResultSet rs) throws SQLException {
					List<RegionMesh> result = new ArrayList<RegionMesh>();
					while(rs.next()){
						RegionMesh rm = new RegionMesh();
						rm.setRegionId(rs.getInt("REGION_ID"));
						rm.setDailyDbId(rs.getInt("DAILY_DB_ID"));
						//monthly_db_id...
						String meshstr = rs.getString("MESHES");
						if(StringUtils.isNotEmpty(meshstr)){
							Set<String> meshes = new HashSet<String>();
							CollectionUtils.addAll(meshes, meshstr.split(","));
							rm.setMeshes(meshes);
						}
						result.add(rm);
					}
					return result;
				}
				
			},clob);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	
	class SingleRegionRsHander implements ResultSetHandler<Region>{

		@Override
		public Region handle(ResultSet rs) throws SQLException {
			if(rs.next()){
				Region region = new Region();
				region.setRegionId(rs.getInt("REGION_ID"));
				region.setRegionName(rs.getString("REGION_NAME"));
				region.setDailyDbId(rs.getInt("DAILY_DB_ID"));
				region.setMonthlyDbId(rs.getInt("MONTHLY_DB_ID"));
				return region;
			}
			return null;
		}
		
	}
	
	class RegionRsHandler implements ResultSetHandler<List<Region>>{

		@Override
		public List<Region> handle(ResultSet rs) throws SQLException {
			List<Region> results = new ArrayList<Region>();
			while(rs.next()){
				Region region = new Region();
				region.setRegionId(rs.getInt("REGION_ID"));
				region.setRegionName(rs.getString("REGION_NAME"));
				region.setDailyDbId(rs.getInt("DAILY_DB_ID"));
				region.setMonthlyDbId(rs.getInt("MONTHLY_DB_ID"));
				results.add(region);
			}
			return results;
		}
		
	}
	/**
	 * @return
	 * @throws Exception 
	 */
	public List<Integer> listDayDbIds() throws Exception {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "select distinct r.daily_db_id from region r";
			return run.query(conn, selectSql, new ResultSetHandler<List<Integer>>(){

				@Override
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> result = new ArrayList<Integer>();
					while(rs.next()){
						result.add(rs.getInt("daily_db_id"));
					}
					return result;
				}});
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
}
