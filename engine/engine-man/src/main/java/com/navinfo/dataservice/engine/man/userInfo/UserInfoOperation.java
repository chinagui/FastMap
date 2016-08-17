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
	
	public static HashMap<Object,Object> loginGetUserInfo(Connection conn,UserInfo userInfo,UserDevice userDevice) throws Exception{
		try{

			QueryRunner run = new QueryRunner();
			String selectSql = "select distinct u.user_id,u.user_real_name"
					+ ",r.role_name"
					+ ",(case when d.device_token = '"
					+ userDevice.getDeviceToken() 
					+"' and d.device_platform = '"
					+ userDevice.getDevicePlatform()
					+ "' and d.device_version = '"
					+ userDevice.getDeviceVersion()
					+ "' then d.device_id else 0 end) AS device_id "
					+ ",(case when uu.device_id = d.device_id and d.user_id = uu.user_id then 1 else 0 end) AS upload "
					+ " from user_info u, role r, role_user_mapping rum, user_device d, user_upload uu "
					+ " where u.user_id = rum.user_id "
					+ " and u.user_id = d.user_id(+) "
					+ " and u.user_id = uu.user_id(+) "
					+ " and rum.role_id = r.role_id "
					+ " and u.user_nick_name = '" + userInfo.getUserNickName() + "'"
					+ " and u.user_password = '" + userInfo.getUserPassword() + "'";


			ResultSetHandler<HashMap<Object,Object>> rsHandler = new ResultSetHandler<HashMap<Object,Object>>() {
				public HashMap<Object,Object> handle(ResultSet rs) throws SQLException {
					HashMap<Object,Object> map = new HashMap<Object,Object>();
					while (rs.next()) {
						if(!map.containsKey("userId")){
							map.put("userId", rs.getLong("user_id"));
							map.put("userRealName", rs.getString("user_real_name"));
							map.put("roleName", rs.getString("role_name"));
						}
						if(rs.getInt("device_id") != 0){
							map.put("upload", rs.getInt("upload"));
							map.put("deviceId",rs.getInt("device_id"));
						}
						if((rs.getInt("device_id") != 0) && (rs.getInt("upload") == 1)){
							map.put("upload", rs.getInt("upload"));
							map.put("deviceId",rs.getInt("device_id"));
							return map;
						}
					}
					return map;
				}

			};

			HashMap<Object,Object> map = run.query(conn, selectSql, rsHandler);
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

	/**
	 * @param conn
	 * @param userId
	 * @param deviceId
	 * @throws Exception 
	 */
	public static void insertIntoUserUpload(Connection conn, long userId, int deviceId) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			
			// 插入user_upload
			String createSql = "insert into user_upload"
					+ " (user_id,device_id) "
					+ "values("
					+ userId
					+ ","
					+ deviceId 
					+ ")";

			run.update(conn, createSql);
			
			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("插入userDevice，原因为:"+e.getMessage(),e);
		}
		
	}

	/**
	 * @param conn
	 * @param userInfo
	 * @return
	 * @throws Exception 
	 */
	public static UserInfo getUserInfoByNickNameAndPassword(Connection conn, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			
			// 插入user_upload
			String querySql = "select u.user_id,u.user_real_name"
					+ " from user_info u"
					+ " where u.user_nick_name = '" + userInfo.getUserNickName() + "'"
					+ " and u.user_password = '" + userInfo.getUserPassword() + "'";
			
			
			ResultSetHandler<UserInfo> rsHandler = new ResultSetHandler<UserInfo>() {
				public UserInfo handle(ResultSet rs) throws SQLException {
					UserInfo user = new UserInfo();
					if (rs.next()) {
						user.setUserId(rs.getInt("user_id"));
						user.setUserRealName(rs.getString("user_real_name"));
					}
					return user;
				}

			};

			UserInfo user = run.query(conn, querySql, rsHandler);
			return user;
			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询user_info出错，原因为:"+e.getMessage(),e);
		}
	}

	/**
	 * @param conn
	 * @param userInfo
	 * @return
	 * @throws Exception 
	 */
	public static String getUserRole(Connection conn, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			
			// 插入user_upload
			String querySql = "select r.role_name"
					+ " from role r,role_user_mapping rum"
					+ " where rum.user_id = " + userInfo.getUserId()
					+ " and rum.role_id = r.role_id ";
					
			ResultSetHandler<String> rsHandler = new ResultSetHandler<String>() {
				public String handle(ResultSet rs) throws SQLException {
					String role = new String();
					if (rs.next()) {
						role = rs.getString("role_name");
					}
					return role;
				}

			};

			String role = run.query(conn, querySql, rsHandler);
			return role;
			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询用户角色，原因为:"+e.getMessage(),e);
		}
	}

	/**
	 * @param conn
	 * @param userInfo
	 * @param userDevice
	 * @return
	 * @throws Exception 
	 */
	public static UserDevice getUserDevice(Connection conn, UserInfo userInfo, UserDevice userDevice) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			
			// 插入user_upload
			String querySql = "select d.device_id"
					+ " from user_device d"
					+ " where d.device_token = '" + userDevice.getDeviceToken() + "'" 
					+ " and d.device_platform = '" + userDevice.getDevicePlatform() + "'"
					+ " and d.device_version = '" + userDevice.getDeviceVersion() + "'"
					+ " and d.user_id = " + userInfo.getUserId();
					
			ResultSetHandler<UserDevice> rsHandler = new ResultSetHandler<UserDevice>() {
				public UserDevice handle(ResultSet rs) throws SQLException {
					UserDevice device = new UserDevice();
					if (rs.next()) {
						device.setDeviceId(rs.getInt("device_id")); 
					}
					return device;
				}

			};

			UserDevice device = run.query(conn, querySql, rsHandler);
			return device;
			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询userDevice，原因为:"+e.getMessage(),e);
		}
	}

	/**
	 * @param conn
	 * @param userId
	 * @param deviceId
	 * @return
	 * @throws Exception 
	 */
	public static int getUserDeviceUpload(Connection conn, Integer userId, int deviceId) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			
			// 查询该用户该设备upload信息
			String querySql = "select count(1) as upload"
					+ " from user_upload uu"
					+ " where uu.user_id = " + userId
					+ " and uu.device_id = " + deviceId;
					
			ResultSetHandler<Integer> rsHandler = new ResultSetHandler<Integer>() {
				public Integer handle(ResultSet rs) throws SQLException {
					int upload = 0;
					if (rs.next()) {
						upload = rs.getInt("upload"); 
					}
					return upload;
				}
	
			};
	
			int upload = run.query(conn, querySql, rsHandler);
			return upload;
			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询userDevice，原因为:"+e.getMessage(),e);
		}
	}

	/**
	 * @param conn
	 * @param userId
	 * @param deviceId
	 * @throws Exception 
	 */
	public static void updateDeviceStatus(Connection conn, long userId, int deviceId) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			
			// 更新user_device状态信息
			String updateSql = "update user_device set status = 0"
					+ " where user_id = " + userId
					+ " and device_id != " + deviceId;
					
	
			run.update(conn, updateSql);
			
			updateSql = "update user_device set status = 1"
					+ " where user_id = " + userId
					+ " and device_id = " + deviceId;
					
	
			run.update(conn, updateSql);
			
			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询userDevice，原因为:"+e.getMessage(),e);
		}
		
	}
}
