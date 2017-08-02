package com.navinfo.dataservice.engine.meta.translates;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.meta.translates.model.Chi2EngKeyword;
import com.navinfo.dataservice.engine.meta.translates.model.EngKeyword;
import com.navinfo.navicommons.database.sql.StringUtil;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;


/**
 * @Title: TranslateDictData
 * @Package: com.navinfo.dataservice.engine.meta.translates
 * @Description: 翻译-数据字典初始化
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

    private Map<String, String> dictSpecialMap = new LinkedHashMap<>();

    private Map<String, String> dictSymbolMap = new LinkedHashMap<>();

    private Map<String, List<String>> dictDictionary = new LinkedHashMap<>();

    private Map<String, String> dictFhWidth = new LinkedHashMap<>();

    private Map<String, List<Map<String, String>>> dictWord = new LinkedHashMap<>();

    private Map<String, String> dictWordIndex = new LinkedHashMap<>();

    private Map<String, Chi2EngKeyword> dictChi2Eng = new LinkedHashMap<>();

    private Map<String, List<EngKeyword>> dictEngKeyword = new LinkedHashMap<>();

    public synchronized Map<String, String> getDictSpecialMap() {
        if (dictSpecialMap.isEmpty())
            loadSpecial();
        return dictSpecialMap;
    }

    public synchronized Map<String, String> getDictSymbolMap() {
        if (dictSymbolMap.isEmpty())
            loadSymbol();
        return dictSymbolMap;
    }

    public synchronized Map<String, List<String>> getDictDictionary() {
        if (dictDictionary.isEmpty())
            loadDictionary();
        return dictDictionary;
    }

    public synchronized Map<String, String> getDictFhWidth() {
        if (dictFhWidth.isEmpty())
            loadFhWidth();
        return dictFhWidth;
    }

    public synchronized Map<String, List<Map<String, String>>> getDictWord() {
        if (dictWord.isEmpty())
            loadDictWord();
        return dictWord;
    }

    public synchronized Map<String, String> getDictWordIndex() {
        if (dictWordIndex.isEmpty())
            loadDictWord();
        return dictWordIndex;
    }

    public synchronized Map<String, Chi2EngKeyword> getDictChi2Eng() {
        if (dictChi2Eng.isEmpty())
            loadChi2Eng();
        return dictChi2Eng;
    }

    public synchronized Map<String, List<EngKeyword>> getDictEngKeyword() {
        if (dictEngKeyword.isEmpty())
            loadEngKeyword();
        return dictEngKeyword;
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
            Map<String, String> map = new LinkedHashMap<>();
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
            Map<String, String> map = new LinkedHashMap<>();
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
            String sql = "SELECT JT,dbms_lob.substr(WM_CONCAT(PY)) FROM (SELECT * FROM TY_NAVICOVPY_PY ORDER BY JT,PYORDER) GROUP BY JT ";
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
            Map<String, List<String>> map = new LinkedHashMap<>();
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
            Map<String, String> map = new LinkedHashMap<>();
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
            logger.warn("加载拼音字典表失败,已忽略...");
            logger.error(e.getMessage(), e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    private class ParseDictWordHandler implements ResultSetHandler<Map<String, List<Map<String, String>>>> {
        @Override
        public Map<String, List<Map<String, String>>> handle(ResultSet rs) throws SQLException {
            rs.setFetchSize(FETCH_SIZE);
            Map<String, List<Map<String, String>>> map = new LinkedHashMap<>();
            List<Map<String, String>> innerList = new ArrayList<>();
            Map<String, String> innerMap;
            String oldWord = null;
            while (rs.next()) {
                innerMap = new LinkedHashMap<>();
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
            String sql = "SELECT ID, CHIKEYWORDS, ENGKEYWORDS, PRIORITY, KIND, SOURCE, MEMO FROM SC_POINT_CHI2ENG_KEYWORD ";
            Map<String, Chi2EngKeyword> map = runner.query(conn, sql, new ParseChi2EngHandler());
            dictChi2Eng.putAll(map);
            logger.debug("加载关键字字典表成功，共" + dictChi2Eng.size() + "条数据");
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            logger.warn("加载拼音字典表失败，已忽略...");
            logger.error(e.getMessage(), e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    private class ParseChi2EngHandler implements ResultSetHandler<Map<String, Chi2EngKeyword>> {
        @Override
        public Map<String, Chi2EngKeyword> handle(ResultSet rs) throws SQLException {
            rs.setFetchSize(FETCH_SIZE);
            Map<String, Chi2EngKeyword> map = new LinkedHashMap<>();
            while (rs.next()) {
                Chi2EngKeyword chi2EngKeyword = new Chi2EngKeyword();

                long id = rs.getLong("ID");
                chi2EngKeyword.setId(id);

                String chikeywords = rs.getString("CHIKEYWORDS");
                chi2EngKeyword.setChikeywords(chikeywords);

                String engkeywords = rs.getString("ENGKEYWORDS");
                chi2EngKeyword.setEngkeywords(engkeywords);

                int priority = rs.getInt("PRIORITY");
                chi2EngKeyword.setPriority(priority);

                String kind = rs.getString("KIND");
                chi2EngKeyword.setKind(kind);

                String source = rs.getString("SOURCE");
                chi2EngKeyword.setSource(source);

                String memo = rs.getString("MEMO");
                chi2EngKeyword.setMemo(memo);

                map.put(chikeywords, chi2EngKeyword);
            }
            return map;
        }
    }


    public void loadEngKeyword() {
        QueryRunner runner = new QueryRunner();
        Connection conn = null;
        try {
            conn = DBConnector.getInstance().getMetaConnection();
            String sql = "SELECT ID, SPEC_WORDS, COMBINED_WORDS, SELECTED_WORDS, PRIORITY, RESULT, ENG_WORDS, TYPE FROM SC_POINT_SPEC_ENGKEYWORD ORDER BY TYPE, SPEC_WORDS DESC";
            Map<String, List<EngKeyword>> map = runner.query(conn, sql, new ParseEngKeywordHandler());
            dictEngKeyword.putAll(map);
            logger.debug("加载特殊词翻译字典表成功，共" + dictEngKeyword.size() + "条数据");
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            logger.warn("加载特殊词翻译字典表失败，已忽略...");
            logger.error(e.getMessage(), e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    private class ParseEngKeywordHandler implements ResultSetHandler<Map<String, List<EngKeyword>>> {
        @Override
        public Map<String, List<EngKeyword>> handle(ResultSet rs) throws SQLException {
            rs.setFetchSize(FETCH_SIZE);
            Map<String, List<EngKeyword>> map = new LinkedHashMap<>();

            StringBuilder regex;

            while (rs.next()) {
                EngKeyword engKeyword = new EngKeyword();

                long id = rs.getLong("ID");
                engKeyword.setId(id);

                String specWords = rs.getString("SPEC_WORDS");
                engKeyword.setSpecWords(specWords);

                String combinedWords = rs.getString("COMBINED_WORDS");
                regex = new StringBuilder();
                if (StringUtils.isNotEmpty(combinedWords)) {
                    regex.append("[");
                    for (String combinedWord : combinedWords.split("/")) {
                        if (StringUtils.equals("数字", combinedWord)) {
                            regex.append("0-9").append("０-９");
                            regex.append("零一二三四五六七八九");
                        }
                        if (StringUtils.equals("字母", combinedWord)) {
                            regex.append("a-z").append("A-Z");
                        }
                    }
                    regex.append(" 　]{1}");
                }
                engKeyword.setCombinedWords(regex.toString());

                String selectedWords = rs.getString("SELECTED_WORDS");
                engKeyword.setSelectedWords(selectedWords);

                String priority = rs.getString("PRIORITY");
                engKeyword.setPriority(priority);

                String kind = rs.getString("RESULT");
                engKeyword.setResult(kind);

                String engWords = rs.getString("ENG_WORDS");
                engKeyword.setEngWords(engWords);

                int type = rs.getInt("TYPE");
                engKeyword.setType(type);

                if (map.containsKey(specWords)) {
                    map.get(specWords).add(engKeyword);
                } else {
                    List<EngKeyword> list = new ArrayList<>();
                    list.add(engKeyword);
                    map.put(specWords, list);
                }
            }
            return map;
        }
    }

    public static void main(String[] args) {
        String regex = "[九-零]{1}";
        //System.out.println(Pattern.matches(regex, "零"));
        //System.out.println(Pattern.matches(regex, "一"));
        //System.out.println(Pattern.matches(regex, "壹"));

        System.out.println(Integer.valueOf('零'));
        System.out.println(Integer.valueOf('一'));
        System.out.println(Integer.valueOf('壹'));
        System.out.println(Integer.valueOf('九'));
        System.out.println(Integer.valueOf('玖'));

        for (int i = 19968; i <= 20661;i++) {
            System.out.println((char)i);
        }
    }
}
