package com.navinfo.dataservice.integrated;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;

/**
 * 
 * @author LiuQing
 * 
 */
public class DeleteNotIntegratedData {
	protected Logger log = Logger.getLogger(this.getClass());
	private Set<String> meshes = new HashSet<String>();

	public Set<String> getMeshes() {
		return meshes;
	}

	public void setMeshes(Set<String> meshes) {
		this.meshes = meshes;
	}

	public void execute(int dbId) throws Exception {
		DataSource ds = null;
		try {
			// 创建临时表
			DbInfo db = DbService.getInstance().getDbById(dbId);
			OracleSchema schema = new OracleSchema(
					DbConnectConfig.createConnectConfig(db.getConnectParam()));
			ds = schema.getPoolDataSource();
			log.debug("创建临时表");
			createOrTruncateTempTable(ds);
			log.debug("创建RDLINK接边临时表");
			this.createTempRdLinkTable(ds);
			log.debug("接边数据提取");
			this.insertTempRdLink(ds);
			log.debug("接边数据删除");
			this.deleteRdLinkData(ds);
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
			log.debug("补充接边数据");
			this.replenishRdLinkData(ds);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
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
	 * 备份不是核心图幅数据
	 * 
	 * @param ds
	 * @throws SQLException
	 */
	private void insertTempRdLink(DataSource ds) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = ds.getConnection();

			String ids = org.apache.commons.lang.StringUtils.join(
					this.getMeshes(), ",");
			log.debug("meshes ===" +ids);

			String sql = "";
			String sqlInsert = "INSERT /*+append*/  INTO  TEMP_RDLINK_NOMESH_DATA  SELECT *FROM RD_LINK WHERE MESH_ID ";
			Clob pidClod = null;
			pidClod = ConnectionUtil.createClob(conn);
			pidClod.setString(1, ids);
			sql = sqlInsert
					+ " NOT IN (select to_number(column_value) from table(clob_to_table(?)))";
			pstmt = conn.prepareStatement(sql);
			pstmt.setClob(1, pidClod);
			pstmt.executeUpdate();
		} catch (Exception e) {
			log.error(e.getMessage() + "导出接边LINK报错", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		} finally {
			if (conn != null) {
				DBUtils.closeStatement(pstmt);
				DbUtils.commitAndCloseQuietly(conn);
			}

		}

	}

	/***
	 * 清理数据
	 * 
	 * @param ds
	 * @throws SQLException
	 */
	private void deleteRdLinkData(DataSource ds) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {

	
			conn = ds.getConnection();
			String ids = org.apache.commons.lang.StringUtils.join(
					this.getMeshes(), ",");

			String sql = "";
			log.debug("meshes ===" +ids);
			String sqlInsert = "DELETE FROM  RD_LINK WHERE MESH_ID ";
			Clob pidClod = ConnectionUtil.createClob(conn);
			pidClod.setString(1, ids);
			sql = sqlInsert
					+ " NOT IN (select to_number(column_value) from table(clob_to_table(?)))";

			pstmt = conn.prepareStatement(sql);
			pstmt.setClob(1, pidClod);
			pstmt.executeUpdate();

		} catch (Exception e) {
			log.error(e.getMessage() + "删除接边LINK报错", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		} finally {
			if (conn != null) {
				DBUtils.closeStatement(pstmt);
				DbUtils.commitAndCloseQuietly(conn);
			}

		}
	}

	/***
	 * 补充数据
	 * 
	 * @param ds
	 * @throws SQLException
	 */
	private void replenishRdLinkData(DataSource ds) throws SQLException {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			QueryRunner runner = new QueryRunner();
			String sql = "INSERT /*+append*/  INTO   RD_LINK  SELECT * FROM TEMP_RDLINK_NOMESH_DATA ";
			log.debug(sql);
			runner.execute(conn, sql);
		} catch (Exception e) {
			log.error(e.getMessage() + "补充接边LINK报错", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		} finally {
			if (conn != null) {
				DbUtils.commitAndCloseQuietly(conn);
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

	/**
	 * 存放不是核心图幅外的link数据
	 * 
	 * @param version
	 * @throws SQLException
	 */
	private void createTempRdLinkTable(DataSource ds) throws SQLException {
		boolean exists = DataBaseUtils.isTableExists(ds,
				"TEMP_RDLINK_NOMESH_DATA");

		QueryRunner runner = new QueryRunner();
		if (!exists) {
			String sql = "CREATE TABLE TEMP_RDLINK_NOMESH_DATA AS SELECT *FROM RD_LINK WHERE 1 <> 1";
			log.debug(sql);
			runner.execute(ds, sql);
		} else {
			String sql = "truncate table TEMP_RDLINK_NOMESH_DATA";
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
