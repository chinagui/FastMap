
package com.navinfo.dataservice.engine.man.userInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;

import com.navinfo.dataservice.api.man.model.UserDevice;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONArray;

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
	
	public static List<Integer> getUserListBySql(Connection conn,String sql) throws Exception{
		try{

			QueryRunner run = new QueryRunner();
			ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>() {
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> userIdList = new ArrayList<Integer>();
					while (rs.next()) {
						userIdList.add(rs.getInt("USER_ID"));
					}
					return userIdList;
				}
			};
			List<Integer> userIdList = run.query(conn, sql, rsHandler);
			return userIdList;
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	
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
	public static Map<Object, Object> getUserRole(Connection conn, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
//			Map<Object, Object> role = new HashMap<Object, Object>();
//			role.put("roleId", 0);
//			role.put("roleName", "");
			
			// 插入user_upload
			String querySql = "select r.role_id,r.role_name"
					+ " from role r,role_user_mapping rum"
					+ " where rum.user_id = " + userInfo.getUserId()
					+ " and rum.role_id = r.role_id ";
					
			ResultSetHandler<Map<Object, Object>> rsHandler = new ResultSetHandler<Map<Object, Object>>() {
				public Map<Object, Object> handle(ResultSet rs) throws SQLException {
//					String role = new String();
					Map<Object, Object> role = new HashMap<Object, Object>();
					if (rs.next()) {
						role.put("roleId", rs.getInt("role_id"));
						role.put("roleName", rs.getString("role_name"));
//						role = rs.getString("role_name");
					}
					return role;
				}

			};

			Map<Object, Object> role = run.query(conn, querySql, rsHandler);
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
	 * @param deviceToken 
	 * @param deviceId
	 * @throws Exception 
	 */
	public static void updateDeviceStatus(Connection conn, long userId, String deviceToken, int deviceId) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			
			// 更新user_device状态信息
			String updateSql = "update user_device set status = 0"
					+ " where user_id = " + userId
					+ " or device_token = '" + deviceToken + "'";
					
	
			run.update(conn, updateSql);
			
			updateSql = "update user_device set status = 1"
					+ " where device_id = " + deviceId;
					
	
			run.update(conn, updateSql);
			
			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询userDevice，原因为:"+e.getMessage(),e);
		}
		
	}

	/**
	 * @param conn
	 * @return
	 * @throws Exception 
	 */
	public static int getUserDeviceId(Connection conn) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();

			// 获取device_id
			String querySql = "select USER_DEVICE_SEQ.NEXTVAL as deviceId from dual";

			int deviceId = Integer.valueOf(run
						.query(conn, querySql, new MapHandler()).get("deviceId")
						.toString());
			return deviceId;
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询userDevice，原因为:"+e.getMessage(),e);
		}
	}

	/**
	 * @param conn
	 * @param userId
	 * @param userDevice
	 * @throws Exception 
	 */
	public static void mergeUserDevice(Connection conn, long userId, UserDevice userDevice) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();

			String updateSql = "";
			String insertSql = "(UD.DEVICE_ID,UD.USER_ID";
			String values = "VALUES (" + userDevice.getDeviceId() + "," + userId;
			
			if (userDevice!=null&&userDevice.getDeviceToken()!=null && StringUtils.isNotEmpty(userDevice.getDeviceToken().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " DEVICE_TOKEN= " + "'" + userDevice.getDeviceToken() + "'";
				insertSql += ",DEVICE_TOKEN";
				values += ",'" + userDevice.getDeviceToken() + "'";
			};
			if (userDevice!=null&&userDevice.getDevicePlatform()!=null && StringUtils.isNotEmpty(userDevice.getDevicePlatform().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " DEVICE_PLATFORM= " + "'" + userDevice.getDevicePlatform() + "'";
				insertSql += ",DEVICE_PLATFORM";
				values += ",'" + userDevice.getDevicePlatform() + "'";
			};
			if (userDevice!=null&&userDevice.getDeviceVersion()!=null && StringUtils.isNotEmpty(userDevice.getDeviceVersion().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " DEVICE_VERSION= " + "'" + userDevice.getDeviceVersion() + "'";
				insertSql += ",DEVICE_VERSION";
				values += ",'" + userDevice.getDeviceVersion() + "'";
			};
			if (userDevice!=null&&userDevice.getDeviceModel()!=null && StringUtils.isNotEmpty(userDevice.getDeviceModel().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " DEVICE_MODEL= " + "'" + userDevice.getDeviceModel() + "'";
				insertSql += ",DEVICE_MODEL";
				values += ",'" + userDevice.getDeviceModel() + "'";
			};
			if (userDevice!=null&&userDevice.getDeviceSystemVersion()!=null && StringUtils.isNotEmpty(userDevice.getDeviceSystemVersion().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " DEVICE_SYSTEM_VERSION= " + "'" + userDevice.getDeviceSystemVersion() + "'";
				insertSql += ",DEVICE_SYSTEM_VERSION";
				values += ",'" + userDevice.getDeviceSystemVersion() + "'";
			};
			if (userDevice!=null&&userDevice.getDeviceDescendantVersion()!=null && StringUtils.isNotEmpty(userDevice.getDeviceDescendantVersion().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " DEVICE_DESCENDANT_VERSION= " + "'" + userDevice.getDeviceDescendantVersion() + "'";
				insertSql += ",DEVICE_DESCENDANT_VERSION";
				values += ",'" + userDevice.getDeviceDescendantVersion() + "'";
			};

			// 插入user_device
			String sql = "MERGE INTO USER_DEVICE UD"
					+ " USING (SELECT DISTINCT " + userDevice.getDeviceId()
					+ " AS DEVICE_ID FROM USER_DEVICE) TEMP"
					+ " ON (UD.DEVICE_ID = TEMP.DEVICE_ID)"
					+ " WHEN MATCHED THEN UPDATE SET " + updateSql + " WHERE UD.DEVICE_ID=" + userDevice.getDeviceId()
					+ " WHEN NOT MATCHED THEN INSERT " + insertSql + ") " + values + ")" ;

			run.update(conn, sql);

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
	public static void mergeUserUpload(Connection conn, long userId, int deviceId) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();

			// 插入user_device
			String sql = "MERGE INTO USER_UPLOAD UU"
					+ " USING (SELECT DISTINCT " + userId + " AS USER_ID"
					+ ", " + deviceId + " AS DEVICE_ID FROM USER_UPLOAD U_U) TEMP "
					+ " ON (UU.USER_ID = TEMP.USER_ID AND UU.DEVICE_ID = TEMP.DEVICE_ID)"
					+ " WHEN NOT MATCHED THEN INSERT (UU.USER_ID, UU.DEVICE_ID) VALUES (" + userId + "," + deviceId+")";

			run.update(conn, sql);

		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("插入userDevice，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * @param conn
	 * @param user_info
	 * @return
	 * @throws Exception 
	 */
	public static Map<Object, Object> getUserGroup(Connection conn, UserInfo userInfo) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			
			// 查询用户组信息
			String querySql = "SELECT UG.GROUP_ID,UG.GROUP_NAME,UG.GROUP_TYPE"
					+ " FROM USER_GROUP UG,GROUP_USER_MAPPING GUM"
					+ " WHERE UG.GROUP_ID = GUM.GROUP_ID"
					+ " AND UG.PARENT_GROUP_ID IS NULL"
					+ " AND GUM.USER_ID = " + userInfo.getUserId();
					
			ResultSetHandler<Map<Object, Object>> rsHandler = new ResultSetHandler<Map<Object, Object>>() {
				public Map<Object, Object> handle(ResultSet rs) throws SQLException {
					Map<Object, Object> group = new HashMap<Object, Object>();
					if (rs.next()) {
						group.put("groupId", rs.getInt("GROUP_ID"));
						group.put("groupName", rs.getString("GROUP_NAME"));
						group.put("groupType", rs.getInt("GROUP_TYPE"));
					}
					return group;
				}
			};

			Map<Object, Object> group = run.query(conn, querySql, rsHandler);
			return group;

		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("插入userDevice，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 查询分配的月编组组长id
	 * @author Han Shaoming
	 * @param conn
	 * @param userId
	 * @param groupType
	 * @return
	 * @throws Exception
	 */
	public static List<Long> getLeaderIdByGroupId(Connection conn, List<Long> groupIdList) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			// 查询用户所在组组长id
			String querySql = "SELECT DISTINCT U.LEADER_ID FROM USER_GROUP U WHERE 1=1 ";
			JSONArray json = new JSONArray();
			if(groupIdList !=null && groupIdList.size()>0){
				for (int i=0;i<groupIdList.size();i++) {
					if(groupIdList.get(i) !=null){
						json.add(groupIdList.get(i));
					}
				}
			}
			String condition = "AND U.GROUP_ID IN("+json.join(",")+")";
			String sql = querySql + condition;
			
			ResultSetHandler<List<Long>> rsh = new ResultSetHandler<List<Long>>() {
				@Override
				public List<Long> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					List<Long> leaderIdList = new ArrayList<Long>();
					while(rs.next()){
						leaderIdList.add(rs.getLong("LEADER_ID"));
					}
					return leaderIdList;
				}
			};
			List<Long> leaderIdList = run.query(conn, sql, rsh);
			return leaderIdList;
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 查询用户信息
	 * @author Han Shaoming
	 * @param conn
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public static Map<String,Object> getUserInfoByUserId(Connection conn, long userId) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			// 查询用户所在组组长id
			String querySql = "SELECT U.USER_ID USER_ID,U.USER_REAL_NAME USER_REAL_NAME,U.USER_EMAIL USER_EMAIL "
					+ "FROM USER_INFO U WHERE USER_ID = ?";
			Object[] params = {userId};		
			ResultSetHandler<Map<String,Object>> rsh = new ResultSetHandler<Map<String,Object>>() {
				@Override
				public Map<String,Object> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					Map<String,Object> map = new HashMap<String, Object>();
					while(rs.next()){
						map.put("userId", rs.getLong("USER_ID"));
						map.put("userRealName", rs.getString("USER_REAL_NAME"));
						map.put("userEmail", rs.getLong("USER_EMAIL"));
					}
					return map;
				}
			};
			Map<String, Object> userInfo = run.query(conn, querySql, params, rsh);
			return userInfo;
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
}
