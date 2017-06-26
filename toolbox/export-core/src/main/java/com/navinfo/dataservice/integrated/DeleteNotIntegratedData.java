package com.navinfo.dataservice.integrated;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.QueryRunner;

/**
 * 
 * @author LiuQing
 * 
 */
public class DeleteNotIntegratedData {
	protected Logger log = Logger.getLogger(this.getClass());

	public void execute(int dbId) throws Exception {
		DataSource ds = null;
		try {
			// 创建临时表
			DbInfo db = DbService.getInstance().getDbById(dbId);
			OracleSchema schema = new OracleSchema(
					DbConnectConfig.createConnectConfig(db.getConnectParam()));
			ds = schema.getPoolDataSource();
			createOrTruncateTempTable(ds);
			Map<Integer, List<TableConfig>> configMap = parseConfigFile(ds);
			Set<Integer> stepSet = configMap.keySet();
			createTempTableIndex(ds);
			ExecuteDeleteNotIntegratedSql executeSql = new ExecuteDeleteNotIntegratedSql();
			log.debug("查找和删除不完整数据");
			for (Integer stepValue : stepSet) {
				log.debug("step " + stepValue);
				List<TableConfig> tableConfigs = configMap.get(stepValue);
				executeSql.execute(ds, tableConfigs);
			}

		} catch (Exception e) {
			log.error("", e);
			throw e;
		} finally {
			if (ds != null) {
				try {
					log.debug("关闭连接池");
					ds = null;
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}

		}
	}

	/**
	 * 
	 * @param version
	 * @throws SQLException
	 */
	private void createOrTruncateTempTable(DataSource ds) throws SQLException {
		boolean exists = DataBaseUtils.isTableExists(ds,
				"TEMP_NOT_INTEGRATED_DATA");

		QueryRunner runner = new QueryRunner();
		if (!exists) {
			String sql = "CREATE TABLE TEMP_NOT_INTEGRATED_DATA(\r\n"
					+ "     TABLE_NAME VARCHAR2(32),\r\n"
					+ "     PID NUMBER(10),\r\n"
					+ "     OBJECT_TYPE   VARCHAR2(32),\r\n"
					+ "     COLUMN_NAME VARCHAR2(32),\r\n"
					+ "     REF_TABLE VARCHAR2(32),\r\n"
					+ "     REF_COLUMN VARCHAR2(32)\r\n" + ") NOLOGGING";
			log.debug(sql);
			runner.execute(ds, sql);
		} else {
			dropTempTableIndex(ds);
			String sql = "truncate table TEMP_NOT_INTEGRATED_DATA";
			log.debug(sql);
			runner.execute(ds, sql);
		}
	}

	private void createTempTableIndex(DataSource ds) throws SQLException {
		String createIndex = "CREATE INDEX TEMP_NID_OP ON TEMP_NOT_INTEGRATED_DATA(OBJECT_TYPE,PID)";
		QueryRunner runner = new QueryRunner();
		log.debug(createIndex);
		runner.execute(ds, createIndex);
	}

	private void dropTempTableIndex(DataSource ds) throws SQLException {
		String dropIndex = "DROP  INDEX TEMP_NID_OP";
		QueryRunner runner = new QueryRunner();
		log.debug(dropIndex);
		runner.execute(ds, dropIndex);
	}

	/**
	 * 解析xml配置文件
	 * 
	 * @param version
	 * @return
	 * @throws Exception
	 * @throws IOException
	 */
	public Map<Integer, List<TableConfig>> parseConfigFile(DataSource ds)
			throws Exception, IOException {
		Document document = readConfigFile();
		Element root = document.getRootElement();// tables
		Map<Integer, List<TableConfig>> configMap = new TreeMap<Integer, List<TableConfig>>();

		List<Element> ojbectNameElements = root.elements("objectName");
		for (Element ojbectNameElement : ojbectNameElements) {
			List<Element> stepElements = ojbectNameElement.elements("step");
			for (Element stepElement : stepElements) {
				String value = stepElement.attributeValue("value");
				Integer step = Integer.parseInt(value);
				List<TableConfig> tableConfigs = configMap.get(step);
				if (tableConfigs == null) {
					tableConfigs = new ArrayList<TableConfig>();
					configMap.put(step, tableConfigs);
				}
				List<Element> tableElements = stepElement.elements("table");
				for (Element tableElement : tableElements) {
					String name = tableElement.attributeValue("name");
					String pid = tableElement.attributeValue("pid");
					String type = tableElement.attributeValue("OBJECT_TYPE");
					TableConfig tableConfig = new TableConfig(name, pid, type);
					tableConfigs.add(tableConfig);

					List<Element> refElements = tableElement.elements("ref");
					List<RefConfig> refConfigs = new ArrayList<RefConfig>();
					for (Element refElement : refElements) {
						String column = refElement.attributeValue("column");
						String refTable = refElement.attributeValue("refTable");
						String refTablePid = refElement
								.attributeValue("refTablePid");
						RefConfig refConfig = new RefConfig(column, refTable,
								refTablePid);
						refConfig.setTableConfig(tableConfig);
						// log.debug("add ref:"+refConfig.toSql());
						refConfigs.add(refConfig);

					}
					tableConfig.setRefConfigs(refConfigs);
					List<Element> removeElements = tableElement
							.elements("remove");
					List<RemoveConfig> removeConfigs = new ArrayList<RemoveConfig>();
					for (Element removeElement : removeElements) {
						String tableName = removeElement
								.attributeValue("tableName");
						String refPid = removeElement.attributeValue("refPid");

						RemoveConfig removeConfig = new RemoveConfig(tableName,
								refPid);
						removeConfig.setTableConfig(tableConfig);
						removeConfigs.add(removeConfig);
					}
					tableConfig.setRemoveConfigs(removeConfigs);
					List<Element> sqlElements = tableElement.elements("sql");
					List<SqlConfig> sqlList = new ArrayList<SqlConfig>();
					for (Element sqlElement : sqlElements) {
						String sql = sqlElement.getTextTrim();
						SqlConfig sqlConfig = new SqlConfig(sql, tableConfig);
						sqlList.add(sqlConfig);
					}
					tableConfig.setSqlList(sqlList);

				}
			}//

		}

		return configMap;
	}

	/**
	 * 读取配置文件
	 * 
	 * @param version
	 * @return
	 * @throws Exception
	 * @throws IOException
	 */
	public Document readConfigFile() throws Exception, IOException {
		String gdbVesion = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.gdbVersion);
		String poiSchemaCreateFile = "/com/navinfo/dataservice/expcore/resources/"
				+ gdbVesion + "/publish/not-integrated-data.xml";
		InputStream is = null;
		try {
			is = DeleteNotIntegratedData.class
					.getResourceAsStream(poiSchemaCreateFile);
			if (is == null) {
				is = Thread.currentThread().getContextClassLoader()
						.getResourceAsStream(poiSchemaCreateFile);
			}
			if (is == null)
				throw new IOException("无法找到配置文件:" + poiSchemaCreateFile);
			SAXReader reader = new SAXReader();
			Document document = reader.read(is);
			return document;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			if (is != null) {

				is.close();
			}
		}
	}

}
