package com.navinfo.dataservice.expcore.sql;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.navinfo.dataservice.bizcommons.sql.ExpSQL;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.expcore.exception.ExportException;


/**
 * 解析sql文件 ，并按sql的执行顺序重新组织。
 * 查询语句会动态的分配一个最大的执行步骤。
 *
 * @author liuqing
 */
public class ExpXMLSQLManager {
    private static Logger log = Logger.getLogger(ExpXMLSQLManager.class);
    private static ExpXMLSQLManager manager = null;

    private static Map<String, Map<String, Map<Integer, List<ExpSQL>>>> versionFileSqlMap = new HashMap<String, Map<String, Map<Integer, List<ExpSQL>>>>();





    private static Set<String> allTempTable = new TreeSet<String>();


    public static synchronized ExpXMLSQLManager getInstance() {
        if (manager == null) {
            manager = new ExpXMLSQLManager();
        }
        return manager;
    }

    /**
     * 解析当前版本下的导出sql
     *
     * @param gdbVersion
     * @return
     */
    private Map<String, Map<Integer, List<ExpSQL>>> parseFileSqlMapByVersion(String gdbVersion) throws ExportException {
        return sortByExecStep(parseConfigFile(gdbVersion));
    }

    public Map<String, Map<Integer, List<ExpSQL>>> getFileSqlMap(String gdbVersion)  throws ExportException{
        Map<String, Map<Integer, List<ExpSQL>>> result = versionFileSqlMap.get(gdbVersion);
        if (result == null) {
            result = parseFileSqlMapByVersion(gdbVersion);
            versionFileSqlMap.put(gdbVersion, result);
        }
        return result;
    }

    private ExpXMLSQLManager() {


    }




    /**
     * 将sql配置文件的内容根据执行步骤重新排序和重新组织结构
     *
     * @param fileMap <文件名，featureElementList>
     * @return
     */
    private Map<String, Map<Integer, List<ExpSQL>>> sortByExecStep(Map<String, FileContent> fileMap) {
        Map<String, Map<Integer, List<ExpSQL>>> newFileMap = new HashMap<String, Map<Integer, List<ExpSQL>>>();
        Iterator<Entry<String, FileContent>> fileMapIte = fileMap.entrySet().iterator();
        while (fileMapIte.hasNext()) {
            Entry<String, FileContent> fileMapEntry = fileMapIte.next();
            String fileName = fileMapEntry.getKey();
            FileContent featureList = fileMapEntry.getValue();
            //xml:sqls/feature
            //解析xml的feature节点
            Map<Integer, List<ExpSQL>> sqlMap = parseFeatureElement(featureList);
            newFileMap.put(fileName, sqlMap);
        }
        return newFileMap;
    }

    /**
     * 解析xml的feature节点
     *
     * @param fileContent
     * @return //<step,sqlList<ExpSQL>>
     */
    private Map<Integer, List<ExpSQL>> parseFeatureElement(FileContent fileContent) {
        //TreeMap 会自动按step排序
        Map<Integer, List<ExpSQL>> sqlMap = new TreeMap<Integer, List<ExpSQL>>();

        List<Element> featureList = fileContent.getAssembledRootElements();
        for (Element feature : featureList) {
            //取得feature的下级
            List<Element> stepList = feature.elements("step");
            //解析stepElement
            pareseStepElement(sqlMap, stepList, fileContent);


        }
        return sqlMap;
    }

    /**
     * 解析stepElement
     *
     * @param sqlMap
     * @param stepList
     * @param fileContent
     */
    private void pareseStepElement(Map<Integer, List<ExpSQL>> sqlMap, List<Element> stepList, FileContent fileContent) {
        Integer step = 0;
        for (Element stepElement : stepList) {
            // 步骤控制符号
            step = Integer.valueOf(stepElement.attributeValue("value"));


            //log.debug("///////////////step"+step);
            //先判断step在map中是否存在
            List<ExpSQL> sqlList = sqlMap.get(step);
            if (sqlList == null)
                sqlList = new ArrayList<ExpSQL>();
            List<Element> sqlElementList = stepElement.elements("sql");
            // 解析 xml sql Element
            parseSqlElement(sqlList, sqlElementList, fileContent);

            sqlMap.put(step, sqlList);
        }
    }

    /**
     * 解析 xml sql Element
     *
     * @param sqlList
     * @param sqlElementList
     * @param fileContent
     */
    private void parseSqlElement(List<ExpSQL> sqlList, List<Element> sqlElementList, FileContent fileContent) {
        if (sqlElementList == null)
            return;
        for (int i = 0; i < sqlElementList.size(); i++) {
            Element element = sqlElementList.get(i);
            //获取sql Element 的Id号
            String id = element.attributeValue("id");
            String condition = element.attributeValue("condition");
            String sqlType = element.attributeValue("sqlType");
            String sqlExtendType = element.attributeValue("sqlExtendType");
            
			
            String sql = element.getTextTrim();
            /*if(sqlType!=null){
            	log.debug(sqlType+":"+sql);
            }*/
            
            if (!fileContent.excludeSql(id)) {
                ExpSQL expSQL = new ExpSQL(id, sql);
                expSQL.setCondition(condition);
                expSQL.setSqlType(sqlType);
               // expSQL.setSqlExtendType(sqlExtendType);
//                log.debug(expSQL.getSql());
                //TODO:正式环境请注释生成临时表脚本的功能
               //getTempTable(expSQL.getSql());
                sqlList.add(expSQL);
            }

        }
    }


    /**
     * 读取sql文件 ,并将文件内容中的import=file 进行实际内容的替换
     *
     * @throws Exception
     * @return返回Map<文件名，FileContent>
     */
    @SuppressWarnings("unchecked")
    public Map<String, FileContent> parseConfigFile(String gdbVersion) throws ExportException{
        Map<String, FileContent> fileContentMap = new HashMap<String, FileContent>();
        try {
            Map<String, List<Element>> fileMap = readFileContent(gdbVersion);
            Iterator<Entry<String, List<Element>>> fileMapIte = fileMap.entrySet().iterator();
            while (fileMapIte.hasNext()) {
                Entry<String, List<Element>> fileMapEntry = fileMapIte.next();
                String fileName = fileMapEntry.getKey();
//                log.debug("parese file:" + fileName);
                List<Element> rootChildren = fileMapEntry.getValue();
                FileContent fileContent = new FileContent(rootChildren);
                assembleImportFile(fileMap, fileContent);
                fileContentMap.put(fileName, fileContent);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ExportException("加载导出脚本时出错。",e);
        }
        return fileContentMap;
    }


    /**
     * 读取所有的sql导出配置文件，不经过任何的处理，仅仅将文件和文件内容（root Element）存放在map中
     *
     * @return Map <fileName,List<RootElement> >
     */
    private Map<String, List<Element>> readFileContent(String gdbVersion) throws ExportException{
        Map<String, List<Element>> fileMap = new HashMap<String, List<Element>>();
        try {
            PathMatchingResourcePatternResolver resolover = new PathMatchingResourcePatternResolver();
            
            Resource[] resources = resolover.getResources("classpath*:/com/navinfo/dataservice/expcore/resources/" + gdbVersion + "/features/**/*.xml");
            for (Resource resource : resources) {
                List<Element> rootChildren = parseXml(resource.getURL());
                fileMap.put(resource.getFilename(), rootChildren);
            }
            //暂时不收集临时表统计信息
//            Resource statRes = resolover.getResource("classpath*:/com/navinfo/dataservice/expcore/resources/" + gdbVersion + "/features/temp-table-stats.xml");
//            fileMap.put(statRes.getFilename(),parseXml(statRes.getURL()));
//            

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ExportException("加载导出脚本时出错",e);
        }
        return fileMap;
    }

    /**
     * 返回root的下级节点
     *
     * @param url
     * @return FeatureElementList
     * @throws DocumentException
     */
    public List<Element> parseXml(URL url) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(url);
        Element root = document.getRootElement();
        return root.elements();
    }

    /**
     * 递归替换import file="" 的内容
     *
     * @param fileMap
     * @param fileContent
     * @return
     */
    private void assembleImportFile(Map<String, List<Element>> fileMap, FileContent fileContent) throws Exception {

        List<Element> rootElements = fileContent.getRootElements();
        for (Element element : rootElements) {
            String elementName = element.getName();
            if (elementName.equals("import")) {
                List<Element> excludeElements = element.elements("exclude");
                if (excludeElements != null) {
                    fileContent.addExcludeElements(excludeElements);
                }
                String refFileName = element.attributeValue("file");
                refFileName = refFileName.trim();
                //log.debug("get file content:" + refFileName);
                List<Element> fileRootElements = fileMap.get(refFileName);

                if (fileRootElements == null) {
                    throw new Exception("Can't find file:" + refFileName);
                }
                fileContent.setRootElements(fileRootElements);
                assembleImportFile(fileMap, fileContent);
            } else {
                fileContent.addAssembledElement(element);
                //log.debug(line);

            }
        }

    }

    /**
     * 解析出sql语句中用到哪些临时表
     *
     * @param sql
     */
    private void getTempTable(String sql) {
        String regEx = "(TEMP_[^\\(\\s\\)]*) ";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            String tempTable = matcher.group();
            if (tempTable != null && tempTable.trim().length() > 0)
                allTempTable.add(tempTable.trim() + "_01");
        }
        System.out.println(sql + ";");
    }

    /**
     * 生成临时表相关的建库脚本
     *
     * @return
     */
    private String getCreateTempTableScripts()  throws ExportException{
        parseFileSqlMapByVersion("1.6.2");
        for (String table : allTempTable) {
            System.out.println("DROP TABLE " + table + " CASCADE CONSTRAINTS;");
        }
        for (String table : allTempTable) {
            //System.out.println("TRUNCATE TABLE " + table + ";");
        }
        for (String table : allTempTable) {
            table = table.substring(0, table.lastIndexOf("_"));
            System.out.println("<sql><![CDATA[TRUNCATE TABLE " + table + ";]]></sql>");
        }
        for (String table : allTempTable) {
            if (!table.equals("TEMP_RD_LINK") && !table.equals("TEMP_RW_LINK") && !table.equals("TEMP_RW_NODE")
                    && !table.equals("TEMP_RD_NODE")) {
                //System.out.println("DROP INDEX IDX_" + table + ";");
            }
        }
        for (String table : allTempTable) {
            if (!table.equals("TEMP_RD_LINK") && !table.equals("TEMP_RW_LINK") && !table.equals("TEMP_RW_NODE")
                    && !table.equals("TEMP_RD_NODE")) {
                //System.out.println("<sql><![CDATA[DROP INDEX IDX_" + table + ";]]></sql>");
            }
        }
        for (String table : allTempTable) {
            if (table.indexOf("TEMP_TMC_POINT") > -1||table.indexOf("TEMP_FILTER_TMC_POINT")>-1)
                System.out.println("CREATE TABLE " + table + " (LOC_CODE  NUMBER(5),LOCTABLE_ID VARCHAR2(2)) NOLOGGING;");
            else
                System.out.println("CREATE TABLE " + table + " (PID  NUMBER(10)) NOLOGGING;");
        }
        for (String table : allTempTable) {
            if (!table.equals("TEMP_RD_LINK") && !table.equals("TEMP_RW_LINK") && !table.equals("TEMP_RW_NODE")
                    && !table.equals("TEMP_RD_NODE")) {
                //System.out.println("CREATE INDEX IDX_" + table + " ON " + table + " (PID)  NOLOGGING;");
            }
        }
        return "";
    }

    public static void main(String args[]) {
        try {

            /* Map<String, Map<Integer, List<ExpSQL>>> fileSqlMap = ExpXMLSQLManager.getInstance().getFileSqlMap();
            //<step ,sqlprocess>
            Set<Map.Entry<Integer, List<ExpSQL>>> sqlEntrySet = fileSqlMap.get("exp-rd-by-mesh-main.xml").entrySet();
            for (Iterator iterator = sqlEntrySet.iterator(); iterator.hasNext();) {
                Map.Entry sqlEntry = (Map.Entry) iterator.next();
                Integer step = (Integer) sqlEntry.getKey();
                List<ExpSQL> sqlList = (List<ExpSQL>) sqlEntry.getValue();
                for (ExpSQL expSQL : sqlList) {
                    log.debug(expSQL.getSql());
                }

            }*/

            ExpXMLSQLManager.getInstance().getCreateTempTableScripts();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    public Map<String, List<String>> sortSqlByTable() throws Exception {
        Map<String, List<String>> resultMap = new TreeMap<String, List<String>>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, Map<Integer, List<ExpSQL>>> map = ExpXMLSQLManager.getInstance().getFileSqlMap("1.6");
        Map<Integer, List<ExpSQL>> sqlMap = map.get("exp-all-by-mesh-main.xml");
        Collection<List<ExpSQL>> sqls = sqlMap.values();
        Class.forName("oracle.jdbc.driver.OracleDriver");
        conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.3.227:1521:orcl", "gdb15", "gdb15");
        String ALL_TABLES = "select TABLE_NAME from user_tables";
        ps = conn.prepareStatement(ALL_TABLES);
        rs = ps.executeQuery();
        List<String> sqlList = new ArrayList<String>();
        for (Iterator<List<ExpSQL>> iterator = sqls.iterator(); iterator.hasNext();) {
            List<ExpSQL> expSQLs = iterator.next();
            Map para = new HashMap();
            para.put("meshCondition", "=595673");
            for (int i = 0; i < expSQLs.size(); i++) {
                ExpSQL expSQL = expSQLs.get(i);
                String sql = expSQL.getSql();
                sql = StringUtils.expandVariables(sql, para, "[", "]");
                sqlList.add(sql);

            }
        }

        while (rs.next()) {
            List<String> result = new ArrayList<String>();
            String tableName = rs.getString(1);

            for (String sql : sqlList) {
                if (sql.toUpperCase().indexOf(tableName) > 0) {
                    result.add(sql);
                }
            }
            resultMap.put(tableName, result);
        }

        rs.close();
        ps.close();
        conn.close();
        Set<Entry<String, List<String>>> set = resultMap.entrySet();
        Iterator<Entry<String, List<String>>> ite = set.iterator();

        while (ite.hasNext()) {
            Entry<String, List<String>> obj = ite.next();
            String tableName = obj.getKey();
            List<String> sqlviewList = obj.getValue();
            if (sqlviewList.size() > 0 && !tableName.startsWith("TEMP")) {
                System.out.println("--//////////////////////" + tableName + "//////////////////////");

                for (int i = 0; i < sqlviewList.size(); i++) {
                    String s = sqlviewList.get(i);
                    System.out.println(s + ";");

                }
            }


        }

        return resultMap;
    }


}
