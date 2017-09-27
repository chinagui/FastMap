package com.navinfo.dataservice.commons.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.dbutils.DbUtils;

/**
 * @Title: ExportSqliteUtils
 * @Package: com.navinfo.dataservice.scripts
 * @Description:
 * @Author: LittleDog
 * @Date: 2017年9月20日
 * @Version: V1.0
 */

/**
 * 使用说明：
 * 		1.尽量避免生成的文件名路径（即：dir）不同
 * 		2.若文件名路径相同时，尽量避免表名（即：tableName）不同
 * 		3.若两次导出文件名路径（即：dir）及表名（即：tableName）相同时，则将表中数据清除掉，再将需要导出的数据写入
 * 
 * 		String[] columnName：表示生成的表的列，该数组中元素的顺序需与Collection<T> dataset 中实体类字段的声明顺序一致
 * 特别注意：
 * 		1.String tableName 该参数即表示生成表的名称（区分大小写）
 * 		2.改导出功能目前仅支持（int,long,float,double,Integer,Long,Float,Double,String）数据类型
 * 			（若需存日期类型，请使用String类型。sqlite中日期类型用TEXT类型表示）
 * 		3.java.lang.Class 的 getDeclaredFields() 方法返回数组中的元素没有排序，也没有任何特定的顺序（重点注意）
 * 
 * @author LittleDog
 */
public class ExportSqliteUtils {

	private ExportSqliteUtils () {};
	
	private static ExportSqliteUtils instance = new ExportSqliteUtils();
	
	public static ExportSqliteUtils getInstance(){
		return instance;
	}
	
	static{
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private Connection getSqliteConn(String dir) throws Exception {
		return DriverManager.getConnection("jdbc:sqlite:" + dir);
	}

	/**
	 * 导出.db文件
	 * @param dir
	 * @param tableName
	 * @param columnName
	 * @param dataset
	 * @throws Exception
	 */
	public <T> void exportSqliteFile(String dir, String tableName, String[] columnName, Collection<T> dataset) throws Exception {
		
		Connection sqliteConn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			sqliteConn = getSqliteConn(dir);
			if (judgeTableIsExist(sqliteConn, tableName)) {
				deleteData(sqliteConn, tableName);
				insertData(sqliteConn, tableName, dataset);
			} else {
				createTable(sqliteConn, tableName, columnName, dataset);
				insertData(sqliteConn, tableName, dataset);
			}
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(sqliteConn);
			throw new Exception(e);
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
			DbUtils.commitAndCloseQuietly(sqliteConn);
		}
	}

	/**
	 * 
	 * @param sqliteConn
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	private Boolean judgeTableIsExist(Connection sqliteConn, String tableName) throws Exception {

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			String sql = "SELECT COUNT(*) FROM SQLITE_MASTER WHERE TYPE = 'table' AND NAME = ?";

			pstmt = sqliteConn.prepareStatement(sql);
			pstmt.setString(1, tableName);
			rs = pstmt.executeQuery();
			
			Boolean flag = false;
			if (rs.next()) {
				int count = rs.getInt(1);
				if (count == 1) {
					flag = true;
				} else if (count == 0) {
					flag = false;
				} else {
					throw new Exception(tableName + "该表在该库中不唯一");
				}
			}

			return flag;
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
	}

	/**
	 * 
	 * @param sqliteConn
	 * @param tableName
	 * @param columnName
	 * @param dataset
	 * @throws Exception
	 */
	private <T> void createTable(Connection sqliteConn, String tableName, String[] columnName, Collection<T> dataset)
			throws Exception {
		if(dataset == null || dataset.size() == 0){
			throw new Exception("创建表失败：传入集合为空，无数据");
		}
		
		Iterator<T> iterator = dataset.iterator();
		StringBuilder sbuilder = new StringBuilder();
		
		if (iterator.hasNext()) {
			T obj = (T) iterator.next();
			
			Field[] fields = obj.getClass().getDeclaredFields();
			
			if(columnName.length != fields.length){
				throw new Exception("创建表失败：列名个数与对象的字段个数不相同");
			}
			
			sbuilder.append("CREATE TABLE ").append(tableName).append("( ");
			
			for (short i = 0; i < fields.length; i++) {
				Field field = fields[i];
				Class<?> fieldClz = field.getType();
				
				if(fieldClz == int.class || fieldClz == Integer.class){
					sbuilder.append(columnName[i]).append(" INTEGER , ");
				} else if(fieldClz == long.class || fieldClz == Long.class){
					sbuilder.append(columnName[i]).append(" INTEGER , ");
				} else if(fieldClz == float.class || fieldClz == Float.class){
					sbuilder.append(columnName[i]).append(" REAL , ");
				} else if(fieldClz == double.class || fieldClz == Double.class){
					sbuilder.append(columnName[i]).append(" REAL , ");
				} else if(fieldClz == String.class){
					sbuilder.append(columnName[i]).append(" TEXT , ");
				}
				
			}

			sbuilder.deleteCharAt(sbuilder.length() - 2).append(" )");
		}

		if (sbuilder != null && sbuilder.length() != 0) {
			PreparedStatement pstmt = null;
			try {
				pstmt = sqliteConn.prepareStatement(sbuilder.toString());
				pstmt.executeUpdate();
			} catch (Exception e) {
				throw new Exception(e);
			} finally {
				DbUtils.closeQuietly(pstmt);
			}
		}

	}

	/**
	 * 
	 * @param sqliteConn
	 * @param tableName
	 * @throws Exception
	 */
	private static void deleteData (Connection sqliteConn, String tableName) throws Exception {
		PreparedStatement pstmt = null;

		try {
			StringBuilder sBuilder = new StringBuilder();
			sBuilder.append(" DELETE FROM ").append(tableName);
			pstmt = sqliteConn.prepareStatement(sBuilder.toString());

			pstmt.executeUpdate();
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/**
	 * 
	 * @param sqliteConn
	 * @param tableName
	 * @param dataset
	 * @throws Exception
	 */
	private static <T> void insertData(Connection sqliteConn, String tableName, Collection<T> dataset) throws Exception {
		
		Iterator<T> iterator = dataset.iterator();
		StringBuilder sbuilder = new StringBuilder();
		int a = 0;
		while (iterator.hasNext()) {
			a ++ ;
			T obj = (T) iterator.next();
			Field[] fields = obj.getClass().getDeclaredFields();
			if(a < 2){
				sbuilder.append(" INSERT INTO ").append(tableName).append(" VALUES ( ");
				for (short i = 0; i < fields.length; i++) {
					sbuilder.append(" ? ,");
				}
				sbuilder.deleteCharAt(sbuilder.length() - 1).append(" )");
			}
			
			if (sbuilder != null && sbuilder.length() != 0) {
				PreparedStatement pstmt = null;
				try {
					pstmt = sqliteConn.prepareStatement(sbuilder.toString());
					
					for (short i = 0; i < fields.length; i++) {
						Field field = fields[i];
						String fieldName = field.getName();
						String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
						Class<?> clazz = obj.getClass();
						
						Method method = clazz.getMethod(getMethodName);
						Object ret = method.invoke(obj);
						
						pstmt.setObject(i + 1, ret);
					}
					
					pstmt.executeUpdate();
				} catch (Exception e) {
					throw new Exception(e);
				} finally {
					DbUtils.closeQuietly(pstmt);
				}
			}
			
		}
			
	}

}
