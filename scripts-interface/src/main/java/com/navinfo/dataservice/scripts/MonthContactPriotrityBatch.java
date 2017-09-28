package com.navinfo.dataservice.scripts;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;

/**
 * 批处理日库和月库联系信息优先级
 * 
 * @ClassName: MonthContactPriotrityBatch
 * @author zhaokk
 * @date 20170928
 * @Description: MonthContactPriotrityBatch.java
 */
public class MonthContactPriotrityBatch {
	private static final Logger logger = LoggerRepos
			.getLogger(MonthContactPriotrityBatch.class);

	public static JSONObject execute(JSONObject request) throws Exception {

		if (!request.containsKey("dbType")) {
			throw new IllegalArgumentException("参数表中缺少参数dbType 0 母库 1 日库 ");
		} else {
			if (request.getInt("dbType") != 1 && request.getInt("dbType") != 0) {
				throw new IllegalArgumentException(
						"参数表中dbType配置不正确  0 母库 1 日库 ");
			}
		}
		List<Integer> dbIdExtend = new ArrayList<Integer>();
		if (request.containsKey("dbIds")) {
			String dbIds = request.getString("dbIds");
			if (StringUtils.isNotEmpty(dbIds)) {
				for (String str : dbIds.split(",")) {
					dbIdExtend.add(Integer.parseInt(str));
				}
			}
		}
		JSONObject response = new JSONObject();
		logger.info("batch contact start...");

		int dbType = request.getInt("dbType");
		if (dbType == 0) {
			logger.info("开始处理母库电话信息信息");
			batchMonthContact();
		}
		if (dbType == 1) {
			logger.info("开始处理日库电话信息信息");
			batchDayContact(dbIdExtend);

		}
		logger.info("batch contact end...");
		logger.debug("Over.");
		return response;

	}

	/***
	 * 执行修改poi电话联系信息
	 * 
	 * @param conn
	 * @param pids
	 * @throws Exception
	 */
	private static void batchContactPriority(Connection conn, List<Long> pids)
			throws Exception {
		String ids = org.apache.commons.lang.StringUtils.join(pids, ",");
		Clob pidClod = ConnectionUtil.createClob(conn);
		pidClod.setString(1, ids);
		logger.info("begin批电话信息");
		StringBuilder addSql = new StringBuilder();
		addSql.append(" MERGE INTO ix_poi_contact a ");
		addSql.append(" USING (SELECT ic.*, ");
		addSql.append("  ROW_NUMBER () OVER (PARTITION BY poi_pid ORDER BY ic.contact_type,ic.contact)  id ");
		addSql.append(" FROM ix_poi_contact ic where ic.u_record !=2 and  ic.poi_pid IN (select to_number(column_value) from table(clob_to_table(?)))) b ");
		addSql.append(" ON (    a.row_id = b.row_id ");
		addSql.append(" AND a.u_record !=2 and  a.poi_pid IN (select to_number(column_value) from table(clob_to_table(?)))) ");
		addSql.append(" WHEN matched  then update set a.priority = b.id ");
		QueryRunner run = new QueryRunner();
		logger.info("end批电话信息");
		try {
			run.update(conn, addSql.toString(), pidClod, pidClod);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}

	}

	/***
	 * 查找符合要修改poi优先级的信息
	 * 
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private static List<Long> getBatchContactPids(Connection conn)
			throws Exception {

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		List<Long> pids = new ArrayList<Long>();
		StringBuilder sql = new StringBuilder();
		sql.append(" with temp as (  ");
		sql.append(" select p.pid  ");
		sql.append(" from ix_poi p,sc_point_spec_kindcode_new@DBLINK_RMS sp ");
		sql.append(" where p.kind_code= sp.poi_kind   and p.state <>1  and sp.type = 7  and sp.category =1  ");
		sql.append(" union select p.pid ");
		sql.append(" from ix_poi p,sc_point_spec_kindcode_new@DBLINK_RMS sp");
		sql.append(" where p.kind_code= sp.poi_kind  and p.state <>1  and sp.type = 7 and sp.category =3 and nvl(p.chain,'0') = nvl(sp.chain,'0') ");
		sql.append(" union select p.pid  ");
		sql.append(" from ix_poi p,sc_point_spec_kindcode_new@DBLINK_RMS sp ");
		sql.append(" where p.kind_code= sp.poi_kind  and p.state <>1 and sp.type = 7 and sp.category =7  and nvl(p.chain,'0') = nvl(sp.chain,'0') and sp.hm_flag in ('HKMC','HM','D') ) ");
		sql.append(" select ix.poi_pid");
		sql.append(" from temp,ix_poi_contact ix ");
		sql.append(" where ix.u_record != 2 and  ix.poi_pid = temp.pid group by ix.poi_pid ,ix.priority having count(1) <> 1 ");
		logger.info(sql.toString());
		try {

			pstmt = conn.prepareStatement(sql.toString());
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				pids.add(resultSet.getLong("poi_pid"));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return pids;

	}

	public static void main(String[] args) throws Exception {
		initContext();
	}

	/***
	 * 执行月库poi联系信息
	 * 
	 * @throws Exception
	 */
	private static void batchMonthContact() throws Exception {
		Connection conn = null;
		List<Long> pids = new ArrayList<Long>();

		try {
			conn = DBConnector.getInstance().getMkConnection();
			pids = getBatchContactPids(conn);
			if (pids.size() == 0) {
				logger.debug("没有符合的pid信息");
				return;
			}
			logger.info(String.format("match pids  [%s]",
					Arrays.toString(pids.toArray(new Long[] {}))));
			batchContactPriority(conn, pids);
		} catch (Exception e) {
			logger.error(e.getMessage() + "批处理母库电话信息失败", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}

	}

	/***
	 * 执行日库联系信息
	 * 
	 * @param dbIdExtend
	 * 
	 * @throws Exception
	 */
	private static void batchDayContact(List<Integer> dbIdExtend)
			throws Exception {
		List<Integer> dbIds = null;
		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		dbIds = manApi.listDayDbIds();
		if (dbIdExtend.size() > 0) {
			dbIds = dbIdExtend;
		} else {
			dbIds = manApi.listDayDbIds();
		}

		for (int dbId : dbIds) {
			Connection conn = null;
			try {
				logger.info("当前正在批[" + dbId + "]大区库的电话优先级信息");
				List<Long> pids = new ArrayList<Long>();
				conn = DBConnector.getInstance().getConnectionById(dbId);
				pids = getBatchContactPids(conn);
				if (pids.size() == 0) {
					logger.info("大区库[" + dbId + "]没有符合的信息不需要批处理");
					continue;
				}
				logger.info(String.format("match pids  [%s]",
						Arrays.toString(pids.toArray(new Long[] {}))));
				batchContactPriority(conn, pids);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				DbUtils.rollbackAndCloseQuietly(conn);
				throw e;
			} finally {
				DbUtils.commitAndCloseQuietly(conn);
			}
		}

	}

	public static void initContext() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-app-scripts.xml", "dubbo-scripts.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}

}
