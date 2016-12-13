package com.navinfo.dataservice.dao.glm.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 初始化要素和它的名称表关系
* @ClassName: TableNameFactory 
* @author Zhang Xiaolong
* @date 2016年12月12日 下午3:02:05 
 */
public class TableNameFactory {
	protected Logger log = Logger.getLogger(this.getClass());

	private TableNameFactory() {
		loadNameSqlInfo();
	}

	private volatile static TableNameFactory instance;

	public static TableNameFactory getInstance() {
		if (instance == null) {
			synchronized (TableNameFactory.class) {
				if (instance == null) {
					instance = new TableNameFactory();
				}
			}
		}
		return instance;
	}

	private Map<String, TableNameSqlInfo> tables;
	
	public Map<String, TableNameSqlInfo> getTables() {
		if (tables == null) {
			synchronized (this) {
				if (tables == null) {
					loadNameSqlInfo();
				}
			}
		}
		return tables;
	}

	public TableNameSqlInfo getSqlInfoByTableName(String tableName) throws Exception {
		TableNameSqlInfo table = getTables().get(tableName);
		return table;
	}

	private void loadNameSqlInfo() {
		String configFile = "search_name_map.xml";
		InputStream is = null;
		log.debug("parse file " + configFile);
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile);
			if (is == null) {
				is = TableNameFactory.class.getResourceAsStream(configFile);
			}

			tables = new HashMap<String, TableNameSqlInfo>();

			SAXReader reader = new SAXReader();
			Document document = reader.read(is);
			Element root = document.getRootElement();
			@SuppressWarnings("unchecked")
			List<Element> eleobjs = root.elements("obj-info");
			for (int i = 0; i < eleobjs.size(); i++) {
				Element obj = eleobjs.get(i);// obj
				
				String tableName = obj.attributeValue("type");
				
				String selectColumn = obj.elementText("selectColumn");
				
				String leftJoinSql = obj.elementText("leftJoinSql");
				
				String outSelectCol = obj.elementText("outSelectCol");
				
				String outLeftJoinSql = obj.elementText("outLeftJoinSql");
				
				TableNameSqlInfo sqlInfo = new TableNameSqlInfo();
				
				sqlInfo.setTableName(tableName);
				
				sqlInfo.setSelectColumn(selectColumn);
				
				sqlInfo.setLeftJoinSql(leftJoinSql);
				
				sqlInfo.setOutLeftJoinSql(outLeftJoinSql);
				
				sqlInfo.setOutSelectCol(outSelectCol);
				
				tables.put(tableName, sqlInfo);
			} 
		} catch (Exception e) {
			log.warn("初始化要素名称表配置发生错误，可能是search_name_map.xml配置错误,请检查");
			log.error(e.getMessage(), e);
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			log.debug("parse file " + configFile +" end");
		}
	}
}
