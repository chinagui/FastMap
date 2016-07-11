package com.navinfo.dataservice.engine.man.userInfo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
	public HashMap<Object, Object> login(UserInfo userInfo, UserDevice userDevice) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			HashMap<Object, Object> result = new HashMap<Object, Object>();

			HashMap<Object, Object> user = UserInfoOperation.loginGetUserInfo(conn, userInfo, userDevice);

			if (user.isEmpty()) {
				return result;
			}

			if ((userDevice.getDeviceToken() != null) && (userDevice.getDevicePlatform() != null) && (userDevice.getDeviceVersion() != null) && (!user.containsKey("deviceId"))) {
				int deviceId = UserInfoOperation.insertIntoUserDevice(conn, (long) user.get("userId"), userDevice);
				user.put("deviceId", deviceId);
			}

			if (!user.isEmpty()) {
				AccessToken access_token = AccessTokenFactory.generate((long) (user.get("userId")));
				if (access_token != null) {
					result.put("access_token", access_token.getTokenString());
					result.put("expires_in", access_token.getTimestamp());
					result.put("role", user.get("roleName"));
					result.put("deviceId", user.get("deviceId"));
					result.put("userId", user.get("userId"));
					result.put("userRealName", user.get("userRealName"));
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

	public List<String> getUploadTime(Integer userId, String deviceId) throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "select upload_poi_time,upload_tips_time from user_upload where user_id=? and device_id=?";

			List<Object> values = new ArrayList<Object>();

			values.add(userId);
			values.add(deviceId);

			ResultSetHandler<List<String>> rsHandler = new ResultSetHandler<List<String>>() {
				public List<String> handle(ResultSet rs) throws SQLException {
					List<String> list = new ArrayList<String>();
					while (rs.next()) {
						Timestamp poi_time = rs.getTimestamp("upload_poi_time");
						Timestamp tips_time = rs.getTimestamp("upload_tips_time");

						list.add(poi_time.toString());
						list.add(tips_time.toString());
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
}
