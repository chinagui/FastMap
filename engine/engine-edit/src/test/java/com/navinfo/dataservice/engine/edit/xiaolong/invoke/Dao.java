/**
 * 
 */
package com.navinfo.dataservice.engine.edit.xiaolong.invoke;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

public class Dao<T> {

	// 查询一条记录，返回对应的对象
	public <T> T query(Class<T> clazz, String sql, Connection ct,Object... args) throws SQLException {
		T example = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// 1、获取connection
			System.out.println("query获取到数据库的连接了");
			// 2、获取PreparedStatement
			ps = ct.prepareStatement(sql);
			// 3、填充占位符
			for (int i = 0; i < args.length; i++) {
				ps.setObject(i + 1, args[i]);
			}
			// 4、进行查询，得到ResultSet
			rs = ps.executeQuery();
			// 5、准备一个Map<String,Object>:(前提是结果集中要有记录)
			if (rs.next()) {
				Map<String, Object> values = new HashMap<String, Object>();

				// 6、得到ResultSetMetaData对象
				ResultSetMetaData rsd = rs.getMetaData();
				// 7、处理ResultSet,把指针向下移动一个单位

				// 8、由ResultSetMetaData对象得到结果集中有多少列
				int lieshu = rsd.getColumnCount();
				// 9、由ResultSetMetaData对象得到每一列的别名，由ResultSet得到具体每一列的值
				for (int i = 0; i < lieshu; i++) {
					String columnName = rsd.getColumnLabel(i + 1);
					Object columnValue= rs.getObject(i + 1);
					// 10、填充Map对象
					if(columnName.toLowerCase().equals("geometry"))
					{
						Geometry geometry = GeoTranslator.struct2Jts((STRUCT)columnValue, 100000, 0);
						System.out.println(columnName+":"+columnValue);
						values.put(columnName, geometry);
					}
					else
					{
						System.out.println(columnName+":"+columnValue);
						values.put(columnName, columnValue);
					}
				}

				// 11、用反射创建Class对应的对象
				example = clazz.newInstance();
				// 12、遍历Map对象，用反射填充对象的属性值，
				for (Map.Entry<String, Object> ent : values.entrySet()) {
					String name = ent.getKey();
					Object value = ent.getValue();
					// 用反射赋值
					ReflectionUtils.setFieldValue(example, name, value);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DbUtils.close(ct);
		}
		return example;

	}

	// 查询多条记录，返回多个对应的对象
	public List<T> querylist(Class<T> clazz, String sql,Connection ct, Object... args) {
		T example = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List li = new ArrayList();
		try {
			// 1、获取connection
			System.out.println("querylist获取到数据库的连接了");
			// 2、获取PreparedStatement
			ps = ct.prepareStatement(sql);
			// 3、填充占位符
			for (int i = 0; i < args.length; i++) {
				ps.setObject(i + 1, args[i]);
			}
			// 4、进行查询，得到ResultSet
			rs = ps.executeQuery();
			// 5、准备一个Map<String,Object>:(前提是结果集中要有记录)

			while (rs.next()) {
				Map<String, Object> values = new HashMap<String, Object>();

				// 6、得到ResultSetMetaData对象
				ResultSetMetaData rsd = rs.getMetaData();
				// 7、处理ResultSet,把指针向下移动一个单位

				// 8、由ResultSetMetaData对象得到结果集中有多少列
				int lieshu = rsd.getColumnCount();
				// 9、由ResultSetMetaData对象得到每一列的别名，由ResultSet得到具体每一列的值
				for (int i = 0; i < lieshu; i++) {
					String lieming = rsd.getColumnLabel(i + 1);
					Object liezhi = rs.getObject(i + 1);
					// 10、填充Map对象
					values.put(lieming, liezhi);
				}

				// 11、用反射创建Class对应的对象
				example = clazz.newInstance();
				// 12、遍历Map对象，用反射填充对象的属性值，
				for (Map.Entry<String, Object> ent : values.entrySet()) {
					String name = ent.getKey();
					Object value = ent.getValue();
					// 用反射赋值
					ReflectionUtils.setFieldValue(example, name, value);

				}
				li.add(example);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				DbUtils.close(ct);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return li;

	}
}