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

    public static Map<String, Object> getCmsInfo(Connection conn, long itemId) throws Exception{
        try{
            QueryRunner run = new QueryRunner();
            String selectSql = "SELECT CMST.NAME CMS_NAME,"
                    + "       CMST.TASK_ID CMS_ID,"
                    + "       CMST.CREATE_USER_ID,"
                    + "       u.user_nick_name,"
                    + "       T.TASK_ID COLLECT_ID,"
                    + "       T.NAME COLLECT_NAME,"
                    + "       P.PHASE_ID,"
                    + "       R.MONTHLY_DB_ID,"
                    + "       C.PROVINCE_NAME,"
                    + "       C.CITY_NAME,"
                    + "       B.BLOCK_NAME,"
                    + "       B.WORK_PROPERTY,"
                    + "       B.WORK_TYPE"
                    + "  FROM TASK CMST, TASK T, BLOCK B, CITY C, REGION R,user_info u"
                    + " WHERE CMST.REGION_ID = R.REGION_ID"
                    + "   AND CMST.PROGRAM_ID = T.PROGRAM_ID"
                    + "   AND CMST.TASK_ID = ?"
                    + "   AND CMST.BLOCK_ID=T.BLOCK_ID"
                    + "   AND CMST.CREATE_USER_ID = u.user_ID(+)"
                    + "   AND T.TYPE = 0"
                    + "   AND CMST.BLOCK_ID = B.BLOCK_ID"
                    + "   AND B.CITY_ID = C.CITY_ID";
            ResultSetHandler<Map<String, Object>> rsHandler = new ResultSetHandler<Map<String, Object>>() {
                public Map<String, Object> handle(ResultSet rs) throws SQLException {
                    Map<String, Object> result=new HashMap<String, Object>();
                    if(rs.next()) {
                        result.put("cmsId", rs.getInt("CMS_ID"));
                        result.put("cmsName", rs.getString("CMS_NAME"));
                        result.put("createUserId", rs.getInt("CREATE_USER_ID"));
                        result.put("userNickName", rs.getString("user_nick_name"));
                        result.put("collectId", rs.getInt("COLLECT_ID"));
                        result.put("collectName", rs.getString("COLLECT_NAME"));
                        result.put("dbId", rs.getInt("MONTHLY_DB_ID"));
                        result.put("phaseId", rs.getInt("PHASE_ID"));
                        result.put("provinceName", rs.getString("PROVINCE_NAME"));
                        result.put("cityName", rs.getString("CITY_NAME"));
                        result.put("blockName", rs.getString("BLOCK_NAME"));
                        result.put("workProperty", rs.getString("WORK_PROPERTY"));
                        result.put("workType", rs.getString("WORK_TYPE"));
                    }
                    return result;
                }
            };
            return run.query(conn, selectSql, rsHandler, itemId);
        }catch(Exception e){
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("查询task下grid列表失败，原因为:"+e.getMessage(),e);
        }
    }
}
