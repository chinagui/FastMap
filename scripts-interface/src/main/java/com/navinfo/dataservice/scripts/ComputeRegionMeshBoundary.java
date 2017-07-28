package com.navinfo.dataservice.scripts;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: ComputeRegionMeshBoundary
 * @author xiaoxiaowen4127
 * @date 2017年7月25日
 * @Description: ComputeRegionMeshBoundary.java
 */
public class ComputeRegionMeshBoundary {
	public static Logger log = Logger.getLogger(SyncTips2Oracle.class);

	public static Set<String> getSingleRegionMeshes(Connection conn,int regionId)throws Exception{
		String sql = "SELECT M.MESH FROM CP_MESHLIST@METADB_LINK M,CP_REGION_PROVINCE R WHERE M.ADMINCODE=R.ADMINCODE AND REGION_ID=?";
		return new QueryRunner().query(conn, sql, new ResultSetHandler<Set<String>>(){

			@Override
			public Set<String> handle(ResultSet rs) throws SQLException {
				Set<String> res = new HashSet<String>();
				while(rs.next()){
					res.add(rs.getString("MESH"));
				}
				return res;
			}
			
		},regionId);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection conn = null;
		try{
			DbInfo tiInfo = DbService.getInstance().getOnlyDbByBizType("fmMan");
			final OracleSchema schema = new OracleSchema(
					DbConnectConfig.createConnectConfig(tiInfo.getConnectParam()));
			conn = schema.getPoolDataSource().getConnection();
			int[] regionIds = new int[]{13};
			Map<Integer,Set<String>> regionGrids = new HashMap<Integer,Set<String>>();
			for(int i:regionIds){
				regionGrids.put(i, getSingleRegionMeshes(conn,i));
			}
			JSONArray ja = new JSONArray();
			for(Entry<Integer,Set<String>> entry:regionGrids.entrySet()){
				JSONObject jo = new JSONObject();
				jo.put("regionId", entry.getKey());
				Geometry geo = MeshUtils.meshes2Jts(entry.getValue());
				jo.put("boundary", geo.toText());
				ja.add(jo);
			}
			log.info(ja.toString());
			
		}catch(Exception e){
			log.error(e.getMessage(),e);
			log.info("Over.");
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}

}
