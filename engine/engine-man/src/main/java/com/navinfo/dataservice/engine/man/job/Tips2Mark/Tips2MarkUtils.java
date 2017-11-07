package com.navinfo.dataservice.engine.man.job.Tips2Mark;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.job.bean.ItemType;
import com.navinfo.navicommons.database.QueryRunner;

/**
 * Created by wangshishuai3966 on 2017/7/10.
 */
public class Tips2MarkUtils {
    private static Logger log = LoggerRepos.getLogger(Tips2MarkUtils.class);

    private static Map<String, Object> getTaskInfo(Connection conn, long taskId) throws Exception {
        try {
            QueryRunner run = new QueryRunner();
            String selectSql = "SELECT T.TASK_ID           COLLECT_ID," +
                    "       T.NAME              COLLECT_NAME," +
                    "       T.STATUS," +
                    "       T.TYPE," +
                    "       R.MONTHLY_DB_ID," +
                    "       C.PROVINCE_NAME," +
                    "       C.CITY_NAME," +
                    "       B.BLOCK_NAME" +
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
                        result.put("status", rs.getInt("status"));
                        result.put("type", rs.getInt("type"));
                    }
                    return result;
                }
            };
            return run.query(conn, selectSql, rsHandler, taskId);
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("查询任务信息失败，原因为:" + e.getMessage(), e);
        }
    }

    private static Map<String, Object> getProgramInfo(Connection conn, final long projectId) throws Exception {
        try {
            QueryRunner run = new QueryRunner();
            String selectSql = "SELECT DISTINCT P.PROGRAM_ID     COLLECT_ID," +
                    "       P.NAME              COLLECT_NAME," +
                    "       P.TYPE," +
                    "       P.STATUS," +
                    "       R.MONTHLY_DB_ID," +
                    "       I.ADMIN_NAME," +
                    "       (select listagg(task_id,',') within GROUP (order by task_id) " +
                    "        from TASK T1 where T1.status=0 and T1.type=0 and T1.program_id=P.program_id) TASKS" +
                    "  FROM PROGRAM           P," +
                    "       TASK              T," +
                    "       REGION            R," +
                    "       INFOR             I" +
                    " WHERE P.PROGRAM_ID = T.PROGRAM_ID" +
                    "   AND T.REGION_ID = R.REGION_ID" +
                    "   AND P.INFOR_ID  = I.INFOR_ID" +
                    "   AND P.PROGRAM_ID = ?";
            ResultSetHandler<Map<String, Object>> rsHandler = new ResultSetHandler<Map<String, Object>>() {
                public Map<String, Object> handle(ResultSet rs) throws SQLException {
                    Map<String, Object> result = new HashMap<String, Object>();
                    if (rs.next()) {
                        result.put("collectId", rs.getInt("COLLECT_ID"));
                        result.put("collectName", rs.getString("COLLECT_NAME"));
                        result.put("dbId", rs.getInt("MONTHLY_DB_ID"));
                        result.put("status", rs.getInt("STATUS"));
                        result.put("type", rs.getInt("TYPE"));
                        result.put("tasks", rs.getString("TASKS"));
                        String adminName = rs.getString("ADMIN_NAME");
                        String provinceName = "测试";
                        String cityName = "测试";
                        String blockName = "测试";
                        if (adminName != null) {
                            String[] names = adminName.split("\\|");
                            if (names.length > 0) {
                                provinceName = names[0];
                            }
                            if (names.length > 1) {
                                cityName = names[1];
                            }
                            if (names.length > 2) {
                                blockName = names[2];
                            }
                        }
                        result.put("provinceName", provinceName);
                        result.put("cityName", cityName);
                        result.put("blockName", blockName);
                    }
                    return result;
                }
            };
            return run.query(conn, selectSql, rsHandler, projectId);
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("查询项目信息失败，原因为:" + e.getMessage(), e);
        }
    }

    private static Map<String, Object> getSubTaskInfo(Connection conn, long subtaskId) throws Exception {
        try {
            QueryRunner run = new QueryRunner();
            String selectSql = "SELECT ST.SUBTASK_ID           COLLECT_ID," +
                    "       ST.NAME              COLLECT_NAME," +
                    "       ST.STAGE," +
                    "       ST.STATUS," +
                    "       R.MONTHLY_DB_ID," +
                    "       C.PROVINCE_NAME," +
                    "       C.CITY_NAME," +
                    "       B.BLOCK_NAME" +
                    "  FROM SUBTASK           ST," +
                    "       TASK              T," +
                    "       BLOCK             B," +
                    "       CITY              C," +
                    "       REGION            R" +
                    " WHERE ST.TASK_ID = T.TASK_ID" +
                    "   AND T.REGION_ID = R.REGION_ID" +
                    "   AND T.BLOCK_ID = B.BLOCK_ID" +
                    "   AND B.CITY_ID = C.CITY_ID" +
                    "   AND ST.SUBTASK_ID = ?";
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
                        result.put("status", rs.getInt("STATUS"));
                        result.put("type", rs.getInt("STAGE"));
                    }
                    return result;
                }
            };
            return run.query(conn, selectSql, rsHandler, subtaskId);
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("查询子任务信息失败，原因为:" + e.getMessage(), e);
        }
    }

    public static Map<String, Object> getItemInfo(Connection conn, long itemId, ItemType itemType) throws Exception {
        switch (itemType) {
            case PROJECT:
                return Tips2MarkUtils.getProgramInfo(conn, itemId);
            case TASK:
                return Tips2MarkUtils.getTaskInfo(conn, itemId);
            case SUBTASK:
                return Tips2MarkUtils.getSubTaskInfo(conn, itemId);
        }
        return null;
    }
}
