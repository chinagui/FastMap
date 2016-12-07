package com.navinfo.dataservice.engine.meta.translate;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by chaixin on 2016/11/29 0029.
 */
public class TranslateDictData {

    private static Logger logger = Logger.getLogger(TranslateDictData.class);

    private static class SingletonHolder {
        private static final TranslateDictData INSTANCE = new TranslateDictData();
    }

    public static final TranslateDictData getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private TranslateDictData() {
//        loadData();
    }

    public void loadData(){
        logger.debug("==================INIT_TRANSLATE_DATA_S==================");
        loadSpecial();
        loadSymbol();
        loadDictionary();
        loadFhWidth();
        loadDictWord();
        loadChi2Eng();
        logger.debug("==================INIT_TRANSLATE_DATA_E==================");
    }

    private static Integer FETCH_SIZE = 3000;

    private Map<String, String> dictSpecialMap = new HashMap<>();

    private Map<String, String> dictSymbolMap = new HashMap<>();

    private Map<String, List<String>> dictDictionary = new HashMap<>();

    private Map<String, String> dictFhWidth = new HashMap<>();

    private Map<String, List<Map<String, String>>> dictWord = new HashMap<>();

    private Map<String, String> dictWordIndex = new HashMap<>();

    private Map<String, String> dictChi2Eng = new HashMap<>();

    public Map<String, String> getDictSpecialMap() {
        if (dictSpecialMap.isEmpty())
            loadSpecial();
        return dictSpecialMap;
    }

    public Map<String, String> getDictSymbolMap() {
        if (dictSymbolMap.isEmpty())
            loadSymbol();
        return dictSymbolMap;
    }

    public Map<String, List<String>> getDictDictionary() {
        if (dictDictionary.isEmpty())
            loadDictionary();
        return dictDictionary;
    }

    public Map<String, String> getDictFhWidth() {
        if (dictFhWidth.isEmpty())
            loadFhWidth();
        return dictFhWidth;
    }

    public Map<String, List<Map<String, String>>> getDictWord() {
        if (dictWord.isEmpty())
            loadDictWord();
        return dictWord;
    }

    public Map<String, String> getDictWordIndex() {
        if (dictWordIndex.isEmpty())
            loadDictWord();
        return dictWordIndex;
    }

    public Map<String, String> getDictChi2Eng() {
        if (dictChi2Eng.isEmpty())
            loadChi2Eng();
        return dictChi2Eng;
    }

    private void loadSpecial() {
        QueryRunner runner = new QueryRunner();
        Connection conn = null;
        try {
            conn = DBConnector.getInstance().getMetaConnection();
            String sql = "SELECT HZ,PY FROM SPECIAL";
            Map<String, String> map = runner.query(conn, sql, new ParseSpecialHandler());
            dictSpecialMap.putAll(map);
            logger.debug("加载特殊对应表完成，共" + map.size() + "条数据");
        } catch (SQLException e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            logger.error(e.getMessage(), e);
            logger.warn("加载特殊对应表失败,已忽略...", e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    private class ParseSpecialHandler implements ResultSetHandler<Map<String, String>> {
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

    private void loadSymbol() {
        QueryRunner runner = new QueryRunner();
        Connection conn = null;
        try {
            conn = DBConnector.getInstance().getMetaConnection();
            String sql = "SELECT SYMBOL,CORRECT FROM WORD_SYMBOL";
            Map<String, String> map = runner.query(conn, sql, new ParseSymbolHandler());
            dictSymbolMap.putAll(map);
            logger.debug("加载删除符号表成功，共" + map.size() + "条数据");
        } catch (SQLException e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            logger.error(e.getMessage(), e);
            logger.warn("加载删除符号表失败，已忽略...", e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    private class ParseSymbolHandler implements ResultSetHandler<Map<String, String>> {
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

    private void loadDictionary() {
        QueryRunner runner = new QueryRunner();
        Connection conn = null;
        try {
            conn = DBConnector.getInstance().getMetaConnection();
            String sql = "SELECT JT,WM_CONCAT(PY) FROM (SELECT * FROM TY_NAVICOVPY_PY ORDER BY JT,PYORDER) GROUP BY JT ";
            Map<String, List<String>> map = runner.query(conn, sql, new ParseDictonaryHandler());
            dictDictionary.putAll(map);
            logger.debug("加载字典表成功，共" + map.size() + "条数据");
        } catch (SQLException e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            logger.error(e.getMessage(), e);
            logger.warn("加载字典表失败，已忽略...", e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }

    }

    private class ParseDictonaryHandler implements ResultSetHandler<Map<String, List<String>>> {
        @Override
        public Map<String, List<String>> handle(ResultSet rs) throws SQLException {
            rs.setFetchSize(FETCH_SIZE);
            Map<String, List<String>> map = new HashMap<>();
            while (rs.next()) {
                map.put(rs.getString(1), Arrays.asList(rs.getString(2).split(",")));
            }
            return map;
        }
    }

    private void loadFhWidth() {
        QueryRunner runner = new QueryRunner();
        Connection conn = null;
        try {
            conn = DBConnector.getInstance().getMetaConnection();
            String sql = "SELECT HALF_WIDTH, FULL_WIDTH FROM TY_CHARACTER_FULL2HALF";
            Map<String, String> map = runner.query(conn, sql, new ParseFhWidthHandler());
            dictFhWidth.putAll(map);
            logger.debug("加载全半角字符对照表成功,共" + map.size() + "条数据");
        } catch (SQLException e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            logger.error(e.getMessage(), e);
            logger.warn("加载全半角字符对照表失败，已忽略...");
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    private class ParseFhWidthHandler implements ResultSetHandler<Map<String, String>> {
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

    private void loadDictWord() {
        QueryRunner runner = new QueryRunner();
        Connection conn = null;
        try {
            conn = DBConnector.getInstance().getMetaConnection();
            String sql = "SELECT WORD,PY,PY2,ADMINAREA FROM TY_NAVICOVPY_WORD ORDER BY WORD";
            dictWord = runner.query(conn, sql, new ParseDictWordHandler());
            logger.debug("加载拼音字典表成功，共" + dictWord.size() + "条数据");
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            logger.error(e.getMessage(), e);
            logger.warn("加载拼音字典表失败,已忽略...");
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    private class ParseDictWordHandler implements ResultSetHandler<Map<String, List<Map<String, String>>>> {
        @Override
        public Map<String, List<Map<String, String>>> handle(ResultSet rs) throws SQLException {
            rs.setFetchSize(FETCH_SIZE);
            Map<String, List<Map<String, String>>> map = new HashMap<>();
            List<Map<String, String>> innerList = new ArrayList<>();
            Map<String, String> innerMap;
            String oldWord = null;
            while (rs.next()) {
                innerMap = new HashMap<>();
                String word = rs.getString(1);
                innerMap.put("py", rs.getString(2));
                innerMap.put("py2", rs.getString(3));
                innerMap.put("adminArea", rs.getString(4));
                if (null == oldWord || word.equals(oldWord)) {
                    innerList.add(innerMap);
                    oldWord = word;
                } else {
                    map.put(oldWord, innerList);
                    innerList = new ArrayList<>();
                    oldWord = word;
                    innerList.add(innerMap);
                }
                for (int i = 0; i < word.length(); i++) {
                    String tmpStr = word.substring(0, i);
                    dictWordIndex.put(tmpStr, tmpStr);
                }
            }
            return map;
        }
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
}
