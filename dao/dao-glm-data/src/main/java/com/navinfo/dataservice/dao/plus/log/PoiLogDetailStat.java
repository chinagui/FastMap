package com.navinfo.dataservice.dao.plus.log;

import java.sql.Clob;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.database.QueryRunner;

/**
 * 只统计POI的履历
 * 
 * @ClassName: LogStat
 * @author xiaoxiaowen4127
 * @date 2016年12月5日
 * @Description: LogStat.java
 */
public class PoiLogDetailStat {
	private static Logger log = Logger.getLogger(PoiLogDetailStat.class);

	/**
	 * 对应日落月的履历统计
	 * 
	 * @param conn
	 * @param tempOpTable
	 *            ：履历操作id的临时表
	 * @return
	 * @throws Exception
	 */
	public static Map<Long, List<LogDetail>> loadByOperation(Connection conn,
			String tempOpTable) throws Exception {
		if (StringUtils.isEmpty(tempOpTable))
			return null;
		String sql = "SELECT T.OB_NM,T.OB_PID,T.TB_NM,T.OLD,T.NEW,T.FD_LST,T.OP_TP,T.TB_ROW_ID FROM LOG_DETAIL T,"
				+ tempOpTable + " TEMP " + "WHERE T.OP_ID=TEMP.OP_ID";
		return new QueryRunner().query(conn, sql,
				new LogDetailRsHandler4ChangeLog());
	}

	/**
	 * 对应精编提交的履历统计
	 * 
	 * @param conn
	 * @param pids
	 * @param userId
	 * @param taskId
	 * @param firstWorkItem
	 * @param secondWorkItem
	 * @return
	 * @throws Exception
	 */
	public static Map<Long, List<LogDetail>> loadByColEditStatus(
			Connection conn, Collection<Long> pids, long userId, long taskId,
			String firstWorkItem, String secondWorkItem) throws Exception {
		// if(pids==null||pids.size()==0)return null;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT T.OB_NM,T.OB_PID,T.TB_NM,T.OLD,T.NEW,T.FD_LST,T.OP_TP,T.TB_ROW_ID "
				+ "FROM LOG_DETAIL T,LOG_OPERATION LP,POI_COLUMN_STATUS P, POI_COLUMN_WORKITEM_CONF C "
				+ "WHERE T.OP_ID=LP.OP_ID "
				+ " AND T.OB_NM='"
				+ ObjectName.IX_POI
				+ "'"
				+ "   AND T.OB_PID=P.PID"
				+ "   AND P.WORK_ITEM_ID = C.WORK_ITEM_ID"
				+ "   AND C.CHECK_FLAG IN (1, 2)"
				+ "   AND C.FIRST_WORK_ITEM = '" + firstWorkItem + "'");
		sb.append(" AND LP.OP_DT>=P.APPLY_DATE");
		sb.append("   AND P.HANDLER=" + userId);
		sb.append("   AND P.TASK_ID=" + taskId);
		sb.append("   AND P.FIRST_WORK_STATUS IN (1,2)");
		// 若针对二级项进行自定义检查，则检查对象应该是二级项状态为待作业，已作业状态
		if (secondWorkItem != null && !secondWorkItem.isEmpty()) {
			sb.append("   AND C.SECOND_WORK_ITEM = '" + secondWorkItem + "'"
					+ "   AND P.SECOND_WORK_STATUS IN (1,2)");
		}

		List<Object> values = new ArrayList<Object>();
		if (pids != null && pids.size() > 0) {
			if (pids.size() > 1000) {
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(pids, ","));
				sb.append(" AND P.PID IN (select to_number(column_value) from table(clob_to_table(?)))");
				values.add(clob);
			} else {
				sb.append(" AND P.PID IN (" + StringUtils.join(pids, ",") + ")");
			}
		}
		if (values != null && values.size() > 0) {
			Object[] queryValues = new Object[values.size()];
			for (int i = 0; i < values.size(); i++) {
				queryValues[i] = values.get(i);
			}
			return new QueryRunner().query(conn, sb.toString(),
					new LogDetailRsHandler4ChangeLog(), queryValues);
		} else {
			return new QueryRunner().query(conn, sb.toString(),
					new LogDetailRsHandler4ChangeLog());
		}
	}
/***
 * 精编履历统计
 * @param conn
 * @param pids
 * @return
 * @throws Exception
 */
	public static Map<Long, List<LogDetail>> loadByColEditStatus(
			Connection conn, Collection<Long> pids) throws Exception {
		// if(pids==null||pids.size()==0)return null;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT T.OB_NM,T.OB_PID,T.TB_NM,T.OLD,T.NEW,T.FD_LST,T.OP_TP,T.TB_ROW_ID "
				+ "FROM LOG_DETAIL T "
				+ " WHERE T.OB_NM='"
				+ ObjectName.IX_POI + "'" );
		List<Object> values = new ArrayList<Object>();
		if (pids != null && pids.size() > 0) {
			if (pids.size() > 1000) {
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(pids, ","));
				sb.append(" AND T.OB_PID IN (select to_number(column_value) from table(clob_to_table(?)))");
				values.add(clob);
			} else {
				sb.append(" AND  T.OB_PID IN (" + StringUtils.join(pids, ",") + ")");
			}
		}
		if (values != null && values.size() > 0) {
			Object[] queryValues = new Object[values.size()];
			for (int i = 0; i < values.size(); i++) {
				queryValues[i] = values.get(i);
			}
			return new QueryRunner().query(conn, sb.toString(),
					new LogDetailRsHandler4ChangeLog(), queryValues);
		} else {
			return new QueryRunner().query(conn, sb.toString(),
					new LogDetailRsHandler4ChangeLog());
		}
	}

	/**
	 * 对应行编提交的履历统计
	 * 
	 * @param conn
	 * @param pids
	 * @return
	 * @throws Exception
	 */
	public static Map<Long, List<LogDetail>> loadByRowEditStatus(
			Connection conn, Collection<Long> pids) throws Exception {
		if (pids == null || pids.size() == 0)
			return null;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT T.OB_NM,T.OB_PID,T.TB_NM,T.OLD,T.NEW,T.FD_LST,T.OP_TP,T.TB_ROW_ID "
				+ "FROM LOG_DETAIL T,LOG_OPERATION LP,POI_EDIT_STATUS P"
				+ " WHERE T.OP_ID=LP.OP_ID "
				+ " AND T.OB_NM='"
				+ ObjectName.IX_POI + "'" + "   AND T.OB_PID=P.PID");
		// 若P.SUBMIT_DATE最后一次提交时间为空，则，取poi的全部履历；否则取SUBMIT_DATE最后一次提交时间之后的所有履历。
		sb.append(" AND ((LP.OP_DT>=P.SUBMIT_DATE AND P.SUBMIT_DATE IS NOT NULL) OR P.SUBMIT_DATE IS NULL)");

		List<Object> values = new ArrayList<Object>();
		if (pids != null && pids.size() > 0) {
			if (pids.size() > 1000) {
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(pids, ","));
				sb.append(" AND P.PID IN (select to_number(column_value) from table(clob_to_table(?)))");
				values.add(clob);
			} else {
				sb.append(" AND P.PID IN (" + StringUtils.join(pids, ",") + ")");
			}
		}
		log.debug(sb.toString());
		if (values != null && values.size() > 0) {
			Object[] queryValues = new Object[values.size()];
			for (int i = 0; i < values.size(); i++) {
				queryValues[i] = values.get(i);
			}
			return new QueryRunner().query(conn, sb.toString(),
					new LogDetailRsHandler4ChangeLog(), queryValues);
		} else {
			return new QueryRunner().query(conn, sb.toString(),
					new LogDetailRsHandler4ChangeLog());
		}
	}
	
	/**
	 * 对应点门牌行编提交的履历统计
	 * 
	 * @param conn
	 * @param pids
	 * @return
	 * @throws Exception
	 */
	public static Map<Long, List<LogDetail>> loadPointAddressByRowEditStatus(
			Connection conn, Collection<Long> pids) throws Exception {
		if (pids == null || pids.size() == 0)
			return null;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT T.OB_NM, T.OB_PID, T.TB_NM, T.OLD, T.NEW, T.FD_LST, T.OP_TP, T.TB_ROW_ID FROM LOG_DETAIL T, LOG_OPERATION LP, POINTADDRESS_EDIT_STATUS P "
				+ " WHERE T.OP_ID = LP.OP_ID AND T.OB_NM = ' " + ObjectName.IX_POINTADDRESS + " ' AND T.OB_PID = P.PID ");
		// 若P.SUBMIT_DATE最后一次提交时间为空，则取poi的全部履历；否则取SUBMIT_DATE最后一次提交时间之后的所有履历。
		sb.append(" AND ((LP.OP_DT >= P.SUBMIT_DATE AND P.SUBMIT_DATE IS NOT NULL) OR P.SUBMIT_DATE IS NULL) ");
		
		List<Object> values = new ArrayList<Object>();
		if (pids != null && pids.size() > 0) {
			if (pids.size() > 1000) {
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(pids, ","));
				sb.append(" AND P.PID IN (SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?))) ");
				values.add(clob);
			} else {
				sb.append(" AND P.PID IN (" + StringUtils.join(pids, ",") + " ) ");
			}
		}
		log.debug(sb.toString());
		if (values != null && values.size() > 0) {
			Object[] queryValues = new Object[values.size()];
			for (int i = 0; i < values.size(); i++) {
				queryValues[i] = values.get(i);
			}
			return new QueryRunner().query(conn, sb.toString(), new LogDetailRsHandler4ChangeLog(), queryValues);
		} else {
			return new QueryRunner().query(conn, sb.toString(), new LogDetailRsHandler4ChangeLog());
		}
	}
	
	/**
	 * 查询子poi的所有履历（包含父子关系的履历）
	 * 
	 * @param conn
	 * @param pids
	 * @return
	 * @throws Exception
	 */
	public static Map<Long, List<LogDetail>> loadByTBRowIDEditStatus(
			Connection conn, Collection<Long> pids) throws Exception {
		if (pids == null || pids.size() == 0)
			return null;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT T.OB_NM,T.OB_PID,T.TB_NM,T.OLD,T.NEW,T.FD_LST,T.OP_TP,T.TB_ROW_ID "
				+ "FROM LOG_DETAIL T,LOG_OPERATION LP,POI_EDIT_STATUS P"
				+ " WHERE T.OP_ID=LP.OP_ID "
				+ " AND T.OB_NM='"
				+ ObjectName.IX_POI + "'" + "   AND T.GEO_PID=P.PID");
		// 若P.SUBMIT_DATE最后一次提交时间为空，则，取poi的全部履历；否则取SUBMIT_DATE最后一次提交时间之后的所有履历。
		sb.append(" AND ((LP.OP_DT>=P.SUBMIT_DATE AND P.SUBMIT_DATE IS NOT NULL) OR P.SUBMIT_DATE IS NULL)");

		List<Object> values = new ArrayList<Object>();
		if (pids != null && pids.size() > 0) {
			if (pids.size() > 1000) {
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(pids, ","));
				sb.append(" AND P.PID IN (select to_number(column_value) from table(clob_to_table(?)))");
				values.add(clob);
			} else {
				sb.append(" AND P.PID IN (" + StringUtils.join(pids, ",") + ")");
			}
		}
		log.debug(sb.toString());
		if (values != null && values.size() > 0) {
			Object[] queryValues = new Object[values.size()];
			for (int i = 0; i < values.size(); i++) {
				queryValues[i] = values.get(i);
			}
			return new QueryRunner().query(conn, sb.toString(),
					new LogDetailRsHandler4ChangeLog(), queryValues);
		} else {
			return new QueryRunner().query(conn, sb.toString(),
					new LogDetailRsHandler4ChangeLog());
		}
	}

	/**
	 * 对应行编提交的履历统计
	 * 
	 * @param conn
	 * @param pids
	 * @return
	 * @throws Exception
	 */
	public static Map<Long, List<LogDetail>> loadAllLog(Connection conn,
			Collection<Long> pids) throws Exception {
		if (pids == null || pids.size() == 0)
			return null;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT T.OB_NM,T.OB_PID,T.TB_NM,T.OLD,T.NEW,T.FD_LST,T.OP_TP,T.TB_ROW_ID "
				+ "FROM LOG_DETAIL T"
				+ " WHERE T.OB_NM='"
				+ ObjectName.IX_POI
				+ "'");

		List<Object> values = new ArrayList<Object>();
		if (pids != null && pids.size() > 0) {
			if (pids.size() > 1000) {
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(pids, ","));
				sb.append(" AND T.OB_PID IN (select to_number(column_value) from table(clob_to_table(?)))");
				values.add(clob);
			} else {
				sb.append(" AND T.OB_PID IN (" + StringUtils.join(pids, ",")
						+ ")");
			}
		}
		if (values != null && values.size() > 0) {
			Object[] queryValues = new Object[values.size()];
			for (int i = 0; i < values.size(); i++) {
				queryValues[i] = values.get(i);
			}
			return new QueryRunner().query(conn, sb.toString(),
					new LogDetailRsHandler4ChangeLog(), queryValues);
		} else {
			return new QueryRunner().query(conn, sb.toString(),
					new LogDetailRsHandler4ChangeLog());
		}
	}

	/**
	 * 查询日编履历
	 * 
	 * @param conn
	 * @param pids
	 * @return
	 * @throws Exception
	 */
	public static Map<Long, List<LogDetail>> loadAllRowLog(Connection conn,
			Collection<Long> pids) throws Exception {
		if (pids == null || pids.size() == 0)
			return null;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT T.OB_NM,T.OB_PID,T.TB_NM,T.OLD,T.NEW,T.FD_LST,T.OP_TP,T.TB_ROW_ID "
				+ " FROM LOG_DETAIL T,LOG_ACTION A,LOG_OPERATION O"
				+ " WHERE A.ACT_ID=O.ACT_ID AND O.OP_ID=T.OP_ID "
				+ " AND T.OB_NM='" + ObjectName.IX_POI + "'");

		List<Object> values = new ArrayList<Object>();
		if (pids != null && pids.size() > 0) {
			if (pids.size() > 1000) {
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(pids, ","));
				sb.append(" AND T.OB_PID IN (select to_number(column_value) from table(clob_to_table(?)))");
				values.add(clob);
			} else {
				sb.append(" AND T.OB_PID IN (" + StringUtils.join(pids, ",")
						+ ")");
			}
		}
		if (values != null && values.size() > 0) {
			Object[] queryValues = new Object[values.size()];
			for (int i = 0; i < values.size(); i++) {
				queryValues[i] = values.get(i);
			}
			return new QueryRunner().query(conn, sb.toString(),
					new LogDetailRsHandler4ChangeLog(), queryValues);
		} else {
			return new QueryRunner().query(conn, sb.toString(),
					new LogDetailRsHandler4ChangeLog());
		}
	}
	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT T.OB_NM,T.OB_PID,T.TB_NM,T.OLD,T.NEW,T.FD_LST,T.OP_TP,T.TB_ROW_ID "
				+ "FROM LOG_DETAIL T,LOG_OPERATION LP,POI_COLUMN_STATUS P"
				+ " WHERE T.OP_ID=LP.OP_ID "
				+ " AND T.OB_NM='"
				+ ObjectName.IX_POI + "'" + "   AND T.OB_PID=P.PID");
		sb.append(" AND LP.OP_DT>=P.APPLY_DATE");
		System.out.println(sb.toString());
	}
}
