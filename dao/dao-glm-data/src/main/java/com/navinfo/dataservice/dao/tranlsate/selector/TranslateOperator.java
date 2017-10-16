package com.navinfo.dataservice.dao.tranlsate.selector;

import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.tranlsate.entity.TranslateLog;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Title: TranslateOperator
 * @Package: com.navinfo.dataservice.dao.tranlsate.selector
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 10/12/2017
 * @Version: V1.0
 */
public class TranslateOperator {

    private Connection conn;

    public TranslateOperator(Connection conn) {
        this.conn = conn;
    }

    public TranslateLog get(JSONObject params) throws Exception{
        if (!checkParams(params, new String[]{"id"})) {
            throw new ServiceException("参数错误!");
        }

        final String id = params.getString("id");

        String sql = "SELECT * FROM " + TranslateLog.TABLE_NAME + " WHERE ID = ?";
        QueryRunner runner = new QueryRunner();

        return runner.query(conn, sql, new Object[]{id}, new ResultSetHandler<TranslateLog>() {
            @Override
            public TranslateLog handle(ResultSet rs) throws SQLException {
                TranslateLog log = new TranslateLog();
                while (rs.next()) {
                    log.setId(rs.getString("ID"));
                    log.setFileName(rs.getString("FILE_NAME"));
                    log.setDownloadUrl(rs.getString("DOWNLOAD_PATH") + rs.getString("DOWNLOAD_FILE_NAME"));
                }
                return log;
            }
        });
    }

    public Page list(JSONObject params) throws Exception{
        if (!checkParams(params, new String[]{"userId", "pageSize", "pageNum"})) {
            throw new ServiceException("参数错误!");
        }

        final long userId = params.getLong("userId");
        final int pageSize = params.getInt("pageSize");
        final int pageNum = params.getInt("pageNum");

        long pageStartNum = (pageNum - 1) * pageSize + 1;
        long pageEndNum = pageNum * pageSize;

        String sql = "SELECT *" + 
                "  FROM (SELECT T.ID," + 
                "               T.FILE_NAME," + 
                "               T.USER_ID," + 
                "               T.DOWNLOAD_PATH," +
                "               T.DOWNLOAD_FILE_NAME," +
                "               T.JOB_ID," +
                "               J.CREATE_TIME," + 
                "               J.END_TIME," + 
                "               J.STATUS," + 
                "               ROWNUM AS ROWNM" + 
                "          FROM TRANSLATE_LOG T, JOB_INFO J" + 
                "         WHERE T.USER_ID = ?" + 
                "           AND T.JOB_ID = J.JOB_ID" + 
                "           AND ROWNUM < ?)" + 
                " WHERE ROWNM > ?";

        QueryRunner runner = new QueryRunner();
        return runner.query(conn, sql, new Object[]{userId, pageEndNum, pageStartNum}, new TranslateHandler(pageNum, pageSize));
    }

    public void save(JSONObject json) throws SQLException {
        String sql = "INSERT INTO TRANSLATE_LOG " +
                TranslateLog.TABLE_NAME +
                "(ID, FILE_NAME, USER_ID, DOWNLOAD_PATH, DOWNLOAD_FILE_NAME, JOB_ID) VALUES(?,?,?,?,?,?)";

        List<Object> objects = new ArrayList<>();
        objects.add(UuidUtils.genUuid());
        objects.add(json.getString("fileName"));
        objects.add(json.getLong("userId"));
        objects.add(json.getString("downloadPath"));
        objects.add(json.getString("downloadFileName"));
        objects.add(json.getInt("jobId"));

        QueryRunner runner = new QueryRunner();
        runner.update(conn, sql, objects);
    }

    private boolean checkParams(JSONObject params, String[] check) {
        for (String ck : check) {
            if (!params.containsKey(ck)) {
                return false;
            }
        }
        return true;
    }


    class TranslateHandler implements ResultSetHandler<Page> {

        private int pageNum;

        private int pageSize;

        public TranslateHandler(int pageNum, int pageSize) {
            this.pageNum = pageNum;
            this.pageSize = pageSize;
        }

        @Override
        public Page handle(ResultSet rs) throws SQLException {
            Page page = new Page(pageNum);
            page.setPageSize(pageSize);

            List<TranslateLog> logs = new ArrayList<>();

            int total = 0;
            while (rs.next()) {
                if(total==0){
                    total=rs.getInt("TOTAL_RECORD_NUM_");
                }

                TranslateLog log = new TranslateLog();
                log.setId(rs.getString("ID"));
                log.setFileName(rs.getString("FILE_NAME"));
                log.setStartDate(rs.getDate("CREATE_TIME"));
                log.setEndDate(rs.getDate("END_TIME"));
                log.setUserId(rs.getLong("USER_ID"));
                log.setDownloadUrl(rs.getString("DOWNLOAD_PATH") + rs.getString("DOWNLOAD_FILE_NAME"));
                log.setJobId(rs.getInt("JOB_ID"));
                log.setState(rs.getInt("STATUS"));

                logs.add(log);
            }
            return null;
        }
    }
}
