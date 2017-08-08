package com.navinfo.dataservice.scripts.tmp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGridUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: GeneratePoiDownloadUrl
 * @author xiaoxiaowen4127
 * @date 2017年8月3日
 * @Description: GeneratePoiDownloadUrl.java
 */
public class GeneratePoiDownloadUrl {
	
	protected static Logger log = Logger.getLogger(GeneratePoiDownloadUrl.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection conn = null;
		try{
			DbInfo db = DbService.getInstance().getOnlyDbByBizType("fmMan");
			final OracleSchema schema = new OracleSchema(
					DbConnectConfig.createConnectConfig(db.getConnectParam()));
			conn = schema.getDriverManagerDataSource().getConnection();
			String urlBody = "http://fastmap.navinfo.com/service/edit/poi/base/download?access_token=000001OZJ5YVHQUIBB6CE1748622B74EC9482FC099E14837&parameter=";
			
//			String sql = "SELECT MESH FROM CP_MESHLIST@METADB_LINK WHERE SCALE='2.5' AND (FLAG IS NULL OR FLAG = 0) AND PROVINCE = '福建省' AND ROWNUM<151";
			String sql = "SELECT MESH FROM CP_MESHLIST@METADB_LINK WHERE SCALE='2.5' AND (FLAG IS NULL OR FLAG = 0) AND (MESH like '5956%' OR MESH LIKE '5955%' OR MESH LIKE '6055%')";
			Set<String> meshes = new QueryRunner().query(conn, sql, new ResultSetHandler<Set<String>>(){

				@Override
				public Set<String> handle(ResultSet rs) throws SQLException {
					Set<String> res = new HashSet<String>();
					while(rs.next()){
						res.add(rs.getString(1));
					}
					return res;
				}
				
			});
			log.info(StringUtils.join(meshes,","));
			int index = 0;
			JSONArray grids = new JSONArray();
			for(String mesh:meshes){
				for(String g:CompGridUtil.mesh2Grid(mesh)){
					JSONObject grid = new JSONObject();
					grid.put("grid", g);
					grid.put("date", "");
					grids.add(grid);
				}
				if(++index%3==0){
					JSONObject para = new JSONObject();
					para.put("grid", grids);
					log.info(urlBody+para.toString());
					grids.clear();
				}
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
		
	}

}
