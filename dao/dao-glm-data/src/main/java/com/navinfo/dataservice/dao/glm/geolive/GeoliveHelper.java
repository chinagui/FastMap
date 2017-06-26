package com.navinfo.dataservice.dao.glm.geolive;

import com.navinfo.dataservice.commons.util.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ly on 2017/6/1.
 */
public class GeoliveHelper {

    protected Logger log = Logger.getLogger(this.getClass());

    JSONObject jsonStorage = new JSONObject();

    //主键
    Map<String, String> pKMaping = new HashMap<>();

    //table中文表
    Map<String, String> tableNameMaping = new HashMap<>();

    //子表
    Map<String, List<String>> childrenMaping = new HashMap<>();

    //外键
    Map<String, Map<String, String>> fKMaping = new HashMap<>();

    //主要素表名列表
    List<String> parentTables = new ArrayList<>();

    private GeoliveHelper() {

        loadGeolive();
    }

    private static volatile GeoliveHelper instance;

    public static GeoliveHelper getIstance() {

        if (instance == null) {

            synchronized (GeoliveHelper.class) {

                if (instance == null) {

                    instance = new GeoliveHelper();
                }
            }
        }

        return instance;
    }

    private void loadGeolive() {

        String configFile = "geoLive.xml";

        InputStream is = null;

        log.debug("parse file " + configFile);

        try {

            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile);

            if (is == null) {

                is = GeoliveHelper.class.getResourceAsStream(configFile);
            }

            SAXReader reader = new SAXReader();

            Document document = reader.read(is);

            SetjsonStorage(document);

        } catch (Exception e) {

            log.warn("初始化Geolive模型配置发生错误，请检查geoLive.xml");

            log.error(e.getMessage(), e);

        } finally {

            try {

                if (is != null) {

                    is.close();
                }
            } catch (IOException e) {

                log.error(e.getMessage(), e);
            }

            log.debug("parse file " + configFile + " end");
        }
    }

    private void SetjsonStorage(Document document) throws Exception {

        loadPrimaryKeys(document);

        loadForeignKeys(document);

        loadTableNameCHI(document);

        josnAddTableInfo(document);

        JosnAddParentLabel();

        josnAddSubLabel();
    }

    private void loadPrimaryKeys(Document document) throws Exception {

        @SuppressWarnings("unchecked")
        List<Element> elements = document.getRootElement().element("PrimaryKeys").elements();

        for (int i = 0; i < elements.size(); i++) {

            Element element = elements.get(i);

            parentTables.add(element.getName());

            pKMaping.put(element.getName(), element.attributeValue("PK"));
        }
    }

    private void loadForeignKeys(Document document) throws Exception {

        @SuppressWarnings("unchecked")
        List<Element> elements = document.getRootElement().element("ForeignKeys").elements();

        for (int i = 0; i < elements.size(); i++) {

            Element element = elements.get(i);

            String table = element.attributeValue("Table");

            String fK = element.attributeValue("FK");

            String ref = element.attributeValue("Ref");

            if (!fKMaping.containsKey(table)) {

                Map<String, String> tmp = new HashMap<>();

                fKMaping.put(table, tmp);
            }

            if (!childrenMaping.containsKey(ref)) {

                List<String> tmp = new ArrayList<>();

                childrenMaping.put(ref, tmp);
            }

            childrenMaping.get(ref).add(table);

            fKMaping.get(table).put(ref, fK);

            //去除有主键的子表
            if (parentTables.contains(table)) {

                parentTables.remove(table);
            }
        }

    }

    private void loadTableNameCHI(Document document) throws Exception {

        @SuppressWarnings("unchecked")
        List<Element> elements = document.getRootElement().element("TableNameCHI").elements();

        for (int i = 0; i < elements.size(); i++) {

            Element element = elements.get(i);

            tableNameMaping.put(element.getName(), element.attributeValue("Name"));
        }
    }

    private void JosnAddParentLabel() {

        JSONArray parentLabels = new JSONArray();

        for (String tableName : parentTables) {

            JSONObject label = new JSONObject();

            label.put("tableName", tableName);

            label.put("nameCHI", getTableNameCHI(tableName));

            label.put("hasChildren", 1);

            parentLabels.add(label);
        }

        jsonStorage.put("PARENT_LABEL", parentLabels);
    }

    private void josnAddSubLabel() {

        for (String tableName : parentTables) {

            JSONArray subLabels = new JSONArray();

            JSONObject labelTmp = new JSONObject();

            labelTmp.put("tableName", tableName);

            labelTmp.put("nameCHI", getTableNameCHI(tableName));

            labelTmp.put("hasLabel", 0);

            subLabels.add(labelTmp);

            if (childrenMaping.containsKey(tableName)) {

                List<String> subTables = childrenMaping.get(tableName);

                for (String subTable : subTables) {

                    labelTmp = new JSONObject();

                    labelTmp.put("tableName", subTable);

                    labelTmp.put("nameCHI", getTableNameCHI(subTable));

                    if (childrenMaping.containsKey(subTable)) {

                        labelTmp.put("hasChildren", 1);

                    } else {
                        labelTmp.put("hasChildren", 0);
                    }

                    subLabels.add(labelTmp);
                }
            }

            jsonStorage.put(tableName + "_LABEL", subLabels);
        }
    }

    private void josnAddTableInfo(Document document) {
        @SuppressWarnings("unchecked")
        List<Element> tableElements = document.getRootElement().element("Tables").elements();

        for (int i = 0; i < tableElements.size(); i++) {

            Element tableElement = tableElements.get(i);

            JSONObject tableInfo = new JSONObject();

            tableInfo.put("tableName", tableElement.getName());

            List<Element> fieldElements = tableElement.elements();

            JSONArray fieldInfos = new JSONArray();

            for (int j = 0; j < fieldElements.size(); j++) {

                Element fieldElement = fieldElements.get(j);

                JSONObject fieldInfo = new JSONObject();

                fieldInfo.put("fieldName", fieldElement.getName());

                fieldInfo.put("alterName", fieldElement.attributeValue("AlterName"));

                fieldInfo.put("fieldType", fieldElement.attributeValue("FieldType"));

                fieldInfos.add(fieldInfo);
            }

            tableInfo.put("fieldInfos", fieldInfos);

            jsonStorage.put(tableElement.getName() + "_INFO", tableInfo);
        }
    }

    private String getTableNameCHI(String tableName) {

        if (tableNameMaping.containsKey(tableName)) {

            return tableNameMaping.get(tableName);
        }

        return tableName;
    }

    public JSONArray getParentLabel() throws Exception {

        if (jsonStorage.containsKey("PARENT_LABEL")) {

            return jsonStorage.getJSONArray("PARENT_LABEL");
        }

        throw new Exception("未找到主表信息");
    }

    public JSONArray getSubLabel(String tableName) throws Exception {

        String labelType = tableName + "_LABEL";

        if (jsonStorage.containsKey(labelType)) {

            return jsonStorage.getJSONArray(labelType);
        }

        throw new Exception(tableName + "：未找到表信息");
    }

    public JSONObject getTableInfo(String tableName) throws Exception {

        String tableFalg = tableName + "_INFO";

        if (jsonStorage.containsKey(tableFalg)) {

            return jsonStorage.getJSONObject(tableFalg);
        }

        throw new Exception(tableName + "：未找到表属性信息");
    }

    public String getForeignKey(String tableName, String parentTableName) throws Exception {

        String foreignKey = null;

        if (fKMaping.containsKey(tableName)) {

            if (fKMaping.get(tableName).containsKey(parentTableName)) {

                foreignKey = fKMaping.get(tableName).get(parentTableName);
            }
        }

        if (StringUtils.isNotEmpty(foreignKey)) {
            return foreignKey;
        }
        throw new Exception(tableName + "：未找到外键");
    }

    public String getPrimaryKey(String tableName) throws Exception {

        if (pKMaping.containsKey(tableName)) {

            return pKMaping.get(tableName);
        }

        throw new Exception(tableName + "：未找到主键");
    }
}
