package com.navinfo.dataservice.engine.man.job.Tips2Mark;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangshishuai3966 on 2017/7/10.
 */
public class Tips2MarkUtils {
    private static Logger log = LoggerRepos.getLogger(Tips2MarkUtils.class);

    public static Map<String, Object> getTaskInfo(Connection conn, long taskId) throws Exception {
        try {
            QueryRunner run = new QueryRunner();
            String selectSql = "SELECT T.TASK_ID           COLLECT_ID," +
                    "       T.NAME              COLLECT_NAME," +
                    "       R.MONTHLY_DB_ID," +
                    "       C.PROVINCE_NAME," +
                    "       C.CITY_NAME," +
                    "       B.BLOCK_NAME," +
                    "       B.WORK_PROPERTY," +
                    "       B.WORK_TYPE" +
                    "  FROM TASK              T," +
                    "       BLOCK             B," +
                    "       CITY              C," +
                    "       REGION            R" +
                    " WHERE T.REGION_ID = R.REGION_ID" +
                    "   AND T.BLOCK_ID = B.BLOCK_ID" +
                    "   AND B.CITY_ID = C.CITY_ID" +
                    "   AND T.TASK_ID = ?";
            ResultSetHandler<Map<String, Object>> rsHandler = new ResultSetHandler<Map<String, Object>>() {
                public Map<String, Object> handle(ResultSet rs) throws SQLException {
                    Map<String, Object> result = new HashMap<String, Object>();
                    if (rs.next()) {
                        result.put("collectId", rs.getInt("COLLECT_ID"));
                        result.put("collectName", rs.getString("COLLECT_NAME"));
                        result.put("dbId", rs.getInt("MONTHLY_DB_ID"));
                        result.put("provinceName", rs.getString("PROVINCE_NAME"));
                        result.put("cityName", rs.getString("CITY_NAME"));
                        result.put("blockName", rs.getString("BLOCK_NAME"));
                    }
                    return result;
                }
            };
            return run.query(conn, selectSql, rsHandler, taskId);
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("查询task信息失败，原因为:" + e.getMessage(), e);
        }
    }

    public static Map<String, Object> getProjectInfo(Connection conn, long projectId) throws Exception {
        try {
            QueryRunner run = new QueryRunner();
            String selectSql = "SELECT T.TASK_ID           COLLECT_ID," +
                    "       T.NAME              COLLECT_NAME," +
                    "       R.MONTHLY_DB_ID," +
                    "       C.PROVINCE_NAME," +
                    "       C.CITY_NAME," +
                    "       B.BLOCK_NAME," +
                    "       B.WORK_PROPERTY," +
                    "       B.WORK_TYPE" +
                    "  FROM TASK              T," +
                    "       BLOCK             B," +
                    "       CITY              C," +
                    "       REGION            R" +
                    " WHERE T.REGION_ID = R.REGION_ID" +
                    "   AND T.BLOCK_ID = B.BLOCK_ID" +
                    "   AND B.CITY_ID = C.CITY_ID" +
                    "   AND T.TASK_ID = ?";
            ResultSetHandler<Map<String, Object>> rsHandler = new ResultSetHandler<Map<String, Object>>() {
                public Map<String, Object> handle(ResultSet rs) throws SQLException {
                    Map<String, Object> result = new HashMap<String, Object>();
                    if (rs.next()) {
                        result.put("collectId", rs.getInt("COLLECT_ID"));
                        result.put("collectName", rs.getString("COLLECT_NAME"));
                        result.put("dbId", rs.getInt("MONTHLY_DB_ID"));
                        result.put("provinceName", rs.getString("PROVINCE_NAME"));
                        result.put("cityName", rs.getString("CITY_NAME"));
                        result.put("blockName", rs.getString("BLOCK_NAME"));
                    }
                    return result;
                }
            };
            return run.query(conn, selectSql, rsHandler, projectId);
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("查询task信息失败，原因为:" + e.getMessage(), e);
        }
    }
    public static Map<String, Object> getSubTaskInfo(Connection conn, long subtaskId) throws Exception {
        try {
            QueryRunner run = new QueryRunner();
            String selectSql = "SELECT T.TASK_ID           COLLECT_ID," +
                    "       T.NAME              COLLECT_NAME," +
                    "       R.MONTHLY_DB_ID," +
                    "       C.PROVINCE_NAME," +
                    "       C.CITY_NAME," +
                    "       B.BLOCK_NAME," +
                    "       B.WORK_PROPERTY," +
                    "       B.WORK_TYPE" +
                    "  FROM TASK              T," +
                    "       BLOCK             B," +
                    "       CITY              C," +
                    "       REGION            R" +
                    " WHERE T.REGION_ID = R.REGION_ID" +
                    "   AND T.BLOCK_ID = B.BLOCK_ID" +
                    "   AND B.CITY_ID = C.CITY_ID" +
                    "   AND T.TASK_ID = ?";
            ResultSetHandler<Map<String, Object>> rsHandler = new ResultSetHandler<Map<String, Object>>() {
                public Map<String, Object> handle(ResultSet rs) throws SQLException {
                    Map<String, Object> result = new HashMap<String, Object>();
                    if (rs.next()) {
                        result.put("collectId", rs.getInt("COLLECT_ID"));
                        result.put("collectName", rs.getString("COLLECT_NAME"));
                        result.put("dbId", rs.getInt("MONTHLY_DB_ID"));
                        result.put("provinceName", rs.getString("PROVINCE_NAME"));
                        result.put("cityName", rs.getString("CITY_NAME"));
                        result.put("blockName", rs.getString("BLOCK_NAME"));
                    }
                    return result;
                }
            };
            return run.query(conn, selectSql, rsHandler, subtaskId);
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("查询task信息失败，原因为:" + e.getMessage(), e);
        }
    }
}
