package com.navinfo.dataservice.engine.meta.translates;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Title: SplitUtil
 * @Package: com.navinfo.dataservice.engine.meta.translates
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/3/30
 * @Version: V1.0
 */
public class TranslateDictData {



    private static Logger logger = Logger.getLogger(TranslateDictData.class);

    private static class SingletonHolder {
        private static final TranslateDictData INSTANCE = new TranslateDictData();
    }

    public static final TranslateDictData getInstance() {
        return SingletonHolder.INSTANCE;
    }


    public void loadData(){
        logger.debug("==================INIT_TRANSLATE_DATA_S==================");
        loadChi2Eng();
        logger.debug("==================INIT_TRANSLATE_DATA_E==================");
    }

    private static Integer FETCH_SIZE = 3000;

    private Map<String, String> dictChi2Eng = new HashMap<>();

    public synchronized Map<String, String> getDictChi2Eng() {
        if (dictChi2Eng.isEmpty())
            loadChi2Eng();
        return dictChi2Eng;
    }

    public void loadChi2Eng() {
        QueryRunner runner = new QueryRunner();
        Connection conn = null;
        try {
            conn = DBConnector.getInstance().getMetaConnection();
            String sql = "SELECT CHIKEYWORDS, ENGKEYWORDS FROM SC_POINT_CHI2ENG_KEYWORD ";
            Map<String, String> map = runner.query(conn, sql, new ParseChi2EngHandler());
            dictChi2Eng.putAll(map);
            logger.debug("加载关键字字典表成功，共" + dictChi2Eng.size() + "条数据");
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            logger.debug("加载拼音字典表失败，已忽略...");
            logger.debug(e.getMessage(), e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    private class ParseChi2EngHandler implements ResultSetHandler<Map<String, String>> {
        @Override
        public Map<String, String> handle(ResultSet rs) throws SQLException {
            rs.setFetchSize(FETCH_SIZE);
            Map<String, String> map = new HashMap<>();
            while (rs.next()) {
                map.put(rs.getString(1), rs.getString(2));
            }
            return map;
        }
    }

    class loadData implements Runnable{

        @Override
        public void run() {

        }
    }
}
