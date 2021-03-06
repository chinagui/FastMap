package com.navinfo.dataservice.engine.man.region;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.CpRegionProvince;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @ClassName: RegionService
 * @author code generator
 * @date 2016-06-08 02:32:17
 * @Description: TODO
 */
public class CpRegionProvinceService {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private CpRegionProvinceService(){}
	private volatile static CpRegionProvinceService instance=null;
	public static CpRegionProvinceService getInstance(){
		if(instance==null){
			synchronized(CpRegionProvinceService.class){
				if(instance==null){
					instance=new CpRegionProvinceService();
				}
			}
		}
		return instance;
	}

	public List<CpRegionProvince> list() throws Exception{
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "SELECT * FROM CP_REGION_PROVINCE";
			return run.query(conn, selectSql, new FullMultiSelHandler());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	public Map<Integer,Integer> listDayDbIdsByAdminId() throws Exception{
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "SELECT C.ADMINCODE,R.DAILY_DB_ID FROM CP_REGION_PROVINCE C,REGION R WHERE C.REGION_ID=R.REGION_ID";
			return run.query(conn, selectSql, new ResultSetHandler<Map<Integer,Integer>>(){

				@Override
				public Map<Integer, Integer> handle(ResultSet rs) throws SQLException {
					Map<Integer,Integer> result = new HashMap<Integer,Integer>();
					while(rs.next()){
						result.put(rs.getInt("ADMINCODE"), rs.getInt("DAILY_DB_ID"));
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
	class FullMultiSelHandler implements ResultSetHandler<List<CpRegionProvince>>{

		@Override
		public List<CpRegionProvince> handle(ResultSet rs) throws SQLException {
			List<CpRegionProvince> result = new ArrayList<CpRegionProvince>();
			while(rs.next()){
				CpRegionProvince cp = new CpRegionProvince();
				cp.setRegionId(rs.getInt("REGION_ID"));
				cp.setNdsRegioncode(rs.getString("NDS_REGIONCODE"));
				cp.setAdmincode(rs.getInt("ADMINCODE"));
				cp.setProvince(rs.getString("PROVINCE"));
				result.add(cp);
			}
			return result;
		}
		
	}
	/**
	 * @return
	 * @throws Exception 
	 */
	public Map<String, Integer> getProvinceRegionIdMap() throws Exception {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "SELECT C.REGION_ID,C.PROVINCE FROM CP_REGION_PROVINCE C";
			return run.query(conn, selectSql, new ResultSetHandler<Map<String,Integer>>(){

				@Override
				public Map<String, Integer> handle(ResultSet rs) throws SQLException {
					Map<String,Integer> result = new HashMap<String,Integer>();
					while(rs.next()){
						result.put(rs.getString("PROVINCE"), rs.getInt("REGION_ID"));
					}
					return result;
				}});
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	public JSONArray getAdminCodeAndProvince() throws Exception{
		
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "select DISTINCT (Substr(admincode,0,2)) admincode,province from cp_region_province t ORDER BY admincode";
			return run.query(conn, selectSql, new ResultSetHandler<JSONArray>(){

				@Override
				public JSONArray  handle(ResultSet rs) throws SQLException {
					JSONArray jsonArray  = new JSONArray();
					while(rs.next()){
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("admincode", rs.getString("admincode"));
						jsonObject.put("province", rs.getString("province"));
						jsonArray.add(jsonObject);
					}
					return jsonArray;
				}});
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
}
