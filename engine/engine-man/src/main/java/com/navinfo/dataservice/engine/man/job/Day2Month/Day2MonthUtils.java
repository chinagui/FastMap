package com.navinfo.dataservice.engine.man.job.Day2Month;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.job.bean.ItemType;
import com.navinfo.navicommons.database.QueryRunner;

/**
 * Created by wangshishuai3966 on 2017/7/14.
 */
public class Day2MonthUtils {
    private static Logger log = LoggerRepos.getLogger(Day2MonthUtils.class);

    public static Map<String, List<Integer>> getTaskInfo(Connection conn, long itemId, ItemType itemType) throws Exception {
        try {
            QueryRunner run = new QueryRunner();

            String selectSql = "";

            switch (itemType) {
                case PROJECT:
                    selectSql = "SELECT T.TASK_ID,T.REGION_ID FROM TASK T WHERE T.TYPE=0 AND T.PROGRAM_ID=?";
                    break;
                case LOT:
                    if(itemId==1){
                        selectSql = "SELECT T.TASK_ID,T.REGION_ID FROM TASK T WHERE T.TYPE=0 AND T.LOT=1";
                    }
                    else if(itemId==2) {
                        selectSql = "SELECT T.TASK_ID,T.REGION_ID FROM TASK T WHERE T.TYPE=0 AND T.LOT in (1,2)";
                    }else{
                        selectSql = "SELECT T.TASK_ID,T.REGION_ID FROM TASK T WHERE T.TYPE=0";
                    }
                    break;
            }
            ResultSetHandler<Map<String, List<Integer>>> rsHandler = new ResultSetHandler<Map<String, List<Integer>>>() {
                public Map<String, List<Integer>> handle(ResultSet rs) throws SQLException {
                    Map<String, List<Integer>> result = new HashMap<>();
                    while (rs.next()) {
                        int taskId = rs.getInt("task_id");
                        String regionId = rs.getString("region_id");

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
            if(itemType==ItemType.LOT) {
                return run.query(conn, selectSql, rsHandler);
            }else{
                return run.query(conn, selectSql, rsHandler, itemId);
            }
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("查询任务信息失败，原因为:" + e.getMessage(), e);
        }
    }

    public static Set<Integer> getTaskIdSet(Connection conn, long programId) throws Exception {
        try {
            QueryRunner run = new QueryRunner();

            String selectSql = "SELECT T.TASK_ID FROM TASK T WHERE T.TYPE=0 AND T.PROGRAM_ID=?";
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
