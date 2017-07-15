package com.navinfo.dataservice.engine.man.job.Day2Month;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.job.bean.ItemType;
import com.navinfo.navicommons.database.QueryRunner;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by wangshishuai3966 on 2017/7/14.
 */
public class Day2MonthUtils {
    private static Logger log = LoggerRepos.getLogger(Day2MonthUtils.class);

    public static Map<Integer, List<Integer>> getTaskInfo(Connection conn, long itemId, ItemType itemType) throws Exception {
        try {
            QueryRunner run = new QueryRunner();

            String selectSql = "";

            switch (itemType) {
                case PROJECT:
                    selectSql = "SELECT T.TASK_ID,T.REGION_ID FROM TASK T WHERE T.STATUS=0 AND T.TYPE=0 AND T.PROGRAM_ID=?";
                    break;
                case LOT:
                    selectSql = "SELECT T.TASK_ID,T.REGION_ID FROM TASK T WHERE T.STATUS=0 AND T.TYPE=0 AND T.LOT=?";
                    break;
            }
            ResultSetHandler<Map<Integer, List<Integer>>> rsHandler = new ResultSetHandler<Map<Integer, List<Integer>>>() {
                public Map<Integer, List<Integer>> handle(ResultSet rs) throws SQLException {
                    Map<Integer, List<Integer>> result = new HashMap<>();
                    while (rs.next()) {
                        int taskId = rs.getInt("task_id");
                        int regionId = rs.getInt("region_id");

                        if (!result.containsKey(regionId)) {
                            List<Integer> list = new ArrayList<>();
                            result.put(regionId, list);
                        }

                        List<Integer> list = result.get(regionId);
                        list.add(taskId);
                    }
                    return result;
                }
            };
            return run.query(conn, selectSql, rsHandler, itemId);
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("查询任务信息失败，原因为:" + e.getMessage(), e);
        }
    }

    public static Set<Integer> getTaskIdSet(Connection conn, long programId) throws Exception {
        try {
            QueryRunner run = new QueryRunner();

            String selectSql = "SELECT T.TASK_ID FROM TASK T WHERE T.STATUS=0 AND T.TYPE=0 AND T.PROGRAM_ID=?";
            ResultSetHandler<Set<Integer>> rsHandler = new ResultSetHandler<Set<Integer>>() {
                public Set<Integer> handle(ResultSet rs) throws SQLException {
                    Set<Integer> result = new HashSet<>();
                    while (rs.next()) {
                        int taskId = rs.getInt("task_id");
                        result.add(taskId);
                    }
                    return result;
                }
            };
            return run.query(conn, selectSql, rsHandler, programId);
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("查询任务信息失败，原因为:" + e.getMessage(), e);
        }
    }
}
