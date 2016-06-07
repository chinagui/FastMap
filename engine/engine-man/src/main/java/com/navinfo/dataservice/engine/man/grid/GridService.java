package com.navinfo.dataservice.engine.man.grid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.datahub.manager.DbManager;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.dataservice.engine.dao.DBConnector;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.MeshUtils;

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
	 * @return 根据给定的gridlist，查询获取regioin和grid的映射；<br/>
	 * @throws Exception 
	 * 
	 */
	public Map queryRegionGridMapping(List<Integer> gridList) throws Exception{
		String sql = "select grid_id,region_id from grid where 1=1  ";
		QueryRunner queryRunner = new QueryRunner();
		Connection conn = null;
		try{
			OracleSchema schema = (OracleSchema)new DbManager().getOnlyDbByType("fmMan");//TODO:
			conn = schema.getDriverManagerDataSource().getConnection();
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
			StringBuffer InClause = buildInClause("grid_id",gridList);
			sql=sql+InClause;
			if(InClause!=null){
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
	public static void main(String[] args) throws Exception {

		GridService s = new GridService();

		System.out.println(s.getByUser(4408, 11));
	}
	
}
