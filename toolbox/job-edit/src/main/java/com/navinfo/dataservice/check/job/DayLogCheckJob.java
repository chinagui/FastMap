package com.navinfo.dataservice.check.job;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.glm.GlmCache;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.database.sql.DbLinkCreator;
import com.navinfo.navicommons.exception.ServiceRtException;
import com.navinfo.navicommons.exception.ThreadExecuteException;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DayLogCheckJob extends AbstractJob {

    private Logger log = Logger
            .getLogger(DayLogCheckJob.class);

    private VMThreadPoolExecutor poolExecutor;

    private QueryRunner runner = new QueryRunner();

    private int bakDbId;

    private int regionDbId;

    private final String diffInfo = "DIFF_INFO";
    private final String geoMd5InfoBak = "GEO_MD5_INFO";
    private final String geoMd5InfoRegion = "GEO_MD5_INFO_DIFF_DB";
    private final int insertCount = 10000;
    private final int oneDBConnCount = 5;

    public DayLogCheckJob(JobInfo jobInfo) {

        super(jobInfo);
    }

    @Override
    public void execute() throws JobException {

        log.debug("履历验证差分开始");
        long t = System.currentTimeMillis();

        bakDbId = jobInfo.getRequest().getInt("bakDbId");

        regionDbId = jobInfo.getRequest().getInt("regionDbId");

        String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);

        //gdbVersion = "270+";
        log.debug("备份库：" + bakDbId + " 大区库：" + regionDbId + " 版本：" + gdbVersion);

        List<String> tableNames = GlmCache.getInstance().getGlm(gdbVersion).getEditTableNames(GlmTable.FEATURE_TYPE_ALL);

        tableNames.remove("CK_EXCEPTION");
        tableNames.remove("ADAS_NODE");//3d几何 暂不支持
        tableNames.remove("ADAS_LINK");//3d几何 暂不支持

        Connection bakConn = null;

        try {
            DatahubApi datahub = (DatahubApi) ApplicationContextUtil.getBean("datahubApi");

            DbInfo bakInfo = datahub.getDbById(bakDbId);

            log.debug("获取Schema");

            OracleSchema bakSchema = new OracleSchema(
                    DbConnectConfig.createConnectConfig(bakInfo.getConnectParam()));

            DbInfo regionInfo = datahub.getDbById(regionDbId);

            OracleSchema regionSchema = new OracleSchema(
                    DbConnectConfig.createConnectConfig(regionInfo.getConnectParam()));

            log.debug("建立DBLink");

            String dbLinkName = createDbLink(bakSchema, regionSchema);

            log.debug("DBLink：" + dbLinkName);

            Map<String, String> tableSelectSql = new HashMap<>();

            List<String> geoTableNames = new ArrayList<>();

            bakConn = bakSchema.getPoolDataSource().getConnection();

            getTableColumn(bakConn, tableNames, tableSelectSql, geoTableNames);

            Collections.sort(tableNames);

            Collections.sort(geoTableNames);

            log.debug("差分非几何字段");

            log.debug(tableNames);

            handleDiffInfoTable(bakConn);

            int diffCount = 0;

            //差分非几何字段
            for (String tableName : tableNames) {

                log.debug(tableName);

                diffCount += scanDiff(bakConn, tableSelectSql.get(tableName), tableName, dbLinkName, regionDbId);

                diffCount += scanDiff(bakConn, tableSelectSql.get(tableName), tableName, dbLinkName, bakDbId);
            }

            initPoolExecutor();

            log.debug("差分几何字段");

            log.debug(geoTableNames);

            createMd5Function(bakConn);

            diffCount += diffGeoTable(bakConn, geoTableNames, dbLinkName);

            deleteDBLink(bakConn, dbLinkName);

            if (diffCount > 0) {

                bakConn.commit();
            }

            log.debug("履历验证差分结束  结束，用时：" + (System.currentTimeMillis() - t) / 1000.0 + "s");

        } catch (Exception e) {

            DbUtils.rollbackAndCloseQuietly(bakConn);
            throw new JobException(e.getMessage(), e);

        } finally {

            shutDownPoolExecutor();

            DbUtils.closeQuietly(bakConn);
        }
    }

    private int diffGeoTable(Connection bakConn, List<String> geoTableNames, String dbLinkName)
            throws Exception {

        int diffCount = 0;

        Map<String, List<Integer>> bakPages = getPageSizes(bakConn, geoTableNames, "");

        Map<String, List<Integer>> regionPages = getPageSizes(bakConn, geoTableNames, "@" + dbLinkName);

        for (String tableName : geoTableNames) {

            //清空geoMd5Info表
            String truncateTable = "TRUNCATE TABLE " + geoMd5InfoBak;

            runner.execute(bakConn, truncateTable);

            truncateTable = "TRUNCATE TABLE " + geoMd5InfoRegion;

            runner.execute(bakConn, truncateTable);

            insertGeoMd5(tableName, dbLinkName, bakPages.get(tableName), regionPages.get(tableName));

            long t = System.currentTimeMillis();

            log.debug(tableName + "：scanGeoDiff  开始：");
            diffCount += scanGeoDiff(bakConn, tableName, bakDbId);
            log.debug(tableName + "：scanGeoDiff  结束，用时：" + (System.currentTimeMillis() - t) / 1000.0 + "s");

            t = System.currentTimeMillis();

            log.debug(tableName + "：scanGeoDiff  开始：");

            diffCount += scanGeoDiff(bakConn, tableName, regionDbId);
            log.debug(tableName + "：scanGeoDiff  结束，用时：" + (System.currentTimeMillis() - t) / 1000.0 + "s");
        }
        return diffCount;
    }

    private void getTableColumn(Connection bakConn, List<String> tableNames, Map<String, String> tableSelectSql, List<String> geoTableNames) throws Exception {

        Map<String, List<String>> tableColumns = new HashMap<>();

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {

            String sql = "SELECT T.COLUMN_NAME, T.TABLE_NAME, T.DATA_TYPE FROM USER_TAB_COLUMNS T ORDER BY T.TABLE_NAME";

            pstmt = bakConn.prepareStatement(sql);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                String tableName = resultSet.getString("TABLE_NAME");

                if (!tableNames.contains(tableName)) {
                    continue;
                }

                String columnName = resultSet.getString("COLUMN_NAME");

                String dataType = resultSet.getString("DATA_TYPE");

                if (dataType.equals("SDO_GEOMETRY") || columnName.equals("GEOMETRY")) {
                    geoTableNames.add(tableName);

                    continue;
                }
                if (columnName.equals("U_DATE")) {
                    continue;
                }

                if (dataType.equals("CLOB") || dataType.equals("BLOB")) {
                    columnName = " TO_CHAR(" + columnName + ") ";
                }
                if (columnName.equals("MODE") || columnName.equals("CURRENT")) {
                    columnName = "\"" + columnName + "\"";
                }

                if (!tableColumns.containsKey(tableName)) {

                    tableColumns.put(tableName, new ArrayList<String>());
                }

                tableColumns.get(tableName).add(columnName);
            }

            getSelectSql(tableColumns, tableSelectSql);

        } catch (Exception e) {

            throw new Exception(e);
        } finally {
            DBUtils.closeStatement(pstmt);
            DBUtils.closeResultSet(resultSet);
        }
    }

    private void getSelectSql(Map<String, List<String>> tableColumns, Map<String, String> tableSelectSql) {

        for (String tableName : tableColumns.keySet()) {

            StringBuilder sb = new StringBuilder();

            List<String> columns = tableColumns.get(tableName);

            sb.append("SELECT ");

            sb.append(columns.get(0));

            for (int i = 1; i < columns.size(); i++) {
                sb.append(", ");
                sb.append(columns.get(i));
            }

            sb.append(" FROM  ");

            sb.append(tableName);

            tableSelectSql.put(tableName, sb.toString());
        }
    }

    /**
     * 创建（维护）辅助表
     *
     * @param bakConn 备份库conn
     */
    private void handleDiffInfoTable(Connection bakConn) throws Exception {

        log.debug("创建（维护）辅助表");

        String sqlExistTable = "SELECT COUNT(1) FROM USER_TABLES WHERE TABLE_NAME = '" + diffInfo + "'";

        int tableCount = runner.queryForInt(bakConn, sqlExistTable);

        if (tableCount == 0) {

            String createTable = "CREATE TABLE " + diffInfo + " (TB_ROW_ID RAW(16),TB_NM VARCHAR2(30),diff_DB_ID NUMBER(10))";
            runner.execute(bakConn, createTable);
            log.debug("创建 " + diffInfo);

        } else {
            log.debug("清空 " + diffInfo + "表数据");
            String truncateTable = "TRUNCATE TABLE " + diffInfo + " DROP STORAGE";
            runner.execute(bakConn, truncateTable);
        }

        sqlExistTable = "SELECT COUNT(1) FROM USER_TABLES WHERE TABLE_NAME = '" + geoMd5InfoBak + "'";

        tableCount = runner.queryForInt(bakConn, sqlExistTable);

        if (tableCount == 0) {

            String createTable = "CREATE TABLE " + geoMd5InfoBak + " (TB_ROW_ID RAW(16),MD5 VARCHAR2(32))";
            runner.execute(bakConn, createTable);
            log.debug("创建" + geoMd5InfoBak);
        } else {
            log.debug("清空 " + geoMd5InfoBak + "表数据");
            String truncateTable = "TRUNCATE TABLE " + geoMd5InfoBak + " DROP STORAGE";
            runner.execute(bakConn, truncateTable);
        }

        sqlExistTable = "SELECT COUNT(1) FROM USER_TABLES WHERE TABLE_NAME = '" + geoMd5InfoRegion + "'";

        tableCount = runner.queryForInt(bakConn, sqlExistTable);

        if (tableCount == 0) {
            String createTable = "CREATE TABLE " + geoMd5InfoRegion + " (TB_ROW_ID RAW(16),MD5 VARCHAR2(32))";
            runner.execute(bakConn, createTable);
            log.debug("创建 " + geoMd5InfoRegion);
        } else {
            log.debug("清空 " + geoMd5InfoRegion + "表数据");
            String truncateTable = "TRUNCATE TABLE " + geoMd5InfoRegion + " DROP STORAGE";
            runner.execute(bakConn, truncateTable);
        }
    }

    /**
     * 备份库创建MD5_CLOB函数
     *
     * @param bakConn 备份库conn
     */
    private void createMd5Function(Connection bakConn) throws Exception {

        String sql = "SELECT COUNT(1) FROM USER_OBJECTS WHERE OBJECT_TYPE = 'FUNCTION' AND OBJECT_NAME = 'MD5_CLOB'";

        int count = runner.queryForInt(bakConn, sql);

        if (count == 0) {

            String createFunction = "CREATE OR REPLACE FUNCTION MD5_CLOB( passwd IN CLOB) RETURN VARCHAR2 IS retval varchar2(32); BEGIN retval := utl_raw.cast_to_raw(DBMS_OBFUSCATION_TOOLKIT.MD5(INPUT_STRING => passwd)) ; RETURN retval; END;)";
            runner.execute(bakConn, createFunction);
            log.debug("备份大区库创建MD5_CLOB函数");
        }
    }

    /**
     * 扫描非几何字段
     *
     * @param conn         备份库conn
     * @param strSelectSql 查询sql
     */
    private int scanDiff(Connection conn, String strSelectSql, String tableName, String dbLinkName, int dbId)
            throws Exception {
        try {

            StringBuilder sb = new StringBuilder();

            sb.append("INSERT INTO ");
            sb.append(diffInfo);
            sb.append(" (TB_ROW_ID, TB_NM, diff_DB_ID) ");
            sb.append(" SELECT MINUS_INFO.ROW_ID TB_ROW_ID, '");

            sb.append(tableName);
            sb.append("' TB_NM, ");
            sb.append(dbId);
            sb.append(" diff_DB_ID  FROM ( ");
            sb.append(strSelectSql);
            if (dbId == regionDbId) {
                sb.append("@");
                sb.append(dbLinkName);
            }
            sb.append(" MINUS ");
            sb.append(strSelectSql);
            if (dbId == bakDbId) {
                sb.append("@");
                sb.append(dbLinkName);
            }
            sb.append(" ) MINUS_INFO");

            log.debug(sb.toString());

            return runner.update(conn, sb.toString());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new Exception("scanRegionDbDiff：" + e.getMessage(), e);
        }
    }

    /**
     * 扫描几何数据
     */
    private int scanGeoDiff(Connection conn, String tableName, int dbId)
            throws Exception {
        try {

            String sourceTable = geoMd5InfoRegion;

            String targetTable = geoMd5InfoBak;

            if (dbId == bakDbId) {

                sourceTable = geoMd5InfoBak;

                targetTable = geoMd5InfoRegion;
            }

            StringBuilder sb = new StringBuilder();

            sb.append("INSERT INTO ");

            sb.append(diffInfo);

            sb.append(" (TB_ROW_ID, TB_NM, diff_DB_ID) ");

            sb.append("SELECT MINUS_INFO.TB_ROW_ID TB_ROW_ID, '");

            sb.append(tableName);

            sb.append("' TB_NM, ");

            sb.append(dbId);

            sb.append(" diff_DB_ID FROM  ");

            sb.append(" (SELECT * FROM ");

            sb.append(sourceTable);

            sb.append("  MINUS  SELECT *  FROM  ");

            sb.append(targetTable);

            sb.append(" ) MINUS_INFO ");

            log.debug(sb.toString());

            return runner.update(conn, sb.toString());

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new Exception("scanRegionDbGeoDiff：" + e.getMessage(), e);
        }
    }


    /**
     * 备份库上创建大区库的db_link
     */
    private String createDbLink(OracleSchema bakSchema, OracleSchema regionSchema) throws Exception {


        try {
            DbLinkCreator cr = new DbLinkCreator();
            String dbLinkName = regionSchema.getConnConfig().getUserName() + "_" + "DayLogCheck";

            cr.create(dbLinkName, false, bakSchema.getPoolDataSource(), regionSchema.getConnConfig().getUserName(),
                    regionSchema.getConnConfig().getUserPasswd(), regionSchema.getConnConfig().getServerIp(),
                    String.valueOf(regionSchema.getConnConfig().getServerPort()), regionSchema.getConnConfig().getServiceName());
            return dbLinkName;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    // 删除DBLINK
    private void deleteDBLink(Connection conn, String dblinkName) throws SQLException {

        log.debug("drop dblink...");

        String sql = "DROP DATABASE LINK " + dblinkName;

        runner.update(conn, sql);

        log.debug("drop dblink success");
    }


    private void insertGeoMd5(String tableName, String dbLinkName, List<Integer> bakPageNum, List<Integer> pageNum) throws Exception {

        int allThreadCount = bakPageNum.size() + pageNum.size() - 2;

        if (allThreadCount < 1) {

            return;
        }

        final CountDownLatch latch = new CountDownLatch(allThreadCount);

        poolExecutor.addDoneSignal(latch);

        for (int i = 0; i < bakPageNum.size() - 1; i++) {

            JSONObject request = new JSONObject();

            request.put("tableName", tableName);

            request.put("sIndex", bakPageNum.get(i));

            request.put("eIndex", bakPageNum.get(i + 1));

            poolExecutor.execute(new insertDataThread(request, latch));
        }

        for (int i = 0; i < pageNum.size() - 1; i++) {

            JSONObject request = new JSONObject();

            request.put("tableName", tableName);

            request.put("sIndex", pageNum.get(i));

            request.put("eIndex", pageNum.get(i + 1));

            request.put("dbLinkName", dbLinkName);

            poolExecutor.execute(new insertDataThread(request, latch));
        }

        try {
            log.debug("等待各insert MD5任务执行完成");
            latch.await();
        } catch (InterruptedException e) {
            log.warn("线程被打断");
        }

        if (poolExecutor.getExceptions().size() > 0)
            throw new ServiceRtException("执行insert MD5值时发生异常" + poolExecutor
                    .getExceptions().get(0).getMessage(), poolExecutor
                    .getExceptions().get(0));

    }


    /**
     * 初始化线程池
     */
    private void initPoolExecutor() {
        try {
            poolExecutor = new VMThreadPoolExecutor(oneDBConnCount * 2, oneDBConnCount * 2, 3,
                    TimeUnit.SECONDS, new LinkedBlockingQueue(),
                    new ThreadPoolExecutor.CallerRunsPolicy());

        } catch (Exception e) {
            throw new ServiceRtException("初始化线程池错误:" + e.getMessage(), e);
        }
    }

    /**
     * 关闭线程池
     */
    private void shutDownPoolExecutor() {
        log.debug("关闭线程池");

        if (poolExecutor != null && !poolExecutor.isShutdown()) {

            poolExecutor.shutdownNow();

            try {
                while (!poolExecutor.isTerminated()) {
                    log.debug("等待线程结束：线程数为" + poolExecutor.getActiveCount());
                    Thread.sleep(2000);
                }
            } catch (InterruptedException e) {
                log.error("关闭线程池失败");
                throw new ServiceRtException("关闭线程池失败", e);
            }
        }
    }

    private Map<String, List<Integer>> getPageSizes(Connection conn, List<String> tableNames, String dbLinkName) throws Exception {

        Map<String, List<Integer>> tablePageSizes = new HashMap<>();

        for (String tableName : tableNames) {

            String sqlCount = "SELECT COUNT(1) FROM " + tableName + dbLinkName;

            int count = runner.queryForInt(conn, sqlCount);

            List<Integer> pageSizes = new ArrayList<>();

            pageSizes.add(0);

            if (count > insertCount) {

                int pageSize = count / oneDBConnCount;

                for (int i = 1; i < oneDBConnCount; i++) {

                    pageSizes.add(pageSize * i);
                }
            }

            pageSizes.add(count);

            tablePageSizes.put(tableName, pageSizes);
        }

        return tablePageSizes;

    }

    class insertDataThread implements Runnable {

        CountDownLatch latch = null;

        JSONObject request;

        insertDataThread(JSONObject request, CountDownLatch latch) {

            this.request = request;

            log.debug(request);

            this.latch = latch;
        }

        @Override
        public void run() {

            Connection conn = null;

            try {

                String dbLinkName = null;

                if (request.containsKey("dbLinkName")) {

                    dbLinkName = request.getString("dbLinkName");
                }

                String tableName = request.getString("tableName");

                int sIndex = request.getInt("sIndex");

                int eIndex = request.getInt("eIndex");

                if (!tablePrimarykey.containsKey(tableName)) {

                    log.debug("不支持：" + tableName);

                    return;
                }

                conn = DBConnector.getInstance().getConnectionById(bakDbId);

                int index = sIndex;

                while (index <= eIndex) {

                    int lastIndex = index + insertCount;

                    if (lastIndex > eIndex) {

                        lastIndex = eIndex;
                    }

                    insertMd5(conn, index, lastIndex, dbLinkName, tableName);

                    index += insertCount;
                }

                conn.commit();

            } catch (Exception e) {

                DbUtils.rollbackAndCloseQuietly(conn);

                throw new ThreadExecuteException(e);

            } finally {
                latch.countDown();
                DbUtils.closeQuietly(conn);
            }
        }

        /**
         * 插入md5信息
         */
        private void insertMd5(Connection conn, int firstIndex, int lastIndex, String dbLinkName, String tableName) throws Exception {

            if (firstIndex > lastIndex) {

                return;
            }

            String strBdLink = "";

            String insertTableName = geoMd5InfoBak;

            if (dbLinkName != null && !dbLinkName.isEmpty()) {

                strBdLink = "@" + dbLinkName;

                insertTableName = geoMd5InfoRegion;
            }

            StringBuilder strSb = new StringBuilder();

            strSb.append("INSERT INTO  ");

            strSb.append(insertTableName);

            strSb.append(" (TB_ROW_ID, MD5) ");

            strSb.append(" SELECT ROW_ID, MD5_CLOB(TMP1.GEOMETRY.GET_WKT()) MD5  FROM ( ");

            strSb.append(" SELECT * FROM (SELECT TT.*, ROWNUM AS ROWNO FROM (SELECT T.ROW_ID, T.GEOMETRY FROM ");

            strSb.append(tableName);

            strSb.append(strBdLink);

            strSb.append(" T ORDER BY T.");

            strSb.append(tablePrimarykey.get(tableName));

            strSb.append(" ) TT  WHERE ROWNUM <= ");

            strSb.append(lastIndex);

            strSb.append(" ) TABLE_ALIAS  WHERE TABLE_ALIAS.ROWNO >");

            strSb.append(firstIndex);

            strSb.append(" ) TMP1 ");

            log.debug(strSb.toString());

            long t = System.currentTimeMillis();

            QueryRunner runner = new QueryRunner();

            runner.execute(conn, strSb.toString());

            log.debug(tableName + "表插入MD5值 " + firstIndex + " -> " + lastIndex + " 用时：" + (System.currentTimeMillis() - t) / 1000.0 + "s");
        }

        private Map<String, String> tablePrimarykey = new HashMap<String, String>() {
            {
                put("ADAS_ITPLINK_GEOMETRY", "LINK_PID");
                put("ADAS_LINK", "LINK_PID");
                put("ADAS_LINK_GEOMETRY", "LINK_PID");
                put("ADAS_NODE", "NODE_PID");
                put("ADAS_RDLINK_GEOMETRY_DTM", "LINK_PID");
                put("AD_ADMIN", "REGION_ID");
                put("AD_FACE", "FACE_PID");
                put("AD_LINK", "LINK_PID");
                put("AD_LINK_100W", "LINK_PID");
                put("AD_NODE", "NODE_PID");
                put("AD_NODE_100W", "NODE_PID");
                put("CMG_BUILDFACE", "FACE_PID");
                put("CMG_BUILDLINK", "LINK_PID");
                put("CMG_BUILDNODE", "NODE_PID");
                put("CM_BUILDFACE", "FACE_PID");
                put("CM_BUILDLINK", "LINK_PID");
                put("CM_BUILDNODE", "NODE_PID");
                put("IX_ANNOTATION", "PID");
                put("IX_ANNOTATION_100W", "PID");
                put("IX_CROSSPOINT", "PID");
                put("IX_HAMLET", "PID");
                put("IX_IC", "PID");
                put("IX_NATGUD", "PID");
                put("IX_POI", "PID");
                put("IX_POINTADDRESS", "PID");
                put("IX_POI_ICON", "REL_ID");
                put("IX_POSTCODE", "POST_ID");
                put("IX_ROADNAME", "PID");
                put("IX_TOLLGATE", "PID");
                put("LC_FACE", "FACE_PID");
                put("LC_FACE_100W", "FACE_PID");
                put("LC_FACE_20W", "FACE_PID");
                put("LC_FACE_TOP", "FACE_PID");
                put("LC_LINK", "LINK_PID");
                put("LC_LINK_100W", "LINK_PID");
                put("LC_LINK_20W", "LINK_PID");
                put("LC_LINK_TOP", "LINK_PID");
                put("LC_NODE", "NODE_PID");
                put("LC_NODE_100W", "NODE_PID");
                put("LC_NODE_20W", "NODE_PID");
                put("LC_NODE_TOP", "NODE_PID");
                put("LU_FACE", "FACE_PID");
                put("LU_LINK", "LINK_PID");
                put("LU_NODE", "NODE_PID");
                put("PT_POI", "PID");
                put("PT_STRAND", "PID");
                put("RD_BRANCH_DETAIL", "DETAIL_ID");
                put("RD_ELECTRONICEYE", "PID");
                put("RD_GSC", "PID");
                put("RD_HGWG_LIMIT", "PID");
                put("RD_LINK", "LINK_PID");
                put("RD_LINK_WARNING", "PID");
                put("RD_MILEAGEPILE", "PID");
                put("RD_NATGUD_JUN_DETAIL", "DETAIL_ID");
                put("RD_NODE", "NODE_PID");
                put("RD_OBJECT", "PID");
                put("RD_SIGNBOARD", "SIGNBOARD_ID");
                put("RD_SIGNPOST", "PID");
                put("RD_SPEEDLIMIT", "PID");
                put("RD_SPEEDLIMIT_TRUCK", "PID");
                put("RW_LINK", "LINK_PID");
                put("RW_LINK_20W", "LINK_PID");
                put("RW_NODE", "NODE_PID");
                put("RW_NODE_20W", "NODE_PID");
                put("ZONE_FACE", "FACE_PID");
                put("ZONE_LINK", "LINK_PID");
                put("ZONE_NODE", "NODE_PID");
            }
        };
    }

}
