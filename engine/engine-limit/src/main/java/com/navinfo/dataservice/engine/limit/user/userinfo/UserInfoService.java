package com.navinfo.dataservice.engine.limit.user.userinfo;


import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.token.AccessTokenFactory;
import com.navinfo.dataservice.engine.limit.glm.model.limit.man.UserInfo;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserInfoService {

    private Logger log = LoggerRepos.getLogger(this.getClass());


    private UserInfoService() {
    }

    private static class SingletonHolder {
        private static final UserInfoService INSTANCE = new UserInfoService();
    }

    public static UserInfoService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void create(UserInfo bean) throws ServiceException {
        Connection conn = null;
        try {
            // 持久化
            QueryRunner run = new QueryRunner();
            conn = DBConnector.getInstance().getLimitConnection();

            String createSql = "insert into user_info (USER_ID, USER_REAL_NAME, USER_NICK_NAME, USER_PASSWORD, USER_EMAIL, USER_PHONE) values(USER_INFO_SEQ.NEXTVAL,?,?,?,?,?)";
            run.update(conn, createSql, bean.getUserRealName(), bean.getUserNickName(), bean.getUserPassword(), bean.getUserEmail(), bean.getUserPhone());
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    public void update(UserInfo bean) throws ServiceException {
        Connection conn = null;
        try {
            // 持久化
            QueryRunner run = new QueryRunner();
            conn = DBConnector.getInstance().getLimitConnection();

            String updateSql = "update user_info set ";
            List<Object> values = new ArrayList<>();

            if (bean != null && bean.getUserRealName() != null && StringUtils.isNotEmpty(bean.getUserRealName())) {
                updateSql += " USER_REAL_NAME=? ,";
                values.add(bean.getUserRealName());
            }

            if (bean != null && bean.getUserNickName() != null && StringUtils.isNotEmpty(bean.getUserNickName())) {
                updateSql += " USER_NICK_NAME=? ,";
                values.add(bean.getUserNickName());
            }

            if (bean != null && bean.getUserPassword() != null && StringUtils.isNotEmpty(bean.getUserPassword())) {
                updateSql += " USER_PASSWORD=? ,";
                values.add(bean.getUserPassword());
            }

            if (bean != null && bean.getUserEmail() != null && StringUtils.isNotEmpty(bean.getUserEmail())) {
                updateSql += " USER_EMAIL=? ,";
                values.add(bean.getUserEmail());
            }

            if (bean != null && bean.getUserPhone() != null && StringUtils.isNotEmpty(bean.getUserPhone())) {
                updateSql += " USER_PHONE=? ,";
                values.add(bean.getUserPhone());
            }


            updateSql = updateSql.substring(0, updateSql.length() - 1);
            updateSql += " where user_id = ?";
            values.add(bean.getUserId());

            run.update(conn, updateSql, values.toArray());
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new ServiceException("修改失败，原因为:" + e.getMessage(), e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    public void delete(UserInfo bean) throws ServiceException {
        Connection conn = null;
        try {
            // 持久化
            QueryRunner run = new QueryRunner();
            conn = DBConnector.getInstance().getLimitConnection();

            String deleteSql = "delete from  user_info where 1=1 ";
            List<Object> values = new ArrayList<>();
            if (bean != null && bean.getUserId() != null && StringUtils.isNotEmpty(bean.getUserId().toString())) {
                deleteSql += " and USER_ID=? ";
                values.add(bean.getUserId());
            }

            if (bean != null && bean.getUserRealName() != null && StringUtils.isNotEmpty(bean.getUserRealName())) {
                deleteSql += " and USER_REAL_NAME=? ";
                values.add(bean.getUserRealName());
            }

            if (bean != null && bean.getUserNickName() != null && StringUtils.isNotEmpty(bean.getUserNickName())) {
                deleteSql += " and USER_NICK_NAME=? ";
                values.add(bean.getUserNickName());
            }

            if (bean != null && bean.getUserPassword() != null && StringUtils.isNotEmpty(bean.getUserPassword())) {
                deleteSql += " and USER_PASSWORD=? ";
                values.add(bean.getUserPassword());
            }

            if (bean != null && bean.getUserEmail() != null && StringUtils.isNotEmpty(bean.getUserEmail())) {
                deleteSql += " and USER_EMAIL=? ";
                values.add(bean.getUserEmail());
            }

            if (bean != null && bean.getUserPhone() != null && StringUtils.isNotEmpty(bean.getUserPhone())) {
                deleteSql += " and USER_PHONE=? ";
                values.add(bean.getUserPhone());
            }


            if (values.size() == 0) {
                run.update(conn, deleteSql);
            } else {
                run.update(conn, deleteSql, values.toArray());
            }

        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new ServiceException("删除失败，原因为:" + e.getMessage(), e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    public Page list(UserInfo bean, final int currentPageNum) throws ServiceException {
        Connection conn = null;
        try {
            QueryRunner run = new QueryRunner();
            conn = DBConnector.getInstance().getLimitConnection();

            String selectSql = "select * from user_info where 1=1 ";
            List<Object> values = new ArrayList<>();
            if (bean != null && bean.getUserId() != null && StringUtils.isNotEmpty(bean.getUserId().toString())) {
                selectSql += " and USER_ID=? ";
                values.add(bean.getUserId());
            }

            if (bean != null && bean.getUserRealName() != null && StringUtils.isNotEmpty(bean.getUserRealName())) {
                selectSql += " and USER_REAL_NAME=? ";
                values.add(bean.getUserRealName());
            }

            if (bean != null && bean.getUserNickName() != null && StringUtils.isNotEmpty(bean.getUserNickName())) {
                selectSql += " and USER_NICK_NAME=? ";
                values.add(bean.getUserNickName());
            }

            if (bean != null && bean.getUserPassword() != null && StringUtils.isNotEmpty(bean.getUserPassword())) {
                selectSql += " and USER_PASSWORD=? ";
                values.add(bean.getUserPassword());
            }

            if (bean != null && bean.getUserEmail() != null && StringUtils.isNotEmpty(bean.getUserEmail())) {
                selectSql += " and USER_EMAIL=? ";
                values.add(bean.getUserEmail());
            }

            if (bean != null && bean.getUserPhone() != null && StringUtils.isNotEmpty(bean.getUserPhone())) {
                selectSql += " and USER_PHONE=? ";
                values.add(bean.getUserPhone());
            }


            ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
                public Page handle(ResultSet rs) throws SQLException {
                    List<UserInfo> list = new ArrayList<>();
                    Page page = new Page(currentPageNum);
                    while (rs.next()) {
                        UserInfo model = new UserInfo();
                        page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
                        model.setUserId(rs.getInt("USER_ID"));
                        model.setUserRealName(rs.getString("USER_REAL_NAME"));
                        model.setUserNickName(rs.getString("USER_NICK_NAME"));
                        model.setUserPassword(rs.getString("USER_PASSWORD"));
                        model.setUserEmail(rs.getString("USER_EMAIL"));
                        model.setUserPhone(rs.getString("USER_PHONE"));

                        list.add(model);
                    }
                    page.setResult(list);
                    return page;
                }

            };
            if (values.size() == 0) {
                return run.query(currentPageNum, 20, conn, selectSql, rsHandler);
            }
            return run.query(currentPageNum, 20, conn, selectSql, rsHandler, values.toArray());
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }

    }

    public UserInfo query(UserInfo bean) throws ServiceException {
        Connection conn = null;
        try {
            // 持久化
            QueryRunner run = new QueryRunner();
            conn = DBConnector.getInstance().getLimitConnection();

            String selectSql = "select * from user_info where 1=1 ";
            List<Object> values = new ArrayList<>();
            if (bean != null && bean.getUserId() != null && StringUtils.isNotEmpty(bean.getUserId().toString())) {
                selectSql += " and USER_ID=? ";
                values.add(bean.getUserId());
            }

            if (bean != null && bean.getUserRealName() != null && StringUtils.isNotEmpty(bean.getUserRealName())) {
                selectSql += " and USER_REAL_NAME=? ";
                values.add(bean.getUserRealName());
            }

            if (bean != null && bean.getUserNickName() != null && StringUtils.isNotEmpty(bean.getUserNickName())) {
                selectSql += " and USER_NICK_NAME=? ";
                values.add(bean.getUserNickName());
            }

            if (bean != null && bean.getUserPassword() != null && StringUtils.isNotEmpty(bean.getUserPassword())) {
                selectSql += " and USER_PASSWORD=? ";
                values.add(bean.getUserPassword());
            }

            if (bean != null && bean.getUserEmail() != null && StringUtils.isNotEmpty(bean.getUserEmail())) {
                selectSql += " and USER_EMAIL=? ";
                values.add(bean.getUserEmail());
            }

            if (bean != null && bean.getUserPhone() != null && StringUtils.isNotEmpty(bean.getUserPhone())) {
                selectSql += " and USER_PHONE=? ";
                values.add(bean.getUserPhone());
            }


            selectSql += " and rownum=1";
            ResultSetHandler<UserInfo> rsHandler = new ResultSetHandler<UserInfo>() {
                public UserInfo handle(ResultSet rs) throws SQLException {
                    if (rs.next()) {
                        UserInfo model = new UserInfo();
                        model.setUserId(rs.getInt("USER_ID"));
                        model.setUserRealName(rs.getString("USER_REAL_NAME"));
                        model.setUserNickName(rs.getString("USER_NICK_NAME"));
                        model.setUserEmail(rs.getString("USER_EMAIL"));
                        model.setUserPhone(rs.getString("USER_PHONE"));

                        return model;
                    }
                    return null;
                }

            };
            if (values.size() == 0) {
                return run.query(conn, selectSql, rsHandler);
            }
            return run.query(conn, selectSql, rsHandler, values.toArray());
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    public UserInfo queryUserInfoByUserId(int userId) throws Exception {
        UserInfo selectUser = new UserInfo();
        selectUser.setUserId( userId);

        return this.query(selectUser);
    }

    /**
     * 登录
     * @param userInfo 用户信息
     */
    public HashMap<Object, Object> login(UserInfo userInfo) throws ServiceException {

        Connection conn = null;
        try {
            conn = DBConnector.getInstance().getLimitConnection();
            HashMap<Object, Object> result = new HashMap<>();

            //根据userInfo获取用户信息
            UserInfo user_info = UserInfoOperation.getUserInfoByNickNameAndPassword(conn, userInfo);
            if (user_info.getUserId() == null) {
                return result;
            }

            //查询角色信息
            //Map<Object, Object> role = UserInfoOperation.getUserRole(conn, user_info);



            //生成access_token
            AccessToken access_token = AccessTokenFactory.generate((long) (user_info.getUserId()));
            if (access_token != null) {
                result.put("access_token", access_token.getTokenString());
                result.put("expires_in", access_token.getTimestamp());
//
//                if (!role.isEmpty()) {
//                    result.put("roleId", role.get("roleId"));
//                    result.put("roleName", role.get("roleName"));
//                }

                result.put("userId", user_info.getUserId());
                result.put("userRealName", user_info.getUserRealName());
            }
            return result;

        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new ServiceException("登录失败，原因为:" + e.getMessage(), e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }



    /**
     * 查询user数据
     *
     */
    public UserInfo getUserInfoByUserId(long userId) throws ServiceException {
        Connection conn = null;
        try {
            conn = DBConnector.getInstance().getLimitConnection();
            return UserInfoOperation.getUserInfoByUserId(conn, userId);

        } catch (Exception e) {

            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }


    /**
     * 获取所有用户信息
     */
    public Map<Integer, String> getUsers() throws Exception {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            conn = DBConnector.getInstance().getLimitConnection();
            String selectSql = "select u.user_id,u.user_real_name from user_info u ";

            pstmt = conn.prepareStatement(selectSql);
            resultSet = pstmt.executeQuery();
            Map<Integer, String> result = new HashMap<>();

            while (resultSet.next()) {
                result.put(resultSet.getInt("user_id"), resultSet.getString("user_real_name"));
            }

            return result;
        } catch (Exception e) {

            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new ServiceException("查询users，原因为:" + e.getMessage(), e);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
            DbUtils.commitAndCloseQuietly(conn);
        }
    }
}
