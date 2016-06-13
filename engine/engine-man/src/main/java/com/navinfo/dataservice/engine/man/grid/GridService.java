package com.navinfo.dataservice.engine.man.grid;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.springframework.util.StringUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.engine.man.region.Region;
import com.navinfo.navicommons.database.QueryRunner;

public class GridService {
	private GridService(){}
	private static class SingletonHolder{
		private static final GridService INSTANCE =new GridService();
	}
	public static GridService getInstance(){
		return SingletonHolder.INSTANCE;
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
	
}
