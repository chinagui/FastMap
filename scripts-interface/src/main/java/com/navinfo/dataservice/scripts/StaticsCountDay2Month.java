package com.navinfo.dataservice.scripts;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.glm.GlmCache;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ExportExcel;
import com.navinfo.dataservice.scripts.model.DayMonthCount;
import com.navinfo.navicommons.database.QueryRunner;

/**
 * @ClassName: StaticsCountDay2Month
 * @author zhaokk
 * @date 2017-8-9 下午3:40:32
 * @Description: TODO
 */
public class StaticsCountDay2Month {
	private static Logger logger = LoggerRepos
			.getLogger(StaticsCountDay2Month.class);

	public static void execute() throws Exception {

		String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.gdbVersion);

		List<String> tableNames = GlmCache.getInstance().getGlm(gdbVersion)
				.getEditTableNames(GlmTable.FEATURE_TYPE_ALL);

		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		List<Region> regionSet = manApi.queryRegionList();
		QueryRunner runner = new QueryRunner();

		ExportExcel<DayMonthCount> ex = new ExportExcel<DayMonthCount>();

		String[] headers = { "日大区库ID", "表名", "数量", "母库数量" };

		List<DayMonthCount> dayMonthCounts = new ArrayList<DayMonthCount>();
		Map<String, Integer> map = getMonthCountForTableName(tableNames);
		for (Region region : regionSet) {
			Connection conn = null;
			try {
				conn = DBConnector.getInstance().getConnectionById(
						region.getDailyDbId());
				for (String tableName : tableNames) {
					DayMonthCount countMonthCount = new DayMonthCount();
					String sqlCount = "SELECT COUNT(1) FROM " + tableName;
					int count = runner.queryForInt(conn, sqlCount);
					countMonthCount.setDayDbId(region.getDailyDbId());
					countMonthCount.setTableName(tableName);
					countMonthCount.setDayCount(count);
					countMonthCount.setMonthCount(map.get(tableName));
					dayMonthCounts.add(countMonthCount);

				}

			} catch (Exception e) {
				DbUtils.rollbackAndCloseQuietly(conn);
				logger.error(e.getMessage(), e);
				throw e;
			} finally {
				DbUtils.closeQuietly(conn);
			}
		}
		try {
			OutputStream out = new FileOutputStream(DateUtils.dateToString(
					new Date(), "yyyyMMddHHmmss") + "dayMonthCount.xls");

			ex.exportExcel("日库数据统计表", headers, dayMonthCounts, out,
					"yyyy-MM-dd HH:mm:ss");
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		try {
			JobScriptsInterface.initContext();
			execute();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

		}
	}

	private static Map<String, Integer> getMonthCountForTableName(
			List<String> tableNames) throws Exception {
		Connection conn = null;
		QueryRunner runner = new QueryRunner();
		Map<String, Integer> map = new HashMap<String, Integer>();
		try {
			conn = DBConnector.getInstance().getMkConnection();
			for (String tableName : tableNames) {
				String sqlCount = "SELECT COUNT(1) FROM " + tableName;
				int count = runner.queryForInt(conn, sqlCount);
				map.put(tableName, count);
			}
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
		return map;
	}

}