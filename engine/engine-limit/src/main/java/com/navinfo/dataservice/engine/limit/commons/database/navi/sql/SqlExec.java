package com.navinfo.dataservice.engine.limit.commons.database.navi.sql;

import com.navinfo.dataservice.engine.limit.commons.database.navi.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA. User: Administrator Date: 11-5-6 Time: 下午3:36
 */
public class SqlExec {
	private Logger log = Logger.getLogger(SqlExec.class);

	private Connection conn;
	private InputStream is;

	public SqlExec(Connection conn) {
		this.conn = conn;
	}

	// ////////////////////////////// Source info /////////////////////////////

	/**
	 * Set the delimiter that separates SQL statements.
	 * 
	 * @parameter expression="${delimiter}" default-value=";"
	 * @since 1.0
	 */
	private String delimiter = ";";

	// /////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Encoding to use when reading SQL statements from a file.
	 * 
	 * @parameter expression="${encoding}" default-value=
	 *            "${project.build.sourceEncoding}"
	 * @since 1.1
	 */
	private String encoding = "utf-8";

	/**
	 * Set the file encoding to use on the SQL files read in
	 * 
	 * @param encoding
	 *            the encoding to use on the files
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Set the delimiter that separates SQL statements. Defaults to
	 * &quot;;&quot;;
	 * 
	 * @param delimiter
	 *            the new delimiter
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public void execute(String classpath, String fileEncoding) throws Exception {
		InputStream is = null;
		try {
			is = SqlExec.class.getResourceAsStream(classpath);
			if (is == null) {
				is=Thread.currentThread().getContextClassLoader()
						.getResourceAsStream(classpath);
			}
			if (is == null)
				throw new IOException("无法找到配置文件:" + classpath);
			execute(is, fileEncoding);
		} catch (Exception e) {
			//log.error(e.getMessage(), e);
			throw e;
		}

	}
	public void executeIgnoreError(String classpath, String fileEncoding) throws Exception {
		InputStream is = null;
		try {
			is = SqlExec.class.getResourceAsStream(classpath);
			if (is == null) {
				is=Thread.currentThread().getContextClassLoader()
						.getResourceAsStream(classpath);
			}
			if (is == null)
				throw new IOException("无法找到配置文件:" + classpath);
			executeIgnoreError(is, fileEncoding);
		} catch (Exception e) {
			//log.error(e.getMessage(), e);
			throw e;
		}

	}

	public void execute(String classpath) throws Exception {
		execute(classpath, encoding);
	}	
	public void executeIgnoreError(String classpath) throws Exception {
		executeIgnoreError(classpath, encoding);
	}

	/**
	 * Load the sql file and then execute it
	 */

	public void execute(InputStream is, String fileEncoding) throws Exception {

		long t1 = System.currentTimeMillis();

		Statement statement = null;
		try {
			statement = conn.createStatement();
			if (is != null) {
				Reader reader = new InputStreamReader(is, fileEncoding);
				try {
					runStatements(reader, statement);
				} finally {
					is.close();
					reader.close();
				}
			}
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (conn != null) {
					conn.commit();
				}
			} catch (SQLException ex) {
			}
		}
		long t2 = System.currentTimeMillis();
		if (log.isDebugEnabled()) {
			log.debug("sql exec time " + (t2 - t1) + "ms");
		}
	}
	public void execute(InputStream is) throws Exception {
		execute(is, encoding);
	}
	
	/**
	 * 读取文件，执行文件中的语句（忽略每条语句执行的错误）
	 * @param is
	 * @param fileEncoding
	 * @throws Exception
	 */
	public void executeIgnoreError(InputStream is, String fileEncoding) throws Exception {
		long t1 = System.currentTimeMillis();
		Statement statement = null;
		try {
			statement = conn.createStatement();
			if (is != null) {
				Reader reader = new InputStreamReader(is, fileEncoding);
				try {
					runStatementsIgnoreError(reader, statement);
				} finally {
					is.close();
					reader.close();
				}
			}

		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (conn != null) {
					conn.commit();
				}
			} catch (SQLException ex) {
			}
		}
		long t2 = System.currentTimeMillis();
		if (log.isDebugEnabled()) {
			log.debug("sql exec time " + (t2 - t1) + "ms");
		}

	}

	/**
	 * read in lines and execute them
	 * 
	 * @param reader
	 *            the reader
	 * @param statement
	 * @throws SQLException
	 * @throws IOException
	 */

	private void runStatements(Reader reader, Statement statement)
			throws SQLException, IOException {
		String line;
		StringBuffer sql = new StringBuffer();
		BufferedReader in = new BufferedReader(reader);

		while ((line = in.readLine()) != null) {
			line = line.trim();

			if (line.startsWith("//")) {
				continue;
			}
			if (line.startsWith("--")) {
				continue;
			}
			StringTokenizer st = new StringTokenizer(line);
			if (st.hasMoreTokens()) {
				String token = st.nextToken();
				if ("REM".equalsIgnoreCase(token)) {
					continue;
				}
			}
			sql.append("\n").append(line);

			if (line.endsWith(delimiter)) {
				execSQL(statement,
						sql.substring(0, sql.length() - delimiter.length()));
				sql.setLength(0); // clean buffer
			}
		}

		// Catch any statements not followed by ;
		if (!sql.toString().equals("")) {
			execSQL(statement, sql.toString());
		}
		in.close();
	}
	/**
	 * read in lines and execute them  Ignore each sql Error
	 *
	 * @param reader
	 *            the reader
	 * @param statement
	 * @throws SQLException
	 * @throws IOException
	 */

	private void runStatementsIgnoreError(Reader reader, Statement statement)
			throws SQLException, IOException {
		String line;
		StringBuffer sql = new StringBuffer();
		BufferedReader in = new BufferedReader(reader);
		while ((line = in.readLine()) != null) {
			line = line.trim();
			if (line.startsWith("//")) {
				continue;
			}
			if (line.startsWith("--")) {
				continue;
			}
			StringTokenizer st = new StringTokenizer(line);
			if (st.hasMoreTokens()) {
				String token = st.nextToken();
				if ("REM".equalsIgnoreCase(token)) {
					continue;
				}
			}
			sql.append("\n").append(line);
			if (line.endsWith(delimiter)) {
				execSQLIgnoreError(statement,
						sql.substring(0, sql.length() - delimiter.length()));
				sql.setLength(0); // clean buffer
			}
		}
		if (!sql.toString().equals("")) {
			execSQLIgnoreError(statement, sql.toString());
		}
		in.close();
	}

	/**
	 * Exec the sql statement.
	 * 
	 * @param statement
	 * @param sql
	 *            query to execute
	 */
	private void execSQL(Statement statement, String sql) throws SQLException {
		// Check and ignore empty statements
		if ("".equals(sql.trim())) {
			return;
		}
		// log.debug(sql);
		try {
			statement.execute(sql);
		} catch (SQLException e) {
			log.error("Failed to execute: " + sql+","+e.getMessage());
			//log.error(e.getMessage(), e);
			throw e;
		}
	}
	private void execSQLIgnoreError(Statement statement, String sql) throws SQLException {
		// Check and ignore empty statements
		if ("".equals(sql.trim())) {
			return;
		}
		// log.debug(sql);
		try {
			statement.execute(sql);
		} catch (SQLException e) {
			log.error("Failed to execute: " + sql+","+e.getMessage());
			//log.error(e.getMessage(), e);
			//throw e;
		}
	}
	public void execSQLIgnoreError(String classpath, String fileEncoding) throws Exception {
		InputStream is = null;
		try {
			is = SqlExec.class.getResourceAsStream(classpath);
			if (is == null) {
				is=Thread.currentThread().getContextClassLoader()
						.getResourceAsStream(classpath);
			}
			if (is == null)
				throw new IOException("无法找到配置文件:" + classpath);
			executeIgnoreError(is, fileEncoding);
		} catch (Exception e) {
			//log.error(e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * 根据对象名称,及对象类型,判定某个对象时候在数据库中存在
	 * 
	 * @param objName
	 * @param objType
	 * @return
	 * @throws SQLException
	 */
	public boolean isExistForType(String objName, String objType)
			throws SQLException {
		String sql = "SELECT COUNT(*) FROM USER_OBJECTS WHERE OBJECT_TYPE =  UPPER(?) AND OBJECT_NAME = UPPER(?)";
		QueryRunner runner = new QueryRunner();
		try {
			return runner.query(conn, sql, new ResultSetHandler<Boolean>() {
				boolean exists = false;

				public Boolean handle(ResultSet rs) throws SQLException {
					if (rs.next()) {
						int count = rs.getInt(1);
						if (count > 0) {
							exists = true;
						}
					}
					return exists;
				}
			}, objType, objName);

		} catch (SQLException e) {
			log.error(
					"验证某对象时候存在的时候出错,类型为" + objType + ",对象名为" + objName
							+ e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * 根据对象名称很对象类型,删除对象
	 * 
	 * @param objName
	 * @param objType
	 * @return
	 * @throws SQLException
	 */
	public void dropObjForType(String objName, String objType) throws Exception {
		String sql = "drop " + objType + " " + objName;
//		log.debug(sql);
		try {
			QueryRunner runner = new QueryRunner();
			runner.execute(conn, sql);
		} catch (SQLException e) {
			log.error(
					"删除某对象时候存在的时候出错,类型为" + objType + ",对象名为" + objName
							+ e.getMessage(), e);
			throw e;
		}
	}
}
