package com.navinfo.dataservice.engine.limit.user.userinfo;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.limit.glm.model.limit.man.UserInfo;
import com.navinfo.navicommons.database.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class UserInfoOperation {
    private static Logger log = LoggerRepos.getLogger(UserInfoOperation.class);

    public UserInfoOperation() {

    }

    public static List<Integer> getUserListBySql(Connection conn, String sql) throws Exception{
        try{

            QueryRunner run = new QueryRunner();
            ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>() {
                public List<Integer> handle(ResultSet rs) throws SQLException {
                    List<Integer> userIdList = new ArrayList<>();
                    while (rs.next()) {
                        userIdList.add(rs.getInt("USER_ID"));
                    }
                    return userIdList;
                }
            };
            return run.query(conn, sql, rsHandler);

        }catch(Exception e){
            log.error(e.getMessage(), e);
            throw new Exception("查询失败，原因为:"+e.getMessage(),e);
        }

    }
    /**
     * 获取用户信息
     */
    public static Map<Long, UserInfo> getUserInfosBySql(Connection conn, String sql) throws Exception{
        try{

            QueryRunner run = new QueryRunner();
            ResultSetHandler<Map<Long, UserInfo>> rsHandler = new ResultSetHandler<Map<Long, UserInfo>>() {
                public Map<Long, UserInfo> handle(ResultSet rs) throws SQLException {
                    Map<Long, UserInfo> userIdList = new HashMap<Long, UserInfo>();
                    while (rs.next()) {
                        UserInfo userInfo=new UserInfo();
                        userInfo.setUserId(rs.getInt("USER_ID"));
                        userInfo.setUserEmail(rs.getString("USER_EMAIL"));
                        userInfo.setUserRealName(rs.getString("USER_REAL_NAME"));
                        userIdList.put(rs.getLong("USER_ID"),userInfo);
                    }
                    return userIdList;
                }
            };
            return run.query(conn, sql, rsHandler);

        }catch(Exception e){
            log.error(e.getMessage(), e);
            throw new Exception("查询失败，原因为:"+e.getMessage(),e);
        }

    }

    public static HashMap<Object,Object> loginGetUserInfo(Connection conn,UserInfo userInfo) throws Exception{
        try{

            QueryRunner run = new QueryRunner();

            StringBuilder strSb = new StringBuilder("select distinct u.user_id, u.user_real_name, r.role_name ");

            strSb.append(" from user_info u, role r, role_user_mapping rum ");

            strSb.append(" where u.user_id = rum.user_id ");

            strSb.append(" and rum.role_id = r.role_id ");

            strSb.append(" and u.user_nick_name = '");

            strSb.append(userInfo.getUserNickName() );

            strSb.append("'");

            strSb.append("and u.user_password = '");

            strSb.append(userInfo.getUserPassword());

            strSb.append("'");


            ResultSetHandler<HashMap<Object, Object>> rsHandler = new ResultSetHandler<HashMap<Object, Object>>() {
                public HashMap<Object, Object> handle(ResultSet rs) throws SQLException {
                    HashMap<Object, Object> map = new HashMap<>();
                    if (rs.next()) {
                        if (!map.containsKey("userId")) {
                            map.put("userId", rs.getLong("user_id"));
                            map.put("userRealName", rs.getString("user_real_name"));
                            map.put("roleName", rs.getString("role_name"));
                        }
                    }
                    return map;
                }

            };

            return run.query(conn, strSb.toString(), rsHandler);

        }catch(Exception e){
            log.error(e.getMessage(), e);
            throw new Exception("查询失败，原因为:"+e.getMessage(),e);
        }

    }

    /**
     * 获取用户登录信息
     *
     */
    public static UserInfo getUserInfoByNickNameAndPassword(Connection conn, UserInfo userInfo) throws Exception {

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

            return run.query(conn, querySql, rsHandler);


        }catch(Exception e){
            log.error(e.getMessage(), e);
            throw new Exception("查询user_info出错，原因为:"+e.getMessage(),e);
        }
    }

    /**
     * 获取用户角色
     *
     */
    public static Map<Object, Object> getUserRole(Connection conn, UserInfo userInfo) throws Exception {

        try{
            QueryRunner run = new QueryRunner();
//			Map<Object, Object> role = new HashMap<Object, Object>();
//			role.put("roleId", 0);
//			role.put("roleName", "");

            // 插入user_upload
            String querySql = "select r.role_id,r.role_name"
                    + " from role r,role_user_mapping rum"
                    + " where rum.user_id = " + userInfo.getUserId()
                    + " and r.role_id in (3,4,5,6)"
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
     * 查询用户信息
     * @param conn
     * @param userId
     * @return
     * @throws Exception
     */
    public static UserInfo getUserInfoByUserId(Connection conn, long userId) throws Exception {

        try{
            Set<Long> userSet=new HashSet<Long>();
            userSet.add(userId);
            Map<Long, UserInfo> returnMap = getUserInfoByUserId(conn, userSet);
            if(returnMap.containsKey(userId)){return returnMap.get(userId);}
            return null;
        }catch(Exception e){
            log.error(e.getMessage(), e);
            throw new Exception("查询失败，原因为:"+e.getMessage(),e);
        }
    }

    /**
     * 查询用户信息
     * @param conn
     * @param userIdSet
     * @return
     * @throws Exception
     */
    public static Map<Long, UserInfo> getUserInfoByUserId(Connection conn, Set<Long> userIdSet) throws Exception {

        try{
            QueryRunner run = new QueryRunner();
            // 查询用户所在组组长id
            String querySql = "SELECT * FROM USER_INFO U WHERE USER_ID in ("+userIdSet.toString().replace("[", "").replace("]", "")+")";
            //Object[] params = {};
            ResultSetHandler<Map<Long, UserInfo>> rsh = new ResultSetHandler<Map<Long, UserInfo>>() {
                @Override
                public Map<Long, UserInfo> handle(ResultSet rs) throws SQLException {

                    Map<Long, UserInfo> users=new HashMap<Long, UserInfo>();
                    while(rs.next()){
                        UserInfo userInfo = new UserInfo();
                        userInfo.setUserId(rs.getInt("USER_ID"));
                        userInfo.setUserRealName(rs.getString("USER_REAL_NAME"));
                        userInfo.setUserEmail(rs.getString("USER_EMAIL"));

                        users.put(rs.getLong("USER_ID"), userInfo);
                    }
                    return users;
                }
            };
            Map<Long, UserInfo> users = run.query(conn, querySql,  rsh);
            return users;
        }catch(Exception e){
            log.error(e.getMessage(), e);
            throw new Exception("查询失败，原因为:"+e.getMessage(),e);
        }
    }

}
