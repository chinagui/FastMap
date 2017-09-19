package com.navinfo.dataservice.engine.limit.commons.database.oracle;

import com.navinfo.dataservice.engine.limit.commons.thread.ThreadLocalContext;
import com.navinfo.dataservice.engine.limit.commons.database.navi.QueryRunner;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import javax.activation.UnsupportedDataTypeException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注册Connection，给每个connection赋任务号
 * 此类中存储了每个任务执行过的Connection
 * 
 * @author LiuQing
 * 
 */
public class ConnectionRegister {
	private static Logger log = Logger.getLogger(ConnectionRegister.class);

	private static ThreadLocal<String> taskThreadLocal = new ThreadLocal<String>();

	private static boolean enableRegister = false;

	/**
	 * 
	 * 
	 * @param vmTaskId
	 *            任务号
	 */
	public static void enableRegister(String vmTaskId) {
		log.info("打开连接注册功能");
		enableRegister = true;
		addTask(vmTaskId);
	}

	public static void addTask(String vmTaskId) {
		taskThreadLocal.set(vmTaskId);
	}

	/**
	 * 取得当前线程变量中已经初始化的任务号
	 * 
	 * @return
	 */
	public static String getVmTaskId() {
		String vmTaskId = taskThreadLocal.get();
		if (vmTaskId == null) {

		}
		return vmTaskId;
	}

	public static void removeTask() {
		taskThreadLocal.remove();

	}

	// private static MultiValueMap connMap = new MultiValueMap();
	private static Map<String, SessionProperties> connMap = new HashMap<String, SessionProperties>();

	private static Map threadConnMap = MultiValueMap.decorate(new ConcurrentHashMap());

	public static Connection registerConnection(Connection connection) throws SQLException {
		storeConnection(connection, getVmTaskId());
		return connection;
	}

	public static Connection registerConnection(Connection connection, String vmTaskId) throws SQLException {
		storeConnection(connection, vmTaskId);
		return connection;
	}

	public static void storeConnection(Connection conn, String vmTaskId) throws SQLException {
		if (!enableRegister)
			return;

		if (vmTaskId == null) {
//			log.warn("数据库操作获取connection,但无法给当前Connection赋vmTaskId");
			return;
		}
		if ("".equals(vmTaskId)) {
//			log.debug("当前连接无需注册");
			return;
		}

		// select SID,serial# from v$session s where s.SID in (SELECT
		// sys_context('USERENV','SID') from dual)
		// log.debug("store conn ,"+vmTaskId);
		SessionProperties pro = null;
		if (conn instanceof MyPoolGuardConnectionWrapper) {
			pro = ((MyPoolGuardConnectionWrapper) conn).getSessionProperties();
		}
		else if (conn instanceof MyDriverManagerConnectionWrapper) {
			// ((MyDriverManagerConnectionWrapper) conn).serverClose();
			pro = ((MyDriverManagerConnectionWrapper) conn).getSessionProperties();
		}else {
			log.warn("当前连接不支持注册");
		}
		if (pro != null) {
			// log.debug("store conn ," + vmTaskId + "," + pro);
			connMap.put(vmTaskId, pro);
		}

	}

	/**
	 * 获取某主线程的子线程所有使用的连接
	 * 
	 * @param ctx
	 * @return
	 */
	public static List getSubThreadUsedConnection(ThreadLocalContext ctx) {
		List list = (List) threadConnMap.get(ctx.getMianThreadId());
		if(list!=null)
		ctx.getLog().debug("get " + ctx.getMianThreadId() + ",size=" + list.size());
		return list;
	}

	/**
	 * 获取某主线程的子线程所有使用的连接,如果未关闭，则关闭
	 * 
	 * @param ctx
	 */
	public static void closeSubThreadUnCloseConnection(ThreadLocalContext ctx) {
		try {
			ctx.getLog().debug("获取某主线程的子线程所有使用的连接,如果未关闭，则关闭");
			List connList = getSubThreadUsedConnection(ctx);
			if (connList == null) {
				return;
			}
			for (int i = 0; i < connList.size(); i++) {
				Connection conn = (Connection) connList.get(i);
				if (conn == null) {
					continue;
				}
				try {
					if (!conn.isClosed()) {
						ctx.getLog().debug("close conn" + conn.toString());
						DbUtils.rollbackAndCloseQuietly(conn);
					}
				} catch (Exception e) {
					ctx.getLog().error("关闭子线程连接失败", e);
				}

			}
		} catch (Exception e) {
			ctx.getLog().error("关闭子线程连接失败", e);
		} finally {
			threadConnMap.remove(ctx.getMianThreadId());
		}
	}

	/**
	 * 任务结束是必须调用此方法，销毁内存结构存储的所有Connection引用
	 */
	public static void destory(String vmTaskId) {
		try {
			connMap.remove(vmTaskId);
			removeTask();
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public static void destory() {
		connMap.clear();
	}

	/**
	 * 当前方法为子线程中获取连接的方法，此方法实现的目的是在子线程获取连接时，能够注册当前连接
	 * 
	 * @param ctx
	 * @param ds
	 * @return
	 * @throws SQLException
	 * @throws UnsupportedDataTypeException
	 */
	public static Connection subThreadGetConnection(ThreadLocalContext ctx, DataSource ds) throws SQLException, UnsupportedDataTypeException {
		String vmTaskId = ctx.getVmTaskId();
		Connection conn = null;
		if (ds instanceof PoolDataSource) {
			conn = ((PoolDataSource) ds).getConnection(vmTaskId);
		} else if (ds instanceof MyDriverManagerDataSource) {
			conn = ((MyDriverManagerDataSource) ds).getConnection(vmTaskId);
		} else {
			throw new UnsupportedDataTypeException("当前连继池，既非 DriverManagerDataSource实例，也非PoolDataSource实例");
		}
		// ctx.getLog().debug("put "+ctx.getMianThreadId()+" "+conn.toString());
		try {
			threadConnMap.put(ctx.getMianThreadId(), conn);
		} catch (Exception e) {
			log.error("",e);
		}
		
		return conn;
	}

	/**
	 * 通过此方法获取的连接无需注册
	 * 
	 * @param ds
	 * @return
	 * @throws SQLException
	 * @throws UnsupportedDataTypeException
	 */
	public static Connection getUnRegisterConnection(DataSource ds) throws SQLException, UnsupportedDataTypeException {
		if (ds instanceof PoolDataSource) {
			return ((PoolDataSource) ds).getConnection("");
		} else if (ds instanceof MyDriverManagerDataSource) {
			return ((MyDriverManagerDataSource) ds).getConnection("");
		} else {
			throw new UnsupportedDataTypeException("当前连继池，既非 DriverManagerDataSource实例，也非PoolDataSource实例");
		}
	}

	/**
	 * 通过关闭某个任务
	 * 
	 * @param vmTaskId
	 * @throws SQLException
	 */
	public static void closeTask(String vmTaskId) throws SQLException {
		SessionProperties sessionProperties = (SessionProperties) connMap.get(vmTaskId);
		forseClose(sessionProperties);

	}

	public static void forseClose(SessionProperties pro) throws SQLException {
		Connection conn = null;
		try {
			org.springframework.jdbc.datasource.DriverManagerDataSource dataSource = new org.springframework.jdbc.datasource.DriverManagerDataSource();
			dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
			dataSource.setUrl(pro.getUrl());
			dataSource.setUsername(pro.getUsername());
			dataSource.setPassword(pro.getPassword());
			String sql = "ALTER SYSTEM DISCONNECT SESSION '" + pro.getSid() + "," + pro.getSerial() + "' IMMEDIATE";
			log.debug(pro.getUrl()+","+pro.getUsername()+","+sql);
			conn = dataSource.getConnection();
			QueryRunner runner = new QueryRunner();
			runner.execute(conn, sql);
		} catch (Exception e) {
			log.error(pro.getUrl()+","+pro.getUsername()+","+e.getMessage());
		} finally {
			DbUtils.closeQuietly(conn);
		}

	}

	public static long[] getOracleSessionProperties(Connection conn) throws SQLException {
		// log.debug("查看当前session的属性:SID,serial#");
		final String sql = "select SID,serial# from v$session s where s.SID in (SELECT sys_context('USERENV','SID') from dual)";
		QueryRunner runner = new QueryRunner();
		long[] result = runner.query(conn, sql, new ResultSetHandler<long[]>() {

			@Override
			public long[] handle(ResultSet rs) throws SQLException {
				long[] result = new long[2];
				if (rs.next()) {
					long sid = rs.getLong(1);
					long serial = rs.getLong(2);
					result[0] = sid;
					result[1] = serial;
					return result;
				}
				throw new SQLException("无法查询当前Session属性:" + sql);
			}

		});
		return result;

	}

}
