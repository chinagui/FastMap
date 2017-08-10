/**
 * 
 */
package com.navinfo.dataservice.engine.edit.xiaolong.sqlhelper;

/** 
* @ClassName: SqlHelper 
* @author Zhang Xiaolong
* @date 2016年7月15日 上午10:56:17 
* @Description: TODO
*/
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.engine.edit.xiaolong.invoke.ReflectionUtils;

import oracle.sql.STRUCT;

public class SqlHelper {

	/**
	 * 获取数据库连接对象
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Connection getConnection() throws Exception {
		return DBConnector.getInstance().getConnectionById(42);
	}

	/**
	 * 执行update操作，返回受影响的行数
	 * @param sql
	 * @return
	 */
	public static int executeUpdateSql(String sql) {
		int result = -1;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = getConnection();
			ps = con.prepareStatement(sql);
			result = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(con, ps, null);
		}
		return result;
	}

	/**
	 * 执行Insert语句，返回Insert成功之后标识列的值
	 * @param sql 执行的SQL
	 * @return 返回执行后的标识
	 * @throws SQLException 
	 */
	public static int executeIdentity(String sql)  {
		int identity = -1;
		Connection con = null;
		Statement ps = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			ps = con.createStatement();
			ps.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				identity = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(con, ps, null);
			try{
				if(rs!=null)rs.close();
			}catch(Exception e){//do nothing}
		}
		return identity;
	}

	/**
	 * 执行不返回结果集SQL
	 * @param sql 查询SQL
 	 * @param params 参数
	 */
	public static void executeUpdateByParams(String sql, SqlParameter... params) {
		Connection con = null;
		CallableStatement cs = null;
		try {
			con = getConnection();
			cs = con.prepareCall(sql);
			setSqlParameter(cs, params);
			cs.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(con, cs, null);
		}
	}

	/**
	 * 执行含有聚合函数的sql，返回函数值
	 * @param sql 
	 * @return
	 */
	public static int executeQuerySql(String sql) {
		int result = -1;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(con, ps, rs);
		}
		return result;
	}

	/**
	 * 执行返回泛型集合的SQL语句
	 * @param cls 泛型类型
	 * @param sql 查询SQL
	 * @return 返回泛型对象的几何
	 */
	public static <T> List<T> executeList(Class<T> cls, String sql) {
		List<T> list = new ArrayList<T>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				T obj = executeResultSet(cls, rs);
				list.add(obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(con, ps, rs);
		}
		return list;
	}

	/**
	 * 执行返回泛型集合的SQL语句
	 * @param cls
	 * @param sql
	 * @param params
	 * @return
	 */
	public static <T> List<T> executeList(Class<T> cls, String sql, SqlParameter... params) {
		List<T> list = new ArrayList<T>();
		Connection con = null;
		CallableStatement cs = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			cs = con.prepareCall(sql);
			setSqlParameter(cs, params);
			rs = cs.executeQuery();
			while (rs.next()) {
				T obj = executeResultSet(cls, rs);
				list.add(obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(con, cs, rs);
		}
		return list;
	}

	/**
	 * 执行返回泛型类型对象的SQL语句
	 * @param cls 泛型类
	 * @param sql 查询sql
	 * @return 查询对象
	 */
	public static <T> T executeEntity(Class<T> cls, String sql) {
		T obj = null;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				obj = executeResultSet(cls, rs);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(con, ps, rs);
		}
		return obj;
	}

	/**
	 *  执行返回泛型类型对象的存储过程
	 * @param cls 泛型对象
	 * @param sql sql语句
	 * @param params 参数
	 * @return 泛型类型的对象
	 */
	public static <T> T executeEntity(Class<T> cls, String sql, SqlParameter... params) {
		T obj = null;
		Connection con = null;
		CallableStatement cs = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			cs = con.prepareCall(sql);
			setSqlParameter(cs, params);
			rs = cs.executeQuery();
			while (rs.next()) {
				obj = executeResultSet(cls, rs);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(con, cs, rs);
		}
		return obj;
	}

	/**
	 * 将一条记录转成一个对象
	 * 
	 * @param cls
	 *            泛型类型
	 * @param rs
	 *            ResultSet对象
	 * @return 泛型类型对象
	 * @throws Exception
	 */
	private static <T> T executeResultSet(Class<T> cls, ResultSet rs) throws Exception {
		T t = cls.newInstance();
		ResultSetMetaData rsm = rs.getMetaData();
		int columnCount = rsm.getColumnCount();
		Field[] fields = cls.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			String fieldName = field.getName();
			if (fieldName.equals("pid")) {
				IRow obj = (IRow)t;
				String pkName = obj.parentPKName();
				Object value = rs.getInt(pkName);
				field.setAccessible(true);
				field.set(t, value);
				continue;
			}
			for (int j = 1; j <= columnCount; j++) {
				String columnName = rsm.getColumnName(j);
				if (fieldName.equalsIgnoreCase(ReflectionUtils.fieldToProperty(columnName))) {
					int columnType = rsm.getColumnType(j);
					Object value = rs.getObject(j);
					if (Types.VARBINARY == columnType && fieldName.equals("rowId")) {
						String rowId = rs.getString(columnName);
						field.setAccessible(true);
						field.set(t, rowId);
						break;
					}
					if (Types.NUMERIC == columnType) {
						if (value.toString().contains(".")) {
							value = ((BigDecimal) value).doubleValue();
						} else {
							value = Integer.parseInt(value.toString());
						}
					}
					if (Types.STRUCT == columnType) {
						value = GeoTranslator.struct2Jts((STRUCT) value, 100000, 0);
					}
					field.setAccessible(true);
					field.set(t, value);
					break;
				}
			}
		}
		return t;
	}

	/**
	 * 设置存储过程参数名称，参数值，参数方向
	 * 
	 * @param cs
	 * @param params
	 * @throws SQLException
	 */
	private static void setSqlParameter(CallableStatement cs, SqlParameter... params) throws SQLException {
		if (params != null) {
			for (SqlParameter param : params) {
				if (param.OutPut) {
					String paramName = param.Name;
					if (paramName == null || paramName.equals("")) {
						cs.registerOutParameter(1, param.Type);// 设置返回类型参数
					} else {
						cs.registerOutParameter(paramName, param.Type);// 设置输出类型参数
					}
				} else {
					cs.setObject(param.Name, param.Value);// 设置输入类型参数
				}
			}
		}
	}

	/**
	 * 得到存储过程参数执行结果
	 * 
	 * @param cs
	 * @param params
	 * @throws SQLException
	 */
	private static void getSqlParameter(CallableStatement cs, SqlParameter... params) throws SQLException {
		for (SqlParameter param : params) {
			if (param.OutPut) {
				String paramName = param.Name;
				if (paramName == null || paramName.equals("")) {
					param.Value = cs.getObject(1);// 返回类型参数值
				} else {
					param.Value = cs.getObject(paramName);// 输出类型参数值
				}
			}
		}
	}

	/**
	 * 关闭JDBC对象，释放资源。
	 * 
	 * @param con
	 *            连接对象
	 * @param ps
	 *            命令对象
	 * @param rs
	 *            结果集对象
	 * @throws SQLException
	 */
	private static void close(Connection con, Statement ps, ResultSet rs) {
		try {
			DbUtils.close(ps);
			DbUtils.close(rs);
			DbUtils.close(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
