package com.navinfo.dataservice.engine.man.config;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;

public class ConfigService {
	private Logger log=LoggerRepos.getLogger(getClass());
	
	private ConfigService(){}

	private static class SingletonHolder{
		private static final ConfigService INSTANCE=new ConfigService();
	}
	
	public static ConfigService getInstance(){
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * 更改man_config表参数值
	 * @param userId
	 * @param dataJson
	 * @throws Exception
	 */
	public void update(long userId, JSONObject dataJson) throws Exception{
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getManConnection();
			String key=dataJson.getString("confKey");
			String value=dataJson.getString("confValue");
			String updateSql="update man_config set conf_value='"+value+"',"
					+ "exe_user_id="+userId+" where conf_key='"+key+"'";
			QueryRunner runner=new QueryRunner();
			runner.update(conn, updateSql);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error("修改参数失败:"+dataJson, e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 查询配置列表
	 * @param dataJson
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> list() throws Exception {
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getManConnection();
			QueryRunner runner=new QueryRunner();
			String sql="SELECT CONF_KEY, CONF_VALUE, C.EXE_USER_ID, NVL(I.USER_REAL_NAME,'') USER_REAL_NAME, C.EXE_DATE"
					+ "  FROM MAN_CONFIG C, USER_INFO I"
					+ " WHERE C.EXE_USER_ID = I.USER_ID(+)";
			List<Map<String, Object>> result=runner.query(conn, sql, new ResultSetHandler<List<Map<String, Object>>>(){

				@Override
				public List<Map<String, Object>> handle(ResultSet rs)
						throws SQLException {
					List<Map<String, Object>> result=new ArrayList<Map<String, Object>>();
					while (rs.next()) {
						Map<String, Object> tmp=new HashMap<String, Object>();
						tmp.put("confKey", rs.getString("CONF_KEY"));
						tmp.put("confValue", rs.getString("CONF_VALUE"));
						tmp.put("exeUserId", rs.getInt("EXE_USER_ID"));
						tmp.put("exeUserName", rs.getString("USER_REAL_NAME"));
						tmp.put("exeDate", DateUtils.dateToString(rs.getTimestamp("EXE_DATE")));
						result.add(tmp);
					}
					return result;
				}
				
			});
			return result;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error("查询列表错误", e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 查询配置对应值
	 * @param dataJson
	 * @return
	 * @throws Exception
	 */
	public String query(String confKey) throws Exception {
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getManConnection();
			QueryRunner runner=new QueryRunner();
			String sql="SELECT CONF_VALUE"
					+ "  FROM MAN_CONFIG C"
					+ " WHERE c.conf_key='"+confKey+"'";
			String result=runner.query(conn, sql, new ResultSetHandler<String>(){

				@Override
				public String handle(ResultSet rs)
						throws SQLException {
					while (rs.next()) {
						return rs.getString("CONF_VALUE");
					}
					return null;
				}
				
			});
			return result;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error("查询配置错误", e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 图幅闸权限验证接口
	 * 原则：
	 * 1.	验证输入的密码是否与sys库配置的man.password相同；相同返回true，不同false
	 * 应用场景：管理平台—图幅管理
	 * @param password
	 * @return
	 */
	public boolean verify(String password) {
		String passwordMan=SystemConfigFactory.getSystemConfig().getValue(PropConstant.manPassword);
		if(passwordMan.equals(password)){return true;}
		else{return false;}
	}
	/**
	 * 图幅闸开关接口
	 * 原则：
	 * 1.	根据openFlag，对图幅进行开关操作
	 * 2.	Action有值，则对action对应的图幅操作
	 * 3.	mediumAction有值，则对meshList图幅操作开关+mediumAction对应的MEDIUM1_FLAG/MEDIUM2_FLAG=1
	 * 4.	mediumAction无值，meshList有值，则对meshList图幅操作开关
	 * 5.	仅openFlag有值，对全部图幅进行操作
	 * @param dataJson
	 * @throws Exception 
	 */
	public void mangeMesh(JSONObject dataJson) throws Exception {
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getMetaConnection();
			mangeMesh(conn,dataJson);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error("查询配置错误", e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 图幅闸开关接口
	 * 原则：
	 * 1.	根据openFlag，对图幅进行开关操作
	 * 2.	Action有值，则对action对应的图幅操作
	 * 3.	mediumAction有值，则对meshList图幅操作开关+mediumAction对应的MEDIUM1_FLAG/MEDIUM2_FLAG=1
	 * 4.	mediumAction无值，meshList有值，则对meshList图幅操作开关
	 * 5.	仅openFlag有值，对全部图幅进行操作
	 * @param dataJson
	 * @throws Exception 
	 */
	public void mangeMesh(Connection metaConn,JSONObject dataJson) throws Exception {
		try{
			int openFlag=dataJson.getInt("openFlag");
			
			String addUpdate="";
			/*QUICK1_FLAG
			QUICK2_FLAG
			QUICK3_FLAG
			MEDIUM1_FLAG
			MEDIUM2_FLAG*/
			if(dataJson.containsKey("mediumAction")){
				int mediumAction=dataJson.getInt("mediumAction");
				addUpdate=",s.MEDIUM"+mediumAction+"_FLAG=1";
			}
			if(dataJson.containsKey("quickAction")){
				int quickAction=dataJson.getInt("quickAction");
				addUpdate=",s.QUICK"+quickAction+"_FLAG=1";
			}
			String sql="update SC_PARTITION_MESHLIST s set s.open_flag="+openFlag+addUpdate+" where 1=1";
			//快线项目日落月关图幅时，只有批次不一致的才关闭图幅
			if(dataJson.containsKey("quickAction")){
				sql=sql+" and s.action!="+dataJson.getInt("quickAction");
			}
			
			if(dataJson.containsKey("action")){
				sql=sql+" and s.action="+dataJson.getInt("action");
			}
			Clob clob=null;
			if(dataJson.containsKey("meshList")){
				//sql=sql+" and s.mesh in ('"+dataJson.getString("meshList").replaceAll(",", "'")+"')";				
				sql=sql+" and s.mesh in (select column_value from table(clob_to_table(?)))";
				clob=ConnectionUtil.createClob(metaConn);
				clob.setString(1, dataJson.getString("meshList").replaceAll(" ", ""));
			}	
			log.info(sql);
			QueryRunner runner=new QueryRunner();
			if(clob==null){runner.update(metaConn, sql);}
			else{runner.update(metaConn, sql,clob);}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(metaConn);
			log.error("查询配置错误", e);
			throw e;
		}
	}
}
