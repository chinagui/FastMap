package com.navinfo.dataservice.dao.tranlsate.selector;

import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Title: TranslateSelector
 * @Package: com.navinfo.dataservice.dao.tranlsate.selector
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 10/12/2017
 * @Version: V1.0
 */
public class TranslateSelector {

    private Connection conn;

    public TranslateSelector(Connection conn) {
        this.conn = conn;
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
                "  FROM (SELECT T.*, ROWNUM AS ROWNM FROM TRANSLATE_LOG T WHERE USER_ID = ? AND ROWNUM < ?)" +
                " WHERE ROWNM > ?";

        ResultSetHandler<Page> handler = new ResultSetHandler<Page>() {
            @Override
            public Page handle(ResultSet rs) throws SQLException {
                return null;
            }
        };
        QueryRunner runner = new QueryRunner();
        return runner.query(conn, sql, new Object[]{userId, pageEndNum, pageStartNum}, handler);
    }

    private boolean checkParams(JSONObject params, String[] check) {
        for (String ck : check) {
            if (!params.containsKey(ck)) {
                return false;
            }
        }
        return true;
    }
}
