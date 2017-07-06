package com.navinfo.dataservice.engine.meta.service;

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
import org.springframework.stereotype.Service;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

/** 
* @ClassName:  ScPointPoicodeNewService 
* @author code generator
* @date 2017-05-18
* @Description: TODO
*/
@Service
public class ScPointPoicodeNewService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	/**
	 * @Title: list
	 * @Description: 查询POI分类元数据接口
	 * @param jsonReq
	 * @return
	 * @throws ServiceException  JSONArray
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年5月18日 下午5:14:51 
	 */
	public JSONArray list(JSONObject jsonReq)throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
					
			String selectSql = " select n.class_name,n.sub_class_name,n.kind_name,n.kind_code,n.class_code,n.sub_class_code from  "
					+ " (select s.class_name,s.sub_class_name,s.kind_name,s.kind_code,s.class_code,s.sub_class_code,TO_CHAR(class_name) || '' || TO_CHAR(sub_class_name) || '' || TO_CHAR(kind_name) name from SC_POINT_POICODE_NEW s ) n "
					+ " where 1=1  ";
			
			if (jsonReq.containsKey("name") && jsonReq.getString("name") != null && StringUtils.isNotEmpty(jsonReq.getString("name"))){
				selectSql+=" and n.name like '%"+jsonReq.getString("name")+"%'  ";
			};
			if (jsonReq.containsKey("code") && jsonReq.getString("code") != null && StringUtils.isNotEmpty(jsonReq.getString("code"))){
				selectSql+=" and n.kind_code = '"+jsonReq.getString("code")+"'  ";
			};
			ResultSetHandler<JSONArray> rsHandler = new ResultSetHandler<JSONArray>(){
				public JSONArray handle(ResultSet rs) throws SQLException {
					JSONArray data = new JSONArray();
					while(rs.next()){
						JSONArray jsonArr = new JSONArray();
						jsonArr.add(rs.getString("class_name"));
						jsonArr.add(rs.getString("sub_class_name"));
						jsonArr.add(rs.getString("kind_name"));
						jsonArr.add(Integer.parseInt(rs.getString("kind_code")));
						jsonArr.add(Integer.parseInt(rs.getString("class_code")));
						jsonArr.add(rs.getString("sub_class_code"));
						jsonArr.add(rs.getString("kind_code"));
						
						data.add(jsonArr);
					}
					return data;
				}
	    	};
	    	return run.query(conn, selectSql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * @Title: list
	 * @Description: 查询POI分类元数据接口
	 * @param jsonReq
	 * @return
	 * @throws ServiceException  JSONArray
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年5月18日 下午5:14:51 
	 */
	public List<Map<String, Object>> list()throws ServiceException{
		Connection conn = null;
		try{
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getMetaConnection();	
					
			String selectSql = " select distinct n.class_name,n.sub_class_name,n.class_code,n.sub_class_code from  "
					+ " SC_POINT_POICODE_NEW n order by n.class_code";
			ResultSetHandler<List<Map<String, Object>>> rsHandler = new ResultSetHandler<List<Map<String, Object>>>(){
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					List<Map<String, Object>> returns=new ArrayList<Map<String, Object>>();
					Map<String, Object> bigClassMap=new HashMap<String, Object>();					
					List<Map<String, Object>> subClassS=new ArrayList<Map<String, Object>>();					
					String bigClass="";
					while(rs.next()){
						Map<String, Object> subClassMap=new HashMap<String, Object>();
						if(StringUtils.isEmpty(bigClass)){bigClass=rs.getString("class_code");}
						if(!bigClass.equals(rs.getString("class_code"))){
							bigClassMap.put("subClassCodes", subClassS);
							returns.add(bigClassMap);
							bigClassMap=new HashMap<String, Object>();
							subClassS=new ArrayList<Map<String, Object>>();
							bigClass=rs.getString("class_code");
						}
						bigClassMap.put("classCode", rs.getString("class_code"));
						bigClassMap.put("className", rs.getString("class_name"));
						bigClassMap.put("flag", 1);
						subClassMap.put("classCode", rs.getString("sub_class_code"));
						subClassMap.put("className", rs.getString("sub_class_name"));
						subClassMap.put("flag", 1);
						subClassS.add(subClassMap);
					}
					bigClassMap.put("subClassCodes", subClassS);
					returns.add(bigClassMap);
					return returns;
				}
	    	};
	    	return run.query(conn, selectSql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
}
