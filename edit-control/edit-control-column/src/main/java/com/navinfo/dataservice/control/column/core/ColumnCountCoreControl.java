package com.navinfo.dataservice.control.column.core;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @Title: ColumnCountCoreControl
 * @Package: com.navinfo.dataservice.control.column.core
 * @Description:
 * @Author: LittleDog
 * @Date: 2017年11月7日
 * @Version: V1.0
 */
public class ColumnCountCoreControl {

	private static final Logger logger = LoggerRepos.getLogger(ColumnCountCoreControl.class);

	private ColumnCountCoreControl() {
	}

	private static class SingletonHolder {
		private static final ColumnCountCoreControl INSTANCE = new ColumnCountCoreControl();
	}

	public static ColumnCountCoreControl getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public JSONArray queryCurrentSubtask(int subtaskId) throws Exception {
		Connection gdbConn = null;
		try {
			gdbConn = DBConnector.getInstance().getMkConnection();
			QueryRunner run = new QueryRunner();
			StringBuilder queryStr = new StringBuilder(5000);
			queryStr.append("WITH A AS");
			queryStr.append(" (SELECT DISTINCT N.SUBTASK_ID, S.PID");
			queryStr.append("    FROM POI_COLUMN_STATUS S, SUBTASK@MAN_LINK N");
			queryStr.append("   WHERE N.SUBTASK_ID = ?");
			queryStr.append("     AND S.TASK_ID = N.SUBTASK_ID");
			queryStr.append("     AND N.TYPE = 7),");
			queryStr.append("A1 AS");
			queryStr.append(" (SELECT DISTINCT A.SUBTASK_ID, N.NAME, T.LOT");
			queryStr.append("    FROM A, TASK@MAN_LINK T, SUBTASK@MAN_LINK N");
			queryStr.append("   WHERE A.SUBTASK_ID = N.SUBTASK_ID");
			queryStr.append("     AND N.TASK_ID = T.TASK_ID),");
			queryStr.append("A2 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) POI_NAME_WORK_TOTAL");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_name'");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("A3 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_NAME_WORK_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_name'");
			queryStr.append("     AND (S.SECOND_WORK_STATUS = 3 OR");
			queryStr.append("         (S.QC_FLAG = 1 AND S.SECOND_WORK_STATUS <> 3 AND");
			queryStr.append("         S.COMMON_HANDLER <> S.HANDLER))");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("A4 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_NAME_WORK_NOT_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_name'");
			queryStr.append("     AND (S.COMMON_HANDLER = 0 OR");
			queryStr.append("         (S.COMMON_HANDLER <> 0 AND S.COMMON_HANDLER = S.HANDLER AND");
			queryStr.append("         S.SECOND_WORK_STATUS <> 3))");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("A5 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_NAME_QC_TOTAL");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_name'");
			queryStr.append("     AND S.QC_FLAG = 1");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("A6 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_NAME_QC_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_name'");
			queryStr.append("     AND S.QC_FLAG = 1");
			queryStr.append("     AND S.SECOND_WORK_STATUS = 3");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("A7 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_NAME_QC_NOT_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_name'");
			queryStr.append("     AND S.QC_FLAG = 1");
			queryStr.append("     AND S.SECOND_WORK_STATUS <> 3");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("B2 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) POI_NAME_WORK_TOTAL");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND S.WORK_ITEM_ID != 'FM-YW-20-017'");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_englishname'");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("B3 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_NAME_WORK_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND S.WORK_ITEM_ID != 'FM-YW-20-017'");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_englishname'");
			queryStr.append("     AND (S.SECOND_WORK_STATUS = 3 OR");
			queryStr.append("         (S.QC_FLAG = 1 AND S.SECOND_WORK_STATUS <> 3 AND");
			queryStr.append("         S.COMMON_HANDLER <> S.HANDLER))");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("B4 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_NAME_WORK_NOT_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND S.WORK_ITEM_ID != 'FM-YW-20-017'");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_englishname'");
			queryStr.append("     AND (S.COMMON_HANDLER = 0 OR");
			queryStr.append("         (S.COMMON_HANDLER <> 0 AND S.COMMON_HANDLER = S.HANDLER AND");
			queryStr.append("         S.SECOND_WORK_STATUS <> 3))");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("B5 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_NAME_QC_TOTAL");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND S.WORK_ITEM_ID != 'FM-YW-20-017'");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_englishname'");
			queryStr.append("     AND S.QC_FLAG = 1");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("B6 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_NAME_QC_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND S.WORK_ITEM_ID != 'FM-YW-20-017'");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_englishname'");
			queryStr.append("     AND S.QC_FLAG = 1");
			queryStr.append("     AND S.SECOND_WORK_STATUS = 3");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("B7 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_NAME_QC_NOT_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND S.WORK_ITEM_ID != 'FM-YW-20-017'");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_englishname'");
			queryStr.append("     AND S.QC_FLAG = 1");
			queryStr.append("     AND S.SECOND_WORK_STATUS <> 3");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("C2 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) POI_ADDRESS_WORK_TOTAL");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_address'");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("C3 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_ADDRESS_WORK_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_address'");
			queryStr.append("     AND (S.SECOND_WORK_STATUS = 3 OR");
			queryStr.append("         (S.QC_FLAG = 1 AND S.SECOND_WORK_STATUS <> 3 AND");
			queryStr.append("         S.COMMON_HANDLER <> S.HANDLER))");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("C4 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_ADDRESS_WORK_NOT_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_address'");
			queryStr.append("     AND (S.COMMON_HANDLER = 0 OR");
			queryStr.append("         (S.COMMON_HANDLER <> 0 AND S.COMMON_HANDLER = S.HANDLER AND");
			queryStr.append("         S.SECOND_WORK_STATUS <> 3))");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("C5 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_ADDRESS_QC_TOTAL");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_address'");
			queryStr.append("     AND S.QC_FLAG = 1");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("C6 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_ADDRESS_QC_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_address'");
			queryStr.append("     AND S.QC_FLAG = 1");
			queryStr.append("     AND S.SECOND_WORK_STATUS = 3");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("C7 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_ADDRESS_QC_NOT_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_address'");
			queryStr.append("     AND S.QC_FLAG = 1");
			queryStr.append("     AND S.SECOND_WORK_STATUS <> 3");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("D2 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) POI_ENGADDR_WORK_TOTAL");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_englishaddress'");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("D3 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_ENGADDR_WORK_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_englishaddress'");
			queryStr.append("     AND (S.SECOND_WORK_STATUS = 3 OR");
			queryStr.append("         (S.QC_FLAG = 1 AND S.SECOND_WORK_STATUS <> 3 AND");
			queryStr.append("         S.COMMON_HANDLER <> S.HANDLER))");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("D4 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_ENGADDR_WORK_NOT_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_englishaddress'");
			queryStr.append("     AND (S.COMMON_HANDLER = 0 OR");
			queryStr.append("         (S.COMMON_HANDLER <> 0 AND S.COMMON_HANDLER = S.HANDLER AND");
			queryStr.append("         S.SECOND_WORK_STATUS <> 3))");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("D5 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_ENGADDR_QC_TOTAL");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_englishaddress'");
			queryStr.append("     AND S.QC_FLAG = 1");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("D6 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_ENGADDR_QC_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_englishaddress'");
			queryStr.append("     AND S.QC_FLAG = 1");
			queryStr.append("     AND S.SECOND_WORK_STATUS = 3");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("D7 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_ENGADDR_QC_NOT_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_englishaddress'");
			queryStr.append("     AND S.QC_FLAG = 1");
			queryStr.append("     AND S.SECOND_WORK_STATUS <> 3");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("E2 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) POI_DEEP_WORK_TOTAL");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_deep'");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("E3 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_DEEP_WORK_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_deep'");
			queryStr.append("     AND (S.SECOND_WORK_STATUS = 3 OR");
			queryStr.append("         (S.QC_FLAG = 1 AND S.SECOND_WORK_STATUS <> 3 AND");
			queryStr.append("         S.COMMON_HANDLER <> S.HANDLER))");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("E4 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_DEEP_WORK_NOT_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_deep'");
			queryStr.append("     AND (S.COMMON_HANDLER = 0 OR");
			queryStr.append("         (S.COMMON_HANDLER <> 0 AND S.COMMON_HANDLER = S.HANDLER))");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("E5 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_DEEP_QC_TOTAL");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_deep'");
			queryStr.append("     AND S.QC_FLAG = 1");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("E6 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_DEEP_QC_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_deep'");
			queryStr.append("     AND S.QC_FLAG = 1");
			queryStr.append("     AND S.SECOND_WORK_STATUS = 3");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("E7 AS");
			queryStr.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) AS POI_DEEP_QC_NOT_FINISH");
			queryStr.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			queryStr.append("   WHERE A.PID = S.PID");
			queryStr.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			queryStr.append("     AND W.FIRST_WORK_ITEM = 'poi_deep'");
			queryStr.append("     AND S.QC_FLAG = 1");
			queryStr.append("     AND S.SECOND_WORK_STATUS <> 3");
			queryStr.append("   GROUP BY A.SUBTASK_ID),");
			queryStr.append("A8 AS");
			queryStr.append(" (SELECT A1.NAME SUBTASK_NAME,");
			queryStr.append("         '中文名称' WORK_PROGRAM_NAME,");
			queryStr.append("         A2.POI_NAME_WORK_TOTAL WORK_TOTAL,");
			queryStr.append("         A3.POI_NAME_WORK_FINISH WORK_FINISH,");
			queryStr.append("         A4.POI_NAME_WORK_NOT_FINISH WORK_NOT_FINISH,");
			queryStr.append("         A5.POI_NAME_QC_TOTAL QUALITY_TOTAL,");
			queryStr.append("         A6.POI_NAME_QC_FINISH QUALITY_FINISH,");
			queryStr.append("         A7.POI_NAME_QC_NOT_FINISH QUALITY_NOT_FINISH,");
			queryStr.append("         A1.LOT LOT");
			queryStr.append("    FROM A1, A2, A3, A4, A5, A6, A7");
			queryStr.append("   WHERE A1.SUBTASK_ID = A2.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = A3.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = A4.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = A5.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = A6.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = A7.SUBTASK_ID(+)),");
			queryStr.append("B8 AS");
			queryStr.append(" (SELECT A1.NAME,");
			queryStr.append("         '英文名称',");
			queryStr.append("         B2.POI_NAME_WORK_TOTAL,");
			queryStr.append("         B3.POI_NAME_WORK_FINISH,");
			queryStr.append("         B4.POI_NAME_WORK_NOT_FINISH,");
			queryStr.append("         B5.POI_NAME_QC_TOTAL,");
			queryStr.append("         B6.POI_NAME_QC_FINISH,");
			queryStr.append("         B7.POI_NAME_QC_NOT_FINISH,");
			queryStr.append("         A1.LOT");
			queryStr.append("    FROM A1, B2, B3, B4, B5, B6, B7");
			queryStr.append("   WHERE A1.SUBTASK_ID = B2.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = B3.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = B4.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = B5.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = B6.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = B7.SUBTASK_ID(+)),");
			queryStr.append("C8 AS");
			queryStr.append(" (SELECT A1.NAME,");
			queryStr.append("         '中文地址',");
			queryStr.append("         C2.POI_ADDRESS_WORK_TOTAL,");
			queryStr.append("         C3.POI_ADDRESS_WORK_FINISH,");
			queryStr.append("         C4.POI_ADDRESS_WORK_NOT_FINISH,");
			queryStr.append("         C5.POI_ADDRESS_QC_TOTAL,");
			queryStr.append("         C6.POI_ADDRESS_QC_FINISH,");
			queryStr.append("         C7.POI_ADDRESS_QC_NOT_FINISH,");
			queryStr.append("         A1.LOT");
			queryStr.append("    FROM A1, C2, C3, C4, C5, C6, C7");
			queryStr.append("   WHERE A1.SUBTASK_ID = C2.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = C3.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = C4.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = C5.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = C6.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = C7.SUBTASK_ID(+)),");
			queryStr.append("D8 AS");
			queryStr.append(" (SELECT A1.NAME,");
			queryStr.append("         '英文地址',");
			queryStr.append("         D2.POI_ENGADDR_WORK_TOTAL,");
			queryStr.append("         D3.POI_ENGADDR_WORK_FINISH,");
			queryStr.append("         D4.POI_ENGADDR_WORK_NOT_FINISH,");
			queryStr.append("         D5.POI_ENGADDR_QC_TOTAL,");
			queryStr.append("         D6.POI_ENGADDR_QC_FINISH,");
			queryStr.append("         D7.POI_ENGADDR_QC_NOT_FINISH,");
			queryStr.append("         A1.LOT");
			queryStr.append("    FROM A1, D2, D3, D4, D5, D6, D7");
			queryStr.append("   WHERE A1.SUBTASK_ID = D2.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = D3.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = D4.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = D5.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = D6.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = D7.SUBTASK_ID(+)),");
			queryStr.append("E8 AS");
			queryStr.append(" (SELECT A1.NAME,");
			queryStr.append("         '深度信息',");
			queryStr.append("         E2.POI_DEEP_WORK_TOTAL,");
			queryStr.append("         E3.POI_DEEP_WORK_FINISH,");
			queryStr.append("         E4.POI_DEEP_WORK_NOT_FINISH,");
			queryStr.append("         E5.POI_DEEP_QC_TOTAL,");
			queryStr.append("         E6.POI_DEEP_QC_FINISH,");
			queryStr.append("         E7.POI_DEEP_QC_NOT_FINISH,");
			queryStr.append("         A1.LOT");
			queryStr.append("    FROM A1, E2, E3, E4, E5, E6, E7");
			queryStr.append("   WHERE A1.SUBTASK_ID = E2.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = E3.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = E4.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = E5.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = E6.SUBTASK_ID(+)");
			queryStr.append("     AND A1.SUBTASK_ID = E7.SUBTASK_ID(+))");
			queryStr.append("SELECT A8.* ");
			queryStr.append("  FROM A8 ");
			queryStr.append("UNION ALL ");
			queryStr.append("SELECT B8.* ");
			queryStr.append("  FROM B8 ");
			queryStr.append("UNION ALL ");
			queryStr.append("SELECT C8.* ");
			queryStr.append("  FROM C8 ");
			queryStr.append("UNION ALL ");
			queryStr.append("SELECT D8.* ");
			queryStr.append("  FROM D8 ");
			queryStr.append("UNION ALL ");
			queryStr.append("SELECT E8.* FROM E8");

			return run.query(gdbConn, queryStr.toString(), new ResultSetHandler<JSONArray>() {
				public JSONArray handle(ResultSet rs) throws SQLException {
					JSONArray resultJson = new JSONArray();
					while (rs.next()) {
						JSONObject jo = new JSONObject();
						jo.put("subtaskName", rs.getString("SUBTASK_NAME"));
						jo.put("workProgramName", rs.getString("WORK_PROGRAM_NAME"));
						jo.put("workTotal", rs.getInt("WORK_TOTAL"));
						jo.put("workFinish", rs.getInt("WORK_FINISH"));
						jo.put("workNotFinish", rs.getInt("WORK_NOT_FINISH"));
						jo.put("qualityTotal", rs.getInt("QUALITY_TOTAL"));
						jo.put("qualityFinish", rs.getInt("QUALITY_FINISH"));
						jo.put("qualityNotFinish", rs.getInt("QUALITY_NOT_FINISH"));
						jo.put("lot", rs.getInt("LOT"));
						resultJson.add(jo);
					}
					return resultJson;
				}
			}, subtaskId);

		} catch (Exception e) {
			logger.error("查看当前子任务统计失败，原因：" + e.getMessage());
			throw e;
		} finally {
			DbUtils.closeQuietly(gdbConn);
		}
	}

	/**
	 * 子任务列表 - 子任务统计
	 * @param userId
	 * @param subtaskIds
	 * @return
	 * @throws Exception
	 */
	public JSONArray querySubtask(long userId, JSONArray subtaskIds) throws Exception {
		Map<Integer, Integer> subtaskIdIsQualityMap = judgeQuality(subtaskIds);

		List<Integer> subtaskQualityList = new ArrayList<>();
		List<Integer> subtaskCommonList = new ArrayList<>();

		for (Entry<Integer, Integer> entry : subtaskIdIsQualityMap.entrySet()) {
			if (entry.getValue() == 1) {
				subtaskQualityList.add(entry.getKey());
			} else if (entry.getValue() == 0) {
				subtaskCommonList.add(entry.getKey());
			}
		}
		JSONArray resultJson = new JSONArray();
		JSONArray commonJsonArray = new JSONArray();
		JSONArray qualityJsonArray = new JSONArray();

		Connection gdbConn = null;
		try {
			gdbConn = DBConnector.getInstance().getMkConnection();

			if (subtaskCommonList != null && subtaskCommonList.size() > 0) {
				commonJsonArray = commonSubtaskQuery(gdbConn, subtaskCommonList, userId);
			}

			if (subtaskQualityList != null && subtaskQualityList.size() > 0) {
				qualityJsonArray = qualitySubtaskQuery(gdbConn, subtaskQualityList, userId);
			}

			resultJson.addAll(commonJsonArray);
			resultJson.addAll(qualityJsonArray);
			return resultJson;
		} catch (Exception e) {
			logger.error("子任务列表 - 子任务统计失败，原因：" + e.getMessage());
			throw e;
		} finally {
			DbUtils.closeQuietly(gdbConn);
		}

	}

	private Map<Integer, Integer> judgeQuality(JSONArray subtaskIds) throws Exception {
		String subtaskIdStr = StringUtils.join(subtaskIds, ",");

		Connection manConn = null;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			manConn = DBConnector.getInstance().getManConnection();

			List<Clob> values = new ArrayList<>();
			String subtaskIdString;
			if (subtaskIds.size() > 1000) {
				Clob clob = ConnectionUtil.createClob(manConn);
				clob.setString(1, subtaskIdStr);
				subtaskIdString = " SUBTASK_ID IN (SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?)))";
				values.add(clob);
			} else {
				subtaskIdString = " SUBTASK_ID IN ( " + subtaskIdStr + " )";
			}
			String sql = "SELECT T.SUBTASK_ID, T.IS_QUALITY FROM SUBTASK T WHERE T." + subtaskIdString;

			pstmt = manConn.prepareStatement(sql);
			if (CollectionUtils.isNotEmpty(values)) {
				for (int i = 0; i < values.size(); i++) {
					pstmt.setClob(i + 1, values.get(i));
				}
			}
			resultSet = pstmt.executeQuery();
			Map<Integer, Integer> map = new HashMap<>();
			while (resultSet.next()) {
				map.put(resultSet.getInt(1), resultSet.getInt(2));
			}
			return map;

		} catch (Exception e) {
			logger.error("查询子任务是否为质检子任务失败，原因为：" + e.getMessage());
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
			DbUtils.closeQuietly(manConn);
		}
	}

	private JSONArray commonSubtaskQuery(Connection gdbConn, List<Integer> subtaskCommonList, long userId)
			throws Exception {
		JSONArray jsonArray = new JSONArray();
		String commonSubtaskIdStr = StringUtils.join(subtaskCommonList, ",");
		try {
			QueryRunner run = new QueryRunner();

			List<Clob> values = new ArrayList<>();
			String commonSubtaskIdString;
			if (subtaskCommonList.size() > 1000) {
				Clob clob = ConnectionUtil.createClob(gdbConn);
				clob.setString(1, commonSubtaskIdStr);
				commonSubtaskIdString = " SUBTASK_ID IN (SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?)))";
				values.add(clob);
			} else {
				commonSubtaskIdString = " SUBTASK_ID IN ( " + commonSubtaskIdStr + " )";
			}
			StringBuilder commonSql = new StringBuilder(1000);
			commonSql.append("WITH A AS");
			commonSql.append(" (SELECT N.SUBTASK_ID");
			commonSql.append("    FROM SUBTASK@MAN_LINK N");
			commonSql.append("   WHERE N.").append(commonSubtaskIdString);
			commonSql.append("     AND N.TYPE = 7),");
			commonSql.append("A1 AS");
			commonSql.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) COMMON_TOTAL");
			commonSql.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			commonSql.append("   WHERE A.SUBTASK_ID = S.TASK_ID");
			commonSql.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			commonSql.append("     AND W.FIRST_WORK_ITEM <> 'poi_deep'");
			commonSql.append("     AND S.WORK_ITEM_ID != 'FM-YW-20-017'");
			commonSql.append("     AND S.SECOND_WORK_STATUS = 1");
			commonSql.append("     AND S.COMMON_HANDLER = 0");
			commonSql.append("   GROUP BY A.SUBTASK_ID),");
			commonSql.append("A2 AS");
			commonSql.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) MY_COMMON_TOTAL");
			commonSql.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			commonSql.append("   WHERE A.SUBTASK_ID = S.TASK_ID");
			commonSql.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			commonSql.append("     AND W.FIRST_WORK_ITEM <> 'poi_deep'");
			commonSql.append("     AND S.WORK_ITEM_ID != 'FM-YW-20-017'");
			commonSql.append("     AND S.COMMON_HANDLER = ?");
			commonSql.append("     AND S.HANDLER = ?");
			commonSql.append("   GROUP BY A.SUBTASK_ID),");
			commonSql.append("A3 AS");
			commonSql.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) DEEP_TOTAL");
			commonSql.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			commonSql.append("   WHERE A.SUBTASK_ID = S.TASK_ID");
			commonSql.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			commonSql.append("     AND W.FIRST_WORK_ITEM = 'poi_deep'");
			commonSql.append("     AND S.SECOND_WORK_STATUS = 1");
			commonSql.append("     AND S.COMMON_HANDLER = 0");
			commonSql.append("   GROUP BY A.SUBTASK_ID),");
			commonSql.append("A4 AS");
			commonSql.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) MY_DEEP_TOTAL");
			commonSql.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			commonSql.append("   WHERE A.SUBTASK_ID = S.TASK_ID");
			commonSql.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			commonSql.append("     AND W.FIRST_WORK_ITEM = 'poi_deep'");
			commonSql.append("     AND S.COMMON_HANDLER = ?");
			commonSql.append("     AND S.HANDLER = ?");
			commonSql.append("   GROUP BY A.SUBTASK_ID)");
			commonSql.append("SELECT A.SUBTASK_ID,");
			commonSql.append("       A1.COMMON_TOTAL,");
			commonSql.append("       A2.MY_COMMON_TOTAL,");
			commonSql.append("       A3.DEEP_TOTAL,");
			commonSql.append("       A4.MY_DEEP_TOTAL");
			commonSql.append("  FROM A, A1, A2, A3, A4");
			commonSql.append(" WHERE A.SUBTASK_ID = A1.SUBTASK_ID(+)");
			commonSql.append("   AND A.SUBTASK_ID = A2.SUBTASK_ID(+)");
			commonSql.append("   AND A.SUBTASK_ID = A3.SUBTASK_ID(+)");
			commonSql.append("   AND A.SUBTASK_ID = A4.SUBTASK_ID(+)");

			if (CollectionUtils.isNotEmpty(values)) {
				for (int i = 0; i < values.size(); i++) {
					jsonArray = run.query(gdbConn, commonSql.toString(), subtaskHandler, values.get(i), userId, userId,
							userId, userId);
				}
			} else {
				jsonArray = run.query(gdbConn, commonSql.toString(), subtaskHandler, userId, userId, userId, userId);
			}
			return jsonArray;
		} catch (Exception e) {
			logger.error("常规子任务统计失败，原因为：" + e.getMessage());
			throw e;
		}
	}

	private JSONArray qualitySubtaskQuery(Connection gdbConn, List<Integer> subtaskQualityList, long userId)
			throws Exception {
		JSONArray jsonArray = new JSONArray();
		String qualitySubtaskIdStr = StringUtils.join(subtaskQualityList, ",");
		try {
			QueryRunner run = new QueryRunner();

			List<Clob> values = new ArrayList<>();
			String qualitySubtaskIdString;
			if (subtaskQualityList.size() > 1000) {
				Clob clob = ConnectionUtil.createClob(gdbConn);
				clob.setString(1, qualitySubtaskIdStr);
				qualitySubtaskIdString = " SUBTASK_ID IN (SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?)))";
				values.add(clob);
			} else {
				qualitySubtaskIdString = " SUBTASK_ID IN ( " + qualitySubtaskIdStr + " )";
			}
			StringBuilder qualitySql = new StringBuilder(1000);
			qualitySql.append("WITH A AS");
			qualitySql.append(" (SELECT N.SUBTASK_ID");
			qualitySql.append("    FROM SUBTASK@MAN_LINK N");
			qualitySql.append("   WHERE N.").append(qualitySubtaskIdString);
			qualitySql.append("     AND N.TYPE = 7),");
			qualitySql.append("A1 AS");
			qualitySql.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) COMMON_TOTAL");
			qualitySql.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			qualitySql.append("   WHERE A.SUBTASK_ID = S.TASK_ID");
			qualitySql.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			qualitySql.append("     AND W.FIRST_WORK_ITEM <> 'poi_deep'");
			qualitySql.append("     AND S.WORK_ITEM_ID != 'FM-YW-20-017'");
			qualitySql.append("     AND S.SECOND_WORK_STATUS = 1");
			qualitySql.append("     AND S.COMMON_HANDLER <> 0");
			qualitySql.append("     AND S.HANDLER = 0");
			qualitySql.append("     AND S.QC_FLAG = 1");
			qualitySql.append("   GROUP BY A.SUBTASK_ID),");
			qualitySql.append("A2 AS");
			qualitySql.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) MY_COMMON_TOTAL");
			qualitySql.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			qualitySql.append("   WHERE A.SUBTASK_ID = S.TASK_ID");
			qualitySql.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			qualitySql.append("     AND W.FIRST_WORK_ITEM <> 'poi_deep'");
			qualitySql.append("     AND S.WORK_ITEM_ID != 'FM-YW-20-017'");
			qualitySql.append("     AND S.COMMON_HANDLER <> 0");
			qualitySql.append("     AND S.COMMON_HANDLER <> S.HANDLER");
			qualitySql.append("     AND S.HANDLER = ?");
			qualitySql.append("     AND S.QC_FLAG = 1");
			qualitySql.append("   GROUP BY A.SUBTASK_ID),");
			qualitySql.append("A3 AS");
			qualitySql.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) DEEP_TOTAL");
			qualitySql.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			qualitySql.append("   WHERE A.SUBTASK_ID = S.TASK_ID");
			qualitySql.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			qualitySql.append("     AND W.FIRST_WORK_ITEM = 'poi_deep'");
			qualitySql.append("     AND S.SECOND_WORK_STATUS = 1");
			qualitySql.append("     AND S.COMMON_HANDLER <> 0");
			qualitySql.append("     AND S.HANDLER = 0");
			qualitySql.append("     AND S.QC_FLAG = 1");
			qualitySql.append("   GROUP BY A.SUBTASK_ID),");
			qualitySql.append("A4 AS");
			qualitySql.append(" (SELECT A.SUBTASK_ID, COUNT(S.WORK_ITEM_ID) MY_DEEP_TOTAL");
			qualitySql.append("    FROM A, POI_COLUMN_STATUS S, POI_COLUMN_WORKITEM_CONF W");
			qualitySql.append("   WHERE A.SUBTASK_ID = S.TASK_ID");
			qualitySql.append("     AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
			qualitySql.append("     AND W.FIRST_WORK_ITEM = 'poi_deep'");
			qualitySql.append("     AND S.COMMON_HANDLER <> 0");
			qualitySql.append("     AND S.COMMON_HANDLER <> S.HANDLER");
			qualitySql.append("     AND S.HANDLER = ?");
			qualitySql.append("     AND S.QC_FLAG = 1");
			qualitySql.append("   GROUP BY A.SUBTASK_ID)");
			qualitySql.append("SELECT A.SUBTASK_ID,");
			qualitySql.append("       A1.COMMON_TOTAL,");
			qualitySql.append("       A2.MY_COMMON_TOTAL,");
			qualitySql.append("       A3.DEEP_TOTAL,");
			qualitySql.append("       A4.MY_DEEP_TOTAL");
			qualitySql.append("  FROM A, A1, A2, A3, A4");
			qualitySql.append(" WHERE A.SUBTASK_ID = A1.SUBTASK_ID(+)");
			qualitySql.append("   AND A.SUBTASK_ID = A2.SUBTASK_ID(+)");
			qualitySql.append("   AND A.SUBTASK_ID = A3.SUBTASK_ID(+)");
			qualitySql.append("   AND A.SUBTASK_ID = A4.SUBTASK_ID(+)");

			if (CollectionUtils.isNotEmpty(values)) {
				for (int i = 0; i < values.size(); i++) {
					jsonArray = run.query(gdbConn, qualitySql.toString(), subtaskHandler, values.get(i), userId,
							userId);
				}
			} else {
				jsonArray = run.query(gdbConn, qualitySql.toString(), subtaskHandler, userId, userId);
			}
			return jsonArray;
		} catch (Exception e) {
			logger.error("质检子任务统计失败，原因为：" + e.getMessage());
			throw e;
		}
	}

	private ResultSetHandler<JSONArray> subtaskHandler = new ResultSetHandler<JSONArray>() {
		public JSONArray handle(ResultSet rs) throws SQLException {
			JSONArray jsonArray = new JSONArray();
			while (rs.next()) {
				JSONObject jo = new JSONObject();
				jo.put("subtaskId", rs.getString("SUBTASK_ID"));
				jo.put("commonTotal", rs.getInt("COMMON_TOTAL"));
				jo.put("myCommonTotal", rs.getInt("MY_COMMON_TOTAL"));
				jo.put("deepTotal", rs.getInt("DEEP_TOTAL"));
				jo.put("myDeepTotal", rs.getInt("MY_DEEP_TOTAL"));
				jsonArray.add(jo);
			}
			return jsonArray;
		}
	};

}