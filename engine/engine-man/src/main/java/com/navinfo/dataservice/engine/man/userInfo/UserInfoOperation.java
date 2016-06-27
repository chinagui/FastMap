package com.navinfo.dataservice.engine.man.userInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.log4j.Logger;

import java.sql.Connection;
import com.navinfo.dataservice.api.man.model.UserDevice;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.dataservice.api.man.model.UserInfo;

/** 
 * @ClassName: UserInfoOperation
 * @author songdongyan
 * @date 2016年6月23日
 * @Description: UserInfoOperation.java
 */
public class UserInfoOperation {
	private static Logger log = LoggerRepos.getLogger(TaskOperation.class);
	
	public UserInfoOperation() {
		// TODO Auto-generated constructor stub
	}
	
	public static HashMap loginGetUserInfo(Connection conn,UserInfo userInfo,UserDevice userDevice) throws Exception{
		try{

			QueryRunner run = new QueryRunner();
			String selectSql = "select distinct u.user_id"
					+ ",r.role_name"
					+ ",(case when d.device_token = '"
					+ userDevice.getDeviceToken() 
					+"' and d.device_platform = '"
					+ userDevice.getDevicePlatform()
					+ "' and d.device_version = '"
					+ userDevice.getDeviceVersion()
					+ "' then d.device_id else 0 end) AS device_id "
					+ " from user_info u, role r, role_user_mapping rum, user_device d "
					+ " where u.user_id = rum.user_id "
					+ " and rum.role_id = r.role_id "
					+ " and u.user_nick_name = '" + userInfo.getUserNickName() + "'"
					+ " and u.user_password = '" + userInfo.getUserPassword() + "'";



			ResultSetHandler<HashMap<?,?>> rsHandler = new ResultSetHandler<HashMap<?,?>>() {
				public HashMap<?,?> handle(ResultSet rs) throws SQLException {
					HashMap map = new HashMap();
					while (rs.next()) {
						if(!map.containsKey("userId")){
							map.put("userId", rs.getLong("user_id"));
							map.put("roleName", rs.getString("role_name"));
						}
						if(rs.getInt("device_id")!=0){
							map.put("deviceId",rs.getInt("device_id"));
							return map;
						}
					}
					return map;
				}

			};

			HashMap map = run.query(conn, selectSql, rsHandler);
			return map;
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	
	}

	/**
	 * @param conn
	 * @param userDevice
	 * @return
	 * @throws Exception 
	 */
	public static int insertIntoUserDevice(Connection conn,long userId, UserDevice userDevice) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();

			// 获取device_id
			String querySql = "select USER_DEVICE_SEQ.NEXTVAL as deviceId from dual";

			int deviceId = Integer.valueOf(run
						.query(conn, querySql, new MapHandler()).get("deviceId")
						.toString());
			
			// 插入user_device
			String createSql = "insert into user_device"
					+ " (device_id,user_id,device_token,device_platform,device_version) "
					+ "values("
					+ deviceId
					+ ","
					+ userId
					+ ",'"
					+ userDevice.getDeviceToken()
					+ "','"
					+ userDevice.getDevicePlatform()
					+ "','" 
					+ userDevice.getDeviceVersion() + "')";

			run.update(conn, createSql);
			
			return deviceId;
			
			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("插入userDevice，原因为:"+e.getMessage(),e);
		}
	}
}
