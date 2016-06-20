package com.navinfo.dataservice.engine.man.region;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

/**
 * @ClassName: RegionService
 * @author code generator
 * @date 2016-06-08 02:32:17
 * @Description: TODO
 */
public class RegionService {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private RegionService(){}
	private static class SingletonHolder{
		private static final RegionService INSTANCE =new RegionService();
	}
	public static RegionService getInstance(){
		return SingletonHolder.INSTANCE;
	}

	public void create(JSONObject json) throws ServiceException {
		Connection conn = null;
		try {
			// 持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			Region bean = (Region) JSONObject.toBean(json, Region.class);

			String createSql = "insert into Region (REGION_ID, REGION_NAME, DAILY_DB_ID, MONTHLY_DB_ID) values(?,?,?,?)";
			run.update(conn, createSql, bean.getRegionId(),
					bean.getRegionName(), bean.getDailyDbId(),
					bean.getMonthlyDbId());
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public void update(JSONObject json) throws ServiceException {
		Connection conn = null;
		try {
			// 持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			JSONObject obj = JSONObject.fromObject(json);
			Region bean = (Region) JSONObject.toBean(obj, Region.class);

			String updateSql = "update Region set REGION_ID=?, REGION_NAME=?, DAILY_DB_ID=?, MONTHLY_DB_ID=? where REGION_ID=?";
			List<Object> values = new ArrayList();
			if (bean != null && bean.getRegionId() != null
					&& StringUtils.isNotEmpty(bean.getRegionId().toString())) {
				updateSql += " and REGION_ID=? ";
				values.add(bean.getRegionId());
			}
			;
			if (bean != null && bean.getRegionName() != null
					&& StringUtils.isNotEmpty(bean.getRegionName().toString())) {
				updateSql += " and REGION_NAME=? ";
				values.add(bean.getRegionName());
			}
			;
			if (bean != null && bean.getDailyDbId() != null
					&& StringUtils.isNotEmpty(bean.getDailyDbId().toString())) {
				updateSql += " and DAILY_DB_ID=? ";
				values.add(bean.getDailyDbId());
			}
			;
			if (bean != null && bean.getMonthlyDbId() != null
					&& StringUtils.isNotEmpty(bean.getMonthlyDbId().toString())) {
				updateSql += " and MONTHLY_DB_ID=? ";
				values.add(bean.getMonthlyDbId());
			}
			
			values.add(bean.getRegionId());
			
			run.update(conn, updateSql, values.toArray());
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("修改失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public void delete(JSONObject json) throws ServiceException {
		Connection conn = null;
		try {
			// 持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			JSONObject obj = JSONObject.fromObject(json);
			Region bean = (Region) JSONObject.toBean(obj, Region.class);

			String deleteSql = "delete from  Region where 1=1 ";
			List<Object> values = new ArrayList();
			if (bean != null && bean.getRegionId() != null
					&& StringUtils.isNotEmpty(bean.getRegionId().toString())) {
				deleteSql += " and REGION_ID=? ";
				values.add(bean.getRegionId());
			}
			;
			if (bean != null && bean.getRegionName() != null
					&& StringUtils.isNotEmpty(bean.getRegionName().toString())) {
				deleteSql += " and REGION_NAME=? ";
				values.add(bean.getRegionName());
			}
			;
			if (bean != null && bean.getDailyDbId() != null
					&& StringUtils.isNotEmpty(bean.getDailyDbId().toString())) {
				deleteSql += " and DAILY_DB_ID=? ";
				values.add(bean.getDailyDbId());
			}
			;
			if (bean != null && bean.getMonthlyDbId() != null
					&& StringUtils.isNotEmpty(bean.getMonthlyDbId().toString())) {
				deleteSql += " and MONTHLY_DB_ID=? ";
				values.add(bean.getMonthlyDbId());
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

	public Page list(JSONObject json, final int currentPageNum)
			throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			JSONObject obj = JSONObject.fromObject(json);
			Region bean = (Region) JSONObject.toBean(obj, Region.class);

			String selectSql = "select * from Region where 1=1 ";
			List<Object> values = new ArrayList();
			if (bean != null && bean.getRegionId() != null
					&& StringUtils.isNotEmpty(bean.getRegionId().toString())) {
				selectSql += " and REGION_ID=? ";
				values.add(bean.getRegionId());
			}
			;
			if (bean != null && bean.getRegionName() != null
					&& StringUtils.isNotEmpty(bean.getRegionName().toString())) {
				selectSql += " and REGION_NAME=? ";
				values.add(bean.getRegionName());
			}
			;
			if (bean != null && bean.getDailyDbId() != null
					&& StringUtils.isNotEmpty(bean.getDailyDbId().toString())) {
				selectSql += " and DAILY_DB_ID=? ";
				values.add(bean.getDailyDbId());
			}
			;
			if (bean != null && bean.getMonthlyDbId() != null
					&& StringUtils.isNotEmpty(bean.getMonthlyDbId().toString())) {
				selectSql += " and MONTHLY_DB_ID=? ";
				values.add(bean.getMonthlyDbId());
			}
			;
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					List list = new ArrayList();
					Page page = new Page(currentPageNum);
					while (rs.next()) {
						HashMap map = new HashMap();
						page.setTotalCount(rs
								.getInt(QueryRunner.TOTAL_RECORD_NUM));
						map.put("regionId", rs.getInt("REGION_ID"));
						map.put("regionName", rs.getString("REGION_NAME"));
						map.put("dailyDbId", rs.getInt("DAILY_DB_ID"));
						map.put("monthlyDbId", rs.getInt("MONTHLY_DB_ID"));
						list.add(map);
					}
					page.setResult(list);
					return page;
				}

			};
			if (values.size() == 0) {
				return run
						.query(currentPageNum, 20, conn, selectSql, rsHandler);
			}
			return run.query(currentPageNum, 20, conn, selectSql, rsHandler,
					values.toArray());
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}

	}
	public List<Region> list() throws Exception{
		return list(null);
	}
	public List<Region> list(Region bean) throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();

			String selectSql = "select * from Region where 1=1 ";
			List<Object> values = new ArrayList();
			if (bean != null && bean.getRegionId() != null
					&& StringUtils.isNotEmpty(bean.getRegionId().toString())) {
				selectSql += " and REGION_ID=? ";
				values.add(bean.getRegionId());
			}
			;
			if (bean != null && bean.getRegionName() != null
					&& StringUtils.isNotEmpty(bean.getRegionName().toString())) {
				selectSql += " and REGION_NAME=? ";
				values.add(bean.getRegionName());
			}
			;
			if (bean != null && bean.getDailyDbId() != null
					&& StringUtils.isNotEmpty(bean.getDailyDbId().toString())) {
				selectSql += " and DAILY_DB_ID=? ";
				values.add(bean.getDailyDbId());
			}
			;
			if (bean != null && bean.getMonthlyDbId() != null
					&& StringUtils.isNotEmpty(bean.getMonthlyDbId().toString())) {
				selectSql += " and MONTHLY_DB_ID=? ";
				values.add(bean.getMonthlyDbId());
			}
			;
			ResultSetHandler<List<Region>> rsHandler = new ResultSetHandler<List<Region>>() {
				public List<Region> handle(ResultSet rs) throws SQLException {
					List<Region> list = new ArrayList<Region>();
					while (rs.next()) {
						Region region = new Region();
						region.setRegionId(rs.getInt("REGION_ID"));
						region.setRegionName(rs.getString("REGION_NAME"));
						region.setDailyDbId(rs.getInt("DAILY_DB_ID"));
						region.setMonthlyDbId(rs.getInt("MONTHLY_DB_ID"));
						list.add(region);
					}
					return list;
				}

			};
			if (values.size() == 0) {
				return run.query(conn, selectSql, rsHandler);
			}
			return run.query(conn, selectSql, rsHandler, values.toArray());
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public Region query(Region bean ) throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "select * from Region where REGION_ID=?";
			ResultSetHandler<Region> rsHandler = new ResultSetHandler<Region>() {
				public Region handle(ResultSet rs) throws SQLException {
					while (rs.next()) {
						Region region = new Region();
						region.setRegionId(rs.getInt("REGION_ID"));
						region.setRegionName(rs.getString("REGION_NAME"));
						region.setDailyDbId(rs.getInt("DAILY_DB_ID"));
						region.setMonthlyDbId(rs.getInt("MONTHLY_DB_ID"));
						
						return region;
					}
					return null;
				}

			};
			return run.query(conn, selectSql, rsHandler, bean.getRegionId());
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	class RegionRsHandler implements ResultSetHandler<List<Region>>{

		@Override
		public List<Region> handle(ResultSet rs) throws SQLException {
			List<Region> results = new ArrayList<Region>();
			while(rs.next()){
				Region region = new Region();
				region.setRegionId(rs.getInt("REGION_ID"));
				region.setRegionName(rs.getString("REGION_NAME"));
				region.setDailyDbId(rs.getInt("DAILY_DB_ID"));
				region.setMonthlyDbId(rs.getInt("MONTHLY_DB_ID"));
				results.add(region);
			}
			return results;
		}
		
	}
}
