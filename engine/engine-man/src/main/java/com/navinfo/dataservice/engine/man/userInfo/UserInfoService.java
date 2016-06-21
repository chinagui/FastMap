package com.navinfo.dataservice.engine.man.userInfo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.man.model.UserDevice;
import com.navinfo.dataservice.api.man.model.UserGroup;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.token.AccessTokenFactory;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

/**
 * @ClassName: UserInfoService
 * @author code generator
 * @date 2016-06-14 03:24:34
 * @Description: TODO
 */
@Service
public class UserInfoService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	public void create(UserInfo bean) throws ServiceException {
		Connection conn = null;
		try {
			// 持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();

			String createSql = "insert into user_info (USER_ID, USER_REAL_NAME, USER_NICK_NAME, USER_PASSWORD, USER_EMAIL, USER_PHONE, USER_LEVEL, USER_SCORE, USER_ICON, USER_GPSID) values(?,?,?,?,?,?,?,?,?,?)";
			run.update(conn, createSql, bean.getUserId(), bean.getUserRealName(), bean.getUserNickName(), bean.getUserPassword(), bean.getUserEmail(), bean.getUserPhone(),
					bean.getUserLevel(), bean.getUserScore(), bean.getUserIcon(), bean.getUserGpsid());
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
			conn = DBConnector.getInstance().getManConnection();

			String updateSql = "update user_info set ";
			List<Object> values = new ArrayList<Object>();

			if (bean != null && bean.getUserRealName() != null && StringUtils.isNotEmpty(bean.getUserRealName().toString())) {
				updateSql += " USER_REAL_NAME=? ,";
				values.add(bean.getUserRealName());
			}
			;
			if (bean != null && bean.getUserNickName() != null && StringUtils.isNotEmpty(bean.getUserNickName().toString())) {
				updateSql += " USER_NICK_NAME=? ,";
				values.add(bean.getUserNickName());
			}
			;
			if (bean != null && bean.getUserPassword() != null && StringUtils.isNotEmpty(bean.getUserPassword().toString())) {
				updateSql += " USER_PASSWORD=? ,";
				values.add(bean.getUserPassword());
			}
			;
			if (bean != null && bean.getUserEmail() != null && StringUtils.isNotEmpty(bean.getUserEmail().toString())) {
				updateSql += " USER_EMAIL=? ,";
				values.add(bean.getUserEmail());
			}
			;
			if (bean != null && bean.getUserPhone() != null && StringUtils.isNotEmpty(bean.getUserPhone().toString())) {
				updateSql += " USER_PHONE=? ,";
				values.add(bean.getUserPhone());
			}
			;
			if (bean != null && bean.getUserLevel() != null && StringUtils.isNotEmpty(bean.getUserLevel().toString())) {
				updateSql += " USER_LEVEL=? ,";
				values.add(bean.getUserLevel());
			}
			;
			if (bean != null && bean.getUserScore() != null && StringUtils.isNotEmpty(bean.getUserScore().toString())) {
				updateSql += " USER_SCORE=? ,";
				values.add(bean.getUserScore());
			}
			;
			if (bean != null && bean.getUserIcon() != null && StringUtils.isNotEmpty(bean.getUserIcon().toString())) {
				updateSql += " USER_ICON=? ,";
				values.add(bean.getUserIcon());
			}
			;
			if (bean != null && bean.getUserGpsid() != null && StringUtils.isNotEmpty(bean.getUserGpsid().toString())) {
				updateSql += " USER_GPSID=? ,";
				values.add(bean.getUserGpsid());
			}
			;

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
			conn = DBConnector.getInstance().getManConnection();

			String deleteSql = "delete from  user_info where 1=1 ";
			List<Object> values = new ArrayList<Object>();
			if (bean != null && bean.getUserId() != null && StringUtils.isNotEmpty(bean.getUserId().toString())) {
				deleteSql += " and USER_ID=? ";
				values.add(bean.getUserId());
			}
			;
			if (bean != null && bean.getUserRealName() != null && StringUtils.isNotEmpty(bean.getUserRealName().toString())) {
				deleteSql += " and USER_REAL_NAME=? ";
				values.add(bean.getUserRealName());
			}
			;
			if (bean != null && bean.getUserNickName() != null && StringUtils.isNotEmpty(bean.getUserNickName().toString())) {
				deleteSql += " and USER_NICK_NAME=? ";
				values.add(bean.getUserNickName());
			}
			;
			if (bean != null && bean.getUserPassword() != null && StringUtils.isNotEmpty(bean.getUserPassword().toString())) {
				deleteSql += " and USER_PASSWORD=? ";
				values.add(bean.getUserPassword());
			}
			;
			if (bean != null && bean.getUserEmail() != null && StringUtils.isNotEmpty(bean.getUserEmail().toString())) {
				deleteSql += " and USER_EMAIL=? ";
				values.add(bean.getUserEmail());
			}
			;
			if (bean != null && bean.getUserPhone() != null && StringUtils.isNotEmpty(bean.getUserPhone().toString())) {
				deleteSql += " and USER_PHONE=? ";
				values.add(bean.getUserPhone());
			}
			;
			if (bean != null && bean.getUserLevel() != null && StringUtils.isNotEmpty(bean.getUserLevel().toString())) {
				deleteSql += " and USER_LEVEL=? ";
				values.add(bean.getUserLevel());
			}
			;
			if (bean != null && bean.getUserScore() != null && StringUtils.isNotEmpty(bean.getUserScore().toString())) {
				deleteSql += " and USER_SCORE=? ";
				values.add(bean.getUserScore());
			}
			;
			if (bean != null && bean.getUserIcon() != null && StringUtils.isNotEmpty(bean.getUserIcon().toString())) {
				deleteSql += " and USER_ICON=? ";
				values.add(bean.getUserIcon());
			}
			;
			if (bean != null && bean.getUserGpsid() != null && StringUtils.isNotEmpty(bean.getUserGpsid().toString())) {
				deleteSql += " and USER_GPSID=? ";
				values.add(bean.getUserGpsid());
			}
			;
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
			conn = DBConnector.getInstance().getManConnection();

			String selectSql = "select * from user_info where 1=1 ";
			List<Object> values = new ArrayList<Object>();
			if (bean != null && bean.getUserId() != null && StringUtils.isNotEmpty(bean.getUserId().toString())) {
				selectSql += " and USER_ID=? ";
				values.add(bean.getUserId());
			}
			;
			if (bean != null && bean.getUserRealName() != null && StringUtils.isNotEmpty(bean.getUserRealName().toString())) {
				selectSql += " and USER_REAL_NAME=? ";
				values.add(bean.getUserRealName());
			}
			;
			if (bean != null && bean.getUserNickName() != null && StringUtils.isNotEmpty(bean.getUserNickName().toString())) {
				selectSql += " and USER_NICK_NAME=? ";
				values.add(bean.getUserNickName());
			}
			;
			if (bean != null && bean.getUserPassword() != null && StringUtils.isNotEmpty(bean.getUserPassword().toString())) {
				selectSql += " and USER_PASSWORD=? ";
				values.add(bean.getUserPassword());
			}
			;
			if (bean != null && bean.getUserEmail() != null && StringUtils.isNotEmpty(bean.getUserEmail().toString())) {
				selectSql += " and USER_EMAIL=? ";
				values.add(bean.getUserEmail());
			}
			;
			if (bean != null && bean.getUserPhone() != null && StringUtils.isNotEmpty(bean.getUserPhone().toString())) {
				selectSql += " and USER_PHONE=? ";
				values.add(bean.getUserPhone());
			}
			;
			if (bean != null && bean.getUserLevel() != null && StringUtils.isNotEmpty(bean.getUserLevel().toString())) {
				selectSql += " and USER_LEVEL=? ";
				values.add(bean.getUserLevel());
			}
			;
			if (bean != null && bean.getUserScore() != null && StringUtils.isNotEmpty(bean.getUserScore().toString())) {
				selectSql += " and USER_SCORE=? ";
				values.add(bean.getUserScore());
			}
			;
			if (bean != null && bean.getUserIcon() != null && StringUtils.isNotEmpty(bean.getUserIcon().toString())) {
				selectSql += " and USER_ICON=? ";
				values.add(bean.getUserIcon());
			}
			;
			if (bean != null && bean.getUserGpsid() != null && StringUtils.isNotEmpty(bean.getUserGpsid().toString())) {
				selectSql += " and USER_GPSID=? ";
				values.add(bean.getUserGpsid());
			}
			;
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					List<UserInfo> list = new ArrayList<UserInfo>();
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
						model.setUserLevel(rs.getInt("USER_LEVEL"));
						model.setUserScore(rs.getInt("USER_SCORE"));
						model.setUserIcon(rs.getObject("USER_ICON"));
						model.setUserGpsid(rs.getString("USER_GPSID"));
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

	public List<UserInfo> list(UserGroup bean) throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "select us.* from user_info us,group_user_mapping gum where us.user_id=gum.user_id and gum.group_id=? ";

			List<Object> values = new ArrayList<Object>();

			values.add(bean.getGroupId());

			ResultSetHandler<List<UserInfo>> rsHandler = new ResultSetHandler<List<UserInfo>>() {
				public List<UserInfo> handle(ResultSet rs) throws SQLException {
					List<UserInfo> list = new ArrayList<UserInfo>();
					while (rs.next()) {
						UserInfo model = new UserInfo();
						model.setUserId(rs.getInt("USER_ID"));
						model.setUserRealName(rs.getString("USER_REAL_NAME"));
						model.setUserNickName(rs.getString("USER_NICK_NAME"));
						model.setUserPassword(rs.getString("USER_PASSWORD"));
						model.setUserEmail(rs.getString("USER_EMAIL"));
						model.setUserPhone(rs.getString("USER_PHONE"));
						model.setUserLevel(rs.getInt("USER_LEVEL"));
						model.setUserScore(rs.getInt("USER_SCORE"));
						model.setUserIcon(rs.getObject("USER_ICON"));
						model.setUserGpsid(rs.getString("USER_GPSID"));
						list.add(model);
					}
					return list;
				}

			};
			return run.query(conn, selectSql, rsHandler, values.toArray());
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
			conn = DBConnector.getInstance().getManConnection();

			String selectSql = "select * from user_info where 1=1 ";
			List<Object> values = new ArrayList<Object>();
			if (bean != null && bean.getUserId() != null && StringUtils.isNotEmpty(bean.getUserId().toString())) {
				selectSql += " and USER_ID=? ";
				values.add(bean.getUserId());
			}
			;
			if (bean != null && bean.getUserRealName() != null && StringUtils.isNotEmpty(bean.getUserRealName().toString())) {
				selectSql += " and USER_REAL_NAME=? ";
				values.add(bean.getUserRealName());
			}
			;
			if (bean != null && bean.getUserNickName() != null && StringUtils.isNotEmpty(bean.getUserNickName().toString())) {
				selectSql += " and USER_NICK_NAME=? ";
				values.add(bean.getUserNickName());
			}
			;
			if (bean != null && bean.getUserPassword() != null && StringUtils.isNotEmpty(bean.getUserPassword().toString())) {
				selectSql += " and USER_PASSWORD=? ";
				values.add(bean.getUserPassword());
			}
			;
			if (bean != null && bean.getUserEmail() != null && StringUtils.isNotEmpty(bean.getUserEmail().toString())) {
				selectSql += " and USER_EMAIL=? ";
				values.add(bean.getUserEmail());
			}
			;
			if (bean != null && bean.getUserPhone() != null && StringUtils.isNotEmpty(bean.getUserPhone().toString())) {
				selectSql += " and USER_PHONE=? ";
				values.add(bean.getUserPhone());
			}
			;
			if (bean != null && bean.getUserLevel() != null && StringUtils.isNotEmpty(bean.getUserLevel().toString())) {
				selectSql += " and USER_LEVEL=? ";
				values.add(bean.getUserLevel());
			}
			;
			if (bean != null && bean.getUserScore() != null && StringUtils.isNotEmpty(bean.getUserScore().toString())) {
				selectSql += " and USER_SCORE=? ";
				values.add(bean.getUserScore());
			}
			;
			if (bean != null && bean.getUserIcon() != null && StringUtils.isNotEmpty(bean.getUserIcon().toString())) {
				selectSql += " and USER_ICON=? ";
				values.add(bean.getUserIcon());
			}
			;
			if (bean != null && bean.getUserGpsid() != null && StringUtils.isNotEmpty(bean.getUserGpsid().toString())) {
				selectSql += " and USER_GPSID=? ";
				values.add(bean.getUserGpsid());
			}
			;
			selectSql += " and rownum=1";
			ResultSetHandler<UserInfo> rsHandler = new ResultSetHandler<UserInfo>() {
				public UserInfo handle(ResultSet rs) throws SQLException {
					while (rs.next()) {
						UserInfo model = new UserInfo();
						model.setUserId(rs.getInt("USER_ID"));
						model.setUserRealName(rs.getString("USER_REAL_NAME"));
						model.setUserNickName(rs.getString("USER_NICK_NAME"));
						model.setUserEmail(rs.getString("USER_EMAIL"));
						model.setUserPhone(rs.getString("USER_PHONE"));
						model.setUserLevel(rs.getInt("USER_LEVEL"));
						model.setUserScore(rs.getInt("USER_SCORE"));
						model.setUserIcon(rs.getObject("USER_ICON"));
						model.setUserGpsid(rs.getString("USER_GPSID"));
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

	/**
	 * @param userInfo
	 * @param userDevice
	 * @throws ServiceException
	 */
	public HashMap login(UserInfo userInfo, UserDevice userDevice) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			// 持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();

			String selectSql = "";

			if (userDevice != null) {
				selectSql = "select u.user_id ,r.role_name " + " from user_info u,role r,role_user_mapping rum,user_device d " + " where u.user_id=d.user_id "
						+ " and u.user_id = rum.user_id " + " and rum.role_id = r.role_id ";
			} else {
				selectSql = "select u.user_id ,r.role_name " + " from user_info u,role r,role_user_mapping rum " + " where u.user_id = rum.user_id "
						+ " and rum.role_id = r.role_id ";
			}

			selectSql += " and u.user_nick_name = '" + userInfo.getUserNickName() + "'";
			selectSql += " and u.user_password = '" + userInfo.getUserPassword() + "'";

			if (userDevice != null && userDevice.getDeviceToken() != null && StringUtils.isNotEmpty(userDevice.getDeviceToken().toString())) {
				selectSql += " and d.device_token = '" + userDevice.getDeviceToken() + "'";
			}
			if (userDevice != null && userDevice.getDevicePlatform() != null && StringUtils.isNotEmpty(userDevice.getDevicePlatform().toString())) {
				selectSql += " and d.device_platform = '" + userDevice.getDevicePlatform() + "'";
			}
			if (userDevice != null && userDevice.getDeviceVersion() != null && StringUtils.isNotEmpty(userDevice.getDeviceVersion().toString())) {
				selectSql += " and d.device_version = '" + userDevice.getDeviceVersion() + "'";
			}

			ResultSetHandler<HashMap> rsHandler = new ResultSetHandler<HashMap>() {
				public HashMap handle(ResultSet rs) throws SQLException {
					HashMap map = new HashMap();
					while (rs.next()) {
						map.put("userId", rs.getLong("user_id"));
						map.put("roleName", rs.getString("role_name"));
						return map;
					}
					return map;
				}

			};

			HashMap map = run.query(conn, selectSql, rsHandler);
			HashMap result = new HashMap();

			if (!map.isEmpty()) {
				AccessToken access_token = AccessTokenFactory.generate((long) (map.get("userId")));
				if (access_token != null) {
					result.put("access_token", access_token.getTokenString());
					result.put("expires_in", access_token.getExpireSecond());
					result.put("role", map.get("role"));
				}
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

}
