package com.navinfo.navicommons.database;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.navinfo.navicommons.exception.ServiceException;

/**
 * @author liuqing
 */
public class SqlNamedQuery {

    private static SqlNamedQuery instance = null;
    private Logger log = Logger.getLogger(SqlNamedQuery.class);
    private Map<String, SqlQueryDefinition> definitions = new HashMap<String, SqlQueryDefinition>();

    public static synchronized SqlNamedQuery getInstance() {
        if (instance == null) {
            instance = new SqlNamedQuery();
        }
        return instance;
    }

    private SqlNamedQuery() {
        try {
//            ClassPathFinder resolover = new ClassPathFinder();
            PathMatchingResourcePatternResolver resolover = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolover.getResources("classpath*:/*-SQLNamedQuery.xml");
            for (Resource resource : resources) {
                readSqlQueryDefinition(resource.getURL());
            }
        } catch (Exception e) {
            throw new SqlNameParseException(e);
        }

    }

    public SqlQueryDefinition getSqlQueryDefinition(String id) {
        return definitions.get(id);
    }

    public String getSql(String id) {
        String sql = getSqlQueryDefinition(id).getSql();
        log.debug(sql);
        return sql;
    }

   

    @SuppressWarnings("unchecked")
    private void readSqlQueryDefinition(URL url) throws Exception {
    	SAXReader reader = new SAXReader();
        Document doc  = reader.read(url);
        Element root = doc.getRootElement();
        List<Element> sqlQueryList = root.elements("sql-query");
        for (Element sqlQuery : sqlQueryList) {
            String id = sqlQuery.attribute("id").getValue();
            Element sqlElement = sqlQuery.element("sql");
            String sql = sqlElement.getTextTrim();
            definitions.put(id, new SqlQueryDefinition(sql));
        }
       
    }

    public static void main(String args[]) {
    }

    public class SqlQueryDefinition {

        private int getParameterCount(String sql) {
            int idx = sql.indexOf("?");
            int i = 0;
            if (idx < 0) {
                return 0;
            } else {
                while (idx > 0) {
                    i++;
                    sql = sql.substring(idx + 1);
                    idx = sql.indexOf("?");
                }

            }
            return i;
        }

        public SqlQueryDefinition(String sql) {
            this.sql = sql;
            this.parameteCount = getParameterCount(sql);
        }

        private String sql;
        private Integer parameteCount;

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        public Integer getParameteCount() {
            return parameteCount;
        }

        public void setParameteCount(Integer parameteCount) {
            this.parameteCount = parameteCount;
        }

    }

    public static String getNamedSql(String key) throws ServiceException {
        String sql = null;
        SqlNamedQuery sqlNamedQuery = null;
        try {
            sqlNamedQuery = getInstance();
        } catch (Exception e) {
            throw new ServiceException("读取sql配置时出错", e);
        }
        sql = sqlNamedQuery.getSql(key);
        if (sql == null)
            throw new ServiceException("[" + key + "]所标识的sql不存在");
        return sql;
    }

}
