package com.navinfo.dataservice.engine.editplus.glm;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.navinfo.navicommons.database.QueryRunner;

/**
 * 表名和字段名全部为大写
 * @ClassName: GlmFactory
 * @author xiaoxiaowen4127
 * @date 2016年11月13日
 * @Description: GlmFactory.java
 */
public class GlmFactory {
	protected Logger log = Logger.getLogger(this.getClass());
	private GlmFactory(){loadGlm();}
	private volatile static GlmFactory instance;
	public static GlmFactory getInstance(){
		if(instance==null){
			synchronized(GlmFactory.class){
				if(instance==null){
					instance=new GlmFactory();
				}
			}
		}
		return instance;
	}
	private Map<String,GlmTable> tables;//key:table_name,value:glmtable
	private Map<String,GlmObject> objs;//key:objType,value:glmobject
	private Set<String> ignoreColumns;//glm不处理的字段

	public Map<String,GlmTable> getTables(){
		return tables;
	}
	public Map<String,GlmObject> getObjs(){
		return objs;
	}
	public Set<String> getIgnoreColumns() {
		return ignoreColumns;
	}
	public GlmTable getTableByName(String tableName)throws GlmTableNotFoundException{
		GlmTable table = null;
		if(tables!=null){
			table=tables.get(tableName);
		}
		
		if(table==null)throw new GlmTableNotFoundException("Glm未初始化该表");
		
		return table;
	}
	public GlmObject getObjByType(String objType)throws GlmTableNotFoundException{
		GlmObject obj=null;
		if(objs!=null){
			obj=objs.get(objType);
		}
		if(obj==null)throw new GlmTableNotFoundException("Glm未初始化该表");
		return obj;
	}
	/**
	 * @param tableName:glm表名大写
	 * @return：模型的className
	 * @throws GlmTableNotFoundException
	 */
	public String getClassByTableName(String tableName)throws GlmTableNotFoundException{
		GlmTable table = getTableByName(tableName);
		return table.getModelClassName();
	}
	
	private void loadGlm(){
		String configFile = "/com/navinfo/dataservice/commons/config/SystemConfig.xml";
		InputStream is = null;
        log.debug("parse file " + configFile);
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile);
            if (is == null) {
                is = GlmFactory.class.getResourceAsStream(configFile);
            }
            
            objs = new HashMap<String,GlmObject>();
            tables = new HashMap<String,GlmTable>();
            
            SAXReader reader = new SAXReader();
            Document document = reader.read(is);
            Element root = document.getRootElement();
            List<Element> eleobjs = root.elements("object");
            for (int i = 0; i < eleobjs.size(); i++) {
                Element obj = eleobjs.get(i);//obj
                GlmObject glmObj = new GlmObject();
                String curObjName = obj.attributeValue("name");
                glmObj.setName(curObjName);
                glmObj.setType(obj.attributeValue("type"));
                glmObj.setModelClassName(obj.attributeValue("class"));

                Map<String,GlmTable> glmTables = new HashMap<String,GlmTable>();
                String mainTabName = obj.attributeValue("mainTable");
                List<Element> objTables = obj.elements("table");
                for(int j=0;j<objTables.size();j++){
                	Element tab = objTables.get(j);//table
                	String curTabName = tab.attributeValue("name");
                	GlmTable glmTab = new GlmTable();
                	glmTab.setName(curTabName);
                	glmTab.setObjType(glmObj.getType());
                	glmTab.setPkColumn(tab.attributeValue("pk"));
                	glmTab.setModelClassName(tab.attributeValue("class"));
                	if(curTabName.equals(mainTabName)){
                		glmObj.setMainTable(glmTab);
                	}else{
                		Element ref = tab.element("objRef");
                		GlmRef glmRef = new GlmRef();
                		glmRef.setCol(ref.attributeValue("col"));
                		glmRef.setRefTable(ref.attributeValue("refTable"));
                		glmRef.setRefCol(ref.attributeValue("refCol"));
                		glmRef.setRefMain(Boolean.parseBoolean(ref.attributeValue("isRefMain")));
                		glmTab.setObjRef(glmRef);
                		Element gr = tab.element("geoRef");
                		if(gr!=null){
                    		GlmRef glmR = new GlmRef();
                    		glmR.setCol(gr.attributeValue("col"));
                    		glmR.setRefTable(gr.attributeValue("refTable"));
                    		glmR.setRefCol(gr.attributeValue("refCol"));
                    		glmR.setRefMain(Boolean.parseBoolean(gr.attributeValue("isRefMain")));
                    		glmTab.setObjRef(glmR);
                		}
                	}
                	glmTables.put(curTabName, glmTab);
                	//factory中的字段
                	tables.put(curTabName, glmTab);
                }//table end
                glmObj.setTables(glmTables);
                //factory中的字段
                objs.put(curObjName, glmObj);
            }//obj end
        } catch (Exception e) {
			log.warn("初始化GLM发生错误，可能是glm.xml配置错误,请检查");
			log.error(e.getMessage(), e);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            	log.error(e.getMessage(),e);
            }
        }
	}
	private void loadGlmColumns(){
		if(tables!=null){
			Connection conn = null;
			try{
				StringBuilder tableSql = new StringBuilder();
				tableSql.append("SELECT T.TABLE_NAME, C.COLUMN_NAME, C.DATA_TYPE,C.COLUMN_ID FROM USER_TABLES T, USER_TAB_COLUMNS C WHERE T.TABLE_NAME = C.TABLE_NAME");
				tableSql.append(" AND T.TABLE_NAME IN ('");
				tableSql.append(StringUtils.join(tables.keySet(),"','"));
				tableSql.append("') ORDER BY T.TABLE_NAME,C.COLUMN_ID");
				Map<String,Map<String,GlmColumn>> results = new QueryRunner().query(conn, tableSql.toString(), new ResultSetHandler<Map<String,Map<String,GlmColumn>>>(){

					@Override
					public Map<String, Map<String, GlmColumn>> handle(ResultSet rs) throws SQLException {
						Map<String,Map<String,GlmColumn>> res = new HashMap<String,Map<String,GlmColumn>>();
						while(rs.next()){
							String tableName = rs.getString("TABLE_NAME");
							Map<String,GlmColumn> cols = res.get(tableName);
							if(cols==null){
								cols = new HashMap<String,GlmColumn>();
								res.put(tableName, cols);
							}
							GlmColumn col=null;
							String colName = rs.getString("COLUMN_NAME");
							String dataType=rs.getString("DATA_TYPe");
							if(GlmColumn.TYPE_NUMBER.equals(dataType)){
								col = new GlmColumn(colName,dataType);
							}else{
								col=new GlmColumn(colName,dataType,rs.getInt("DATA_PRECISION"),rs.getInt("DATA_SCALE"));
							}
							cols.put(col.getName(),col);
						}
						return res;
					}
					
				});
				//依次将表结构写入对象
				for(Map.Entry<String, GlmTable> entry:tables.entrySet()){
					entry.getValue().setColumns(results.get(entry.getKey()));
				}
			} catch (Exception e) {
				log.warn("初始化GLM发生错误，从母库中获取字段列表发生错误,请检查");
				log.error(e.getMessage(), e);
	        } finally {
	            DbUtils.closeQuietly(conn);
	        }
		}
	}
}
