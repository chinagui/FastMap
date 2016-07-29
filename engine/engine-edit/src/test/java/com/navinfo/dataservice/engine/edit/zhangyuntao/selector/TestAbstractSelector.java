package com.navinfo.dataservice.engine.edit.zhangyuntao.selector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

/**
 * @Title: AbstractSelector.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月26日 下午3:20:33
 * @version: v1.0
 */
public abstract class TestAbstractSelector<T extends IRow> {
	private Class<T> clazz;

	private Connection conn;

	public TestAbstractSelector(Connection conn, Class<T> clazz) {
		this.clazz = clazz;
		this.conn = conn;
	}

	public TestAbstractSelector(Class<T> clazz) {
		this.clazz = clazz;
	}

	public Class<T> getClazz() {
		return clazz;
	}

	public void setClazz(Class<T> clazz) {
		this.clazz = clazz;
	}

	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public IRow loadById(int id, boolean isLock) throws Exception {
		T t = clazz.newInstance();
		StringBuffer sb = new StringBuffer();
		sb.append("select * from ");
		sb.append(clazz.getMethod("tableName").invoke(t) + " ");
		sb.append("where ");
		sb.append(clazz.getMethod("parentPKName").invoke(t) + " ");
		sb.append("= :1 ");

		if (isLock) {
			sb.append("for update nowait");
		}

		PreparedStatement ptst = null;
		ResultSet resultSet = null;
		ptst = this.conn.prepareStatement(sb.toString());
		ptst.setInt(1, id);
		resultSet = ptst.executeQuery();
		if (resultSet.next()) {
			ReflectionAttrUtils.executeResultSet(t, resultSet);
			this.loadChildren(t, isLock);
		}

		return t;
	}

	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		T t = clazz.newInstance();
		StringBuffer sb = new StringBuffer();
		sb.append("select * from ");
		sb.append(clazz.getMethod("tableName").invoke(t) + " ");
		sb.append("where row_id ");
		sb.append("= hextoraw(:1) ");

		if (isLock) {
			sb.append("for update nowait");
		}

		PreparedStatement ptst = null;
		ResultSet resultSet = null;
		ptst = this.conn.prepareStatement(sb.toString());
		ptst.setString(1, rowId);
		resultSet = ptst.executeQuery();
		if (resultSet.next()) {
			ReflectionAttrUtils.executeResultSet(t, resultSet);
		}

		return t;
	}

	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();
		T t = clazz.newInstance();

		StringBuffer sb = new StringBuffer();
		sb.append("select * from ");
		sb.append(clazz.getMethod("tableName").invoke(t) + " ");
		sb.append("where ");
		sb.append(clazz.getMethod("parentPKName").invoke(t) + " ");
		sb.append("= :1 ");
		if (isLock) {
			sb.append("for update nowait");
		}

		PreparedStatement ptst = null;
		ResultSet resultSet = null;
		ptst = this.conn.prepareStatement(sb.toString());
		ptst.setInt(1, id);
		resultSet = ptst.executeQuery();
		while (resultSet.next()) {
			t = clazz.newInstance();
			ReflectionAttrUtils.executeResultSet(t, resultSet);
			rows.add(t);
		}
		return rows;
	}

	@SuppressWarnings("unused")
	private void loadChildren(IRow row, boolean isLock) throws Exception {
		// Map<Class<? extends IRow>, List<IRow>> maps = row.related();
		Map<Class<? extends IRow>, List<IRow>> maps = new HashMap<Class<? extends IRow>, List<IRow>>();
		if (null == maps) {
			return;
		} else {
			for (Class<? extends IRow> cla : maps.keySet()) {
				IRow r = cla.newInstance();
				List<IRow> children = this.loadRowsByParentId(r, row.parentPKValue(), isLock);

				maps.get(cla).addAll(children);

				this.loadChildren(r, isLock);
			}
		}
	}

	private List<IRow> loadRowsByParentId(IRow row, int parentPid, boolean isLock) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		StringBuffer sb = new StringBuffer();
		sb.append("select * from ");
		sb.append(row.tableName() + " ");
		sb.append("where ");
		sb.append(row.parentPKName() + " ");
		sb.append("= :1 ");
		if (isLock) {
			sb.append("for update nowait");
		}

		PreparedStatement ptst = null;
		ResultSet resultSet = null;
		ptst = this.conn.prepareStatement(sb.toString());
		ptst.setInt(1, parentPid);
		resultSet = ptst.executeQuery();
		while (resultSet.next()) {
			ReflectionAttrUtils.executeResultSet(row, resultSet);
			rows.add(row);
		}
		return rows;
	}
}
