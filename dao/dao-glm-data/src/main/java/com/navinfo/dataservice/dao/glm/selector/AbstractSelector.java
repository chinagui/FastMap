/**
 * 
 */
package com.navinfo.dataservice.dao.glm.selector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;

/**
 * @ClassName: BasicSelector
 * @author Zhang Xiaolong
 * @date 2016年7月26日 下午2:14:18
 * @Description: TODO
 */
public class AbstractSelector implements ISelector {

	private IRow row;

	private Connection conn;

	public AbstractSelector(Class<?> cls, Connection conn) throws InstantiationException, IllegalAccessException {
		this.row = (IRow) cls.newInstance();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		StringBuilder sb = new StringBuilder(
				"select * from " + row.tableName() + " where " + row.parentPKName() + " = :1 and u_record !=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				ReflectionAttrUtils.executeResultSet(row, resultSet);

				Map<Class<? extends IRow>, List<IRow>> childMap = row.childMap();

				for (Map.Entry<Class<? extends IRow>, List<IRow>> entry : childMap.entrySet()) {
					Class<? extends IRow> cls = entry.getKey();
					List<IRow> childRows = loadRowsByClassParentId(cls, id, isLock);
					if (CollectionUtils.isNotEmpty(childRows)) {
						entry.getValue().addAll(childRows);
					}
				}
			} else {
				throw new Exception("查询的PID为：" + id + "的" + row.tableName().toUpperCase() + "不存在");
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return row;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		String sql = "select * from " + row.tableName() + " where row_id=hextoraw(:1)";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setString(1, rowId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				ReflectionAttrUtils.executeResultSet(row, resultSet);
			} else {

				throw new DataNotFoundException("数据不存在");
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return row;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from " + row.tableName() + " where " + row.parentPKName() + "=:1 and u_record!=:2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			pstmt.setInt(2, 2);

			System.out.println(sql);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				ReflectionAttrUtils.executeResultSet(row, resultSet);

				rows.add(row);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return rows;
	}

	private List<IRow> loadRowsByClassParentId(Class<? extends IRow> cls, int id, boolean isLock) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		IRow row = cls.newInstance();

		String sql = "select * from " + row.tableName() + " where " + row.parentPKName() + "=:1 and u_record!=:2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			pstmt.setInt(2, 2);

			System.out.println(sql);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				ReflectionAttrUtils.executeResultSet(row, resultSet);

				rows.add(row);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return rows;
	}

}
