package com.navinfo.dataservice.engine.man.inforMan;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.json.JSONException;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.common.DbOperation;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @ClassName: InforManService
 * @author code generator
 * @date 2016-06-15 02:27:02
 * @Description: TODO
 */
public class InforManService {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private InforManService() {
	}

	private static class SingletonHolder {
		private static final InforManService INSTANCE = new InforManService();
	}

	public static InforManService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	
	public HashMap<String,Object> query(int inforId) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "select * from infor where INFOR_ID=" + inforId;
			HashMap<String,Object> list = InforManOperation.selectTaskBySql2(conn, selectSql);
			list.put("gridIds", getProgramGridsByInfor(conn,inforId));
			return list;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public Map<Integer, Integer> getProgramGridsByInfor(Connection conn,int inforId)throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sqlString="SELECT GRID_ID,1 type"
					+ "  FROM INFOR_GRID_MAPPING"
					+ " WHERE INFOR_ID = "+inforId
					+ " UNION"
					+ " SELECT M.GRID_ID, M.TYPE"
					+ "  FROM PROGRAM_GRID_MAPPING M, PROGRAM P"
					+ " WHERE M.PROGRAM_ID = P.PROGRAM_ID"
					+ "   AND P.INFOR_ID = "+inforId;
			log.info("getProgramGridsByInfor sql:" + sqlString);
			ResultSetHandler<Map<Integer, Integer>> rsh = new ResultSetHandler<Map<Integer, Integer>>() {
				@Override
				public Map<Integer, Integer> handle(ResultSet rs) throws SQLException {
					Map<Integer, Integer> list = new HashMap<Integer, Integer>();
					while(rs.next()){
						list.put(rs.getInt("GRID_ID"), rs.getInt("type"));
					}
					return list;
				}
			};
			Map<Integer, Integer> gridList = run.query(conn, sqlString, rsh);
			return gridList;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询明细失败，原因为:" + e.getMessage(), e);
		}
	}

	public void close(List<String> inforIdslist) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			String inforIdStr = inforIdslist.toString().replace("[", "'").replace("]", "'").replace(" ", "")
					.replace(",", "','");

			String updateSql = "UPDATE INFOR SET PLAN_STATUS=2 WHERE INFOR_ID IN (" + inforIdStr + ")";
			DbOperation.exeUpdateOrInsertBySql(conn, updateSql);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("删除失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 一级情报监控
	 * 原则： 
	 * 根据wkt（可选）筛选全国一级情报数据。
	 * 1.	将wkt转成gridList
	 * 2.	与infor_grid_mapping关联可获取与wkt交叉的情报数据列表
	 * 使用场景：管理平台-一级情报监控
	 * @param fromObject
	 * @return
	 */
	public List<Map<String, Object>> monitor(JSONObject json)throws Exception {
		Connection conn=null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			String extendSql="";
//			if(json.containsKey("wkt")){
//				String wkt = json.getString("wkt");
//			}
			if(json.containsKey("planStatus")){
				String planStatus = ((json.getJSONArray("planStatus").toString()).replace('[', '(')).replace(']',
						')');
				extendSql=extendSql+"   AND T.PLAN_STATUS IN "+planStatus;
			}

			String selectSql = "SELECT T.INFOR_ID, T.GEOMETRY, T.PLAN_STATUS,NVL(R.PROGRAM_ID, 0) PROGRAM_ID"
					+ "  FROM INFOR T, PROGRAM R"
					+ " WHERE t.infor_id=r.infor_id(+)"
					+ extendSql;
			QueryRunner run=new QueryRunner();
			return run.query(conn, selectSql, new ResultSetHandler<List<Map<String,Object>>>(){
				@Override
				public List<Map<String, Object>> handle(ResultSet rs)
						throws SQLException {
					List<Map<String, Object>> res=new ArrayList<Map<String,Object>>();
					while(rs.next()){
						HashMap<String,Object> map = new HashMap<String,Object>();
						map.put("inforId", rs.getString("INFOR_ID"));
						JSONArray geoList = new JSONArray();
						String inforGeo=rs.getString("GEOMETRY");
						String[] inforGeoList=inforGeo.split(";");
						for(String geoTmp:inforGeoList){
							try {
								geoList.add(GeoTranslator.jts2Geojson(GeoTranslator.wkt2Geometry(geoTmp)));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						map.put("geometry", geoList);
						map.put("planStatus",rs.getInt("PLAN_STATUS"));
						map.put("programId", rs.getInt("PROGRAM_ID"));
						res.add(map);
					}
					return res;
				}});
		}catch (Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error("", e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
