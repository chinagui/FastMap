/**
 * 
 */
package com.navinfo.dataservice.dao.glm.selector;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiEditStatus;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLinkName;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiEditStatusSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiParentSelector;

/**
 * @ClassName: BasicSelector
 * @author Zhang Xiaolong
 * @date 2016年7月26日 下午2:14:18
 */
public class AbstractSelector implements ISelector {

	private IRow row;

	private Class<?> cls;

	private Connection conn;

	public Connection getConn() {
		return conn;
	}

	public AbstractSelector(Connection conn) {
		this.conn = conn;
	}

	public AbstractSelector(Class<?> cls, Connection conn) {
		this.cls = cls;
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock, boolean... noChild) throws Exception {
		this.row = (IRow) cls.newInstance();
		String sql=	"select * from " + row.tableName() + " where " + ((IObj) row).primaryKey() + " = :1 and u_record !=2";
		StringBuilder sb = new StringBuilder(sql);

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
				// 设置主表信息
				ReflectionAttrUtils.executeResultSet(row, resultSet);
				if (noChild == null || noChild.length == 0 || !noChild[0]) {
					if (row instanceof IObj) {
						IObj obj = (IObj) row;
						// 子表list map
						Map<Class<? extends IRow>, List<IRow>> childList = obj.childList();

						// 子表map
						Map<Class<? extends IRow>, Map<String, ?>> childMap = obj.childMap();
						if (childList != null) {
							setChildValue(obj, childList, childMap, isLock);
						}
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
	public IRow loadAllById(int id, boolean isLock, boolean... noChild) throws Exception {
		this.row = (IRow) cls.newInstance();
		String sql=	"select * from " + row.tableName() + " where " + ((IObj) row).primaryKey() + " = :1";
		StringBuilder sb = new StringBuilder(sql);

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
				// 设置主表信息
				ReflectionAttrUtils.executeResultSet(row, resultSet);
				if (noChild == null || noChild.length == 0 || !noChild[0]) {
					if (row instanceof IObj) {
						IObj obj = (IObj) row;
						// 子表list map
						Map<Class<? extends IRow>, List<IRow>> childList = obj.childList();

						// 子表map
						Map<Class<? extends IRow>, Map<String, ?>> childMap = obj.childMap();
						if (childList != null) {
							setChildValue(obj, childList, childMap, isLock);
						}
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
	
	@SuppressWarnings("unchecked")
	@Override
	public IRow loadByIdAndChildClass(int id, boolean isLock, Class<? extends IRow>... childClass) throws Exception {
		try {
			IRow row = loadById(id, isLock);
			// 设置子表信息
			if (childClass != null) {
				if (row instanceof IObj) {
					IObj obj = (IObj) row;
					// 子表list map
					Map<Class<? extends IRow>, List<IRow>> childList = obj.childList();

					// 子表map
					Map<Class<? extends IRow>, Map<String, ?>> childMap = obj.childMap();

					setChildValueForClass(obj, childList, childMap, isLock, childClass);
				}
			}
		} catch (

		Exception e) {
			throw e;
		}

		return row;

	}

	@Override
	public List<IRow> loadByIds(List<Integer> idList, boolean isLock, boolean loadChild) throws Exception {
		List<IRow> rowList = new ArrayList<>();
		this.row = (IRow) cls.newInstance();

		String ids = org.apache.commons.lang.StringUtils.join(idList, ",");

		String primaryKey = ((IObj) row).primaryKey();

		String inClause = null;

		Clob pidClod = null;
		if (idList.size() > 1000) {
			pidClod = conn.createClob();
			pidClod.setString(1, ids);
			inClause = primaryKey + " IN (select to_number(pid) from table(clob_to_table(?)))";
		} else {
			inClause = primaryKey + " IN (" + ids + ")";
		}

		StringBuilder sb = new StringBuilder("select * from " + row.tableName() + " where " + inClause
				+ " and u_record !=2 ORDER BY DECODE (" + primaryKey + "," + ids + ")");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			
			if(idList.size() > 1000)
			{
				pstmt.setClob(1, pidClod);
			}

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				// 设置主表信息
				IRow row = (IRow) cls.newInstance();
				ReflectionAttrUtils.executeResultSet(row, resultSet);
				// 设置子表信息
				if (loadChild) {
					if (row instanceof IObj) {
						IObj obj = (IObj) row;
						// 子表list map
						Map<Class<? extends IRow>, List<IRow>> childList = obj.childList();

						// 子表map
						Map<Class<? extends IRow>, Map<String, ?>> childMap = obj.childMap();
						if (childList != null) {
							setChildValue(obj, childList, childMap, isLock);
						}
					}
				}
				rowList.add(row);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return rowList;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		this.row = (IRow) cls.newInstance();
		String sql = "select * from " + row.tableName() + " where row_id=hextoraw(:1) and u_record !=2 ";

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
		this.row = (IRow) cls.newInstance();

		List<IRow> rows = new ArrayList<IRow>();

		String sql = "";

		if (row instanceof RdLinkName || row instanceof RwLinkName) {
			sql = "select a.*,b.name from " + row.tableName() + " a,rd_name b where a." + row.parentPKName()
					+ " =:1 and a.name_groupid=b.name_groupid(+) and b.lang_code(+)='CHI' and a.u_record!=:2";
		} else {
			sql = "select * from " + row.tableName() + " where " + row.parentPKName() + "=:1 and u_record!=:2";

			if (isLock) {
				sql += " for update nowait";
			}
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				IRow rowInner = (IRow) cls.newInstance();

				ReflectionAttrUtils.executeResultSet(rowInner, resultSet);

				rows.add(rowInner);
				if (rowInner instanceof IObj) {
					IObj obj = (IObj) rowInner;
					// 子表list map
					Map<Class<? extends IRow>, List<IRow>> childList = obj.childList();

					// 子表map
					Map<Class<? extends IRow>, Map<String, ?>> childMap = obj.childMap();
					if (childList != null) {
						setChildValue(obj, childList, childMap, isLock);
					}
				}
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return rows;
	}
	
	public List<IRow> loadBySql(String sql, boolean isLock,boolean loadChild) throws Exception {
		List<IRow> rowList = new ArrayList<>();
		this.row = (IRow) cls.newInstance();

		StringBuilder sb = new StringBuilder(sql);
		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				// 设置主表信息
				IRow row = (IRow) cls.newInstance();
				ReflectionAttrUtils.executeResultSet(row, resultSet);
				// 设置子表信息
				if (loadChild) {
					if (row instanceof IObj) {
						IObj obj = (IObj) row;
						// 子表list map
						Map<Class<? extends IRow>, List<IRow>> childList = obj.childList();

						// 子表map
						Map<Class<? extends IRow>, Map<String, ?>> childMap = obj.childMap();
						if (childList != null) {
							setChildValue(obj, childList, childMap, isLock);
						}
					}
				}
				rowList.add(row);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return rowList;
	}

	public List<IRow> loadRowsByParentIds(List<Integer> pids, boolean isLock) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		if (pids == null || pids.size() == 0) {
			return rows;
		}

		List<Integer> pidsTemp = new ArrayList<Integer>();

		pidsTemp.addAll(pids);

		int pointsDataLimit = 100;

		while (pidsTemp.size() >= pointsDataLimit) {

			List<Integer> listPid = pidsTemp.subList(0, pointsDataLimit);

			rows.addAll(batchLoadRowsByParentIds(listPid, isLock));

			pidsTemp.subList(0, pointsDataLimit).clear();
		}

		if (!pidsTemp.isEmpty()) {
			rows.addAll(batchLoadRowsByParentIds(pidsTemp, isLock));
		}

		return rows;
	}

	private List<IRow> batchLoadRowsByParentIds(List<Integer> idList, boolean isLock) throws Exception {
		this.row = (IRow) cls.newInstance();

		List<IRow> rows = new ArrayList<IRow>();

		String sql = "";

		if (row instanceof RdLinkName || row instanceof RwLinkName) {
			sql = "select a.*,b.name from " + row.tableName() + " a,rd_name b where a." + row.parentPKName() + " in ("
					+ StringUtils.getInteStr(idList)
					+ ") and a.name_groupid=b.name_groupid(+) and b.lang_code(+)='CHI' and a.u_record!=:2";
		} else {
			sql = "select * from " + row.tableName() + " where " + row.parentPKName() + " in ("
					+ StringUtils.getInteStr(idList) + ") and u_record!=:2";

			if (isLock) {
				sql += " for update nowait";
			}
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				IRow rowInner = (IRow) cls.newInstance();

				ReflectionAttrUtils.executeResultSet(rowInner, resultSet);

				rows.add(rowInner);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return rows;
	}

	/**
	 * 查询子表并且按照需求进行排序
	 * 
	 * @param cls
	 *            查询的子表class类型
	 * @param id
	 *            主表pid
	 * @param isLock
	 *            是否锁表
	 * @param order
	 *            排序
	 * @return 子表集合
	 * @throws Exception
	 */
	public List<IRow> loadRowsByClassParentId(Class<?> cls, int id, boolean isLock, String order) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		IRow row = (IRow) cls.newInstance();

		StringBuilder sql = new StringBuilder();

		if (row instanceof RdLinkName || row instanceof RwLinkName) {
			sql.append("select a.*,b.name from " + row.tableName() + " a,rd_name b where a." + row.parentPKName()
					+ " =:1 and a.name_groupid=b.name_groupid(+) and b.lang_code(+)='CHI' and a.u_record!=:2");
		} else {
			sql.append("select * from " + row.tableName() + " where " + row.parentPKName() + "=:1 and u_record!=:2");
			if (StringUtils.isNotEmpty(order)) {
				sql.append(" order by " + order);
			}

			if (isLock) {
				sql.append(" for update nowait");
			}
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql.toString());

			pstmt.setInt(1, id);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				IRow rowInner = (IRow) cls.newInstance();

				ReflectionAttrUtils.executeResultSet(rowInner, resultSet);

				rows.add(rowInner);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return rows;
	}

	private void setChildValue(IObj obj, Map<Class<? extends IRow>, List<IRow>> childList,
			Map<Class<? extends IRow>, Map<String, ?>> childMap, boolean isLock) throws Exception {
		for (Map.Entry<Class<? extends IRow>, List<IRow>> entry : childList.entrySet()) {
			Class<? extends IRow> cls = entry.getKey();
			List<IRow> values = entry.getValue();
			setChild(cls, obj, isLock, values, childMap);
		}
	}

	private void setChildValueForClass(IObj obj, Map<Class<? extends IRow>, List<IRow>> childList,
			Map<Class<? extends IRow>, Map<String, ?>> childMap, boolean isLock, Class<? extends IRow>[] childClass)
			throws Exception {
		for (Class<? extends IRow> cls : childClass) {
			List<IRow> values = childList.get(cls);
			setChild(cls, obj, isLock, values, childMap);
		}
	}

	@SuppressWarnings("unchecked")
	private void setChild(Class<? extends IRow> cls, IObj obj, boolean isLock, List<IRow> values,
			Map<Class<? extends IRow>, Map<String, ?>> childMap) throws Exception {
		// 特殊场景处理 1.POI父子关系查询
		if (cls.equals(IxPoiParent.class) && obj instanceof IxPoi) {
			handlePoiParent((IxPoi) obj, isLock);
		} else if (cls.equals(IxPoiChildren.class) && obj instanceof IxPoi) {
			handlePoiChildren((IxPoi) obj, isLock);
		} else if (cls.equals(IxPoiEditStatus.class) && obj instanceof IxPoi) {
			// 特殊场景：2.POI_EDIT_STATUS
			handlePoiEditStatus((IxPoi) obj, isLock);
		} else {
			List<IRow> childRows = loadRowsByClassParentId(cls, obj.pid(), isLock, null);
			if (CollectionUtils.isNotEmpty(childRows)) {
				for (IRow row : childRows) {
					if (row instanceof IObj) {
						IObj childObj = (IObj) row;
						// 子表list map
						Map<Class<? extends IRow>, List<IRow>> childObjList = childObj.childList();

						// 子表map
						Map<Class<? extends IRow>, Map<String, ?>> childObjMap = childObj.childMap();
						if (childObjList != null) {
							setChildValue(childObj, childObjList, childObjMap, isLock);
						}
					}

				}
				values.addAll(childRows);
			}
			if (childMap != null) {
				@SuppressWarnings("rawtypes")
				Map map = childMap.get(cls);
				if (map != null) {
					for (IRow row : values) {
						map.put(row.rowId(), row);
					}
				}
			}
		}
	}

	/**
	 * @param ixPoi
	 * @param isLock
	 * @throws Exception
	 */
	private void handlePoiEditStatus(IxPoi ixPoi, boolean isLock) throws Exception {
		IxPoiEditStatusSelector ixPoiEditStatusSelector = new IxPoiEditStatusSelector(conn);

		int status = ixPoiEditStatusSelector.loadStatusByRowId(ixPoi.rowId(), isLock);

		ixPoi.setStatus(status);

	}

	/**
	 * @throws Exception
	 * 
	 */
	private void handlePoiParent(IxPoi ixPoi, boolean isLock) throws Exception {
		// 设置子表IX_POI_PARENT
		IxPoiParentSelector ixPoiParentSelector = new IxPoiParentSelector(conn);

		ixPoi.setParents(ixPoiParentSelector.loadParentRowsByPoiId(ixPoi.getPid(), isLock));

		for (IRow row : ixPoi.getParents()) {
			IxPoiParent obj = (IxPoiParent) row;

			ixPoi.parentMap.put(obj.getRowId(), obj);
		}
	}

	/**
	 * @throws Exception
	 * 
	 */
	private void handlePoiChildren(IxPoi ixPoi, boolean isLock) throws Exception {
		IxPoiParentSelector ixPoiParentSelector = new IxPoiParentSelector(conn);
		// 设置poi的子
		List<IRow> parent = ixPoiParentSelector.loadRowsByParentId(ixPoi.getPid(), isLock);

		if (CollectionUtils.isNotEmpty(parent)) {
			int size = parent.size();

			if (size != 1) {
				throw new Exception("poi作为父数据不唯一");
			} else {
				IxPoiParent poiParent = (IxPoiParent) parent.get(0);

				ixPoi.setChildren(poiParent.getPoiChildrens());

				for (IRow row : ixPoi.getChildren()) {
					IxPoiChildren obj = (IxPoiChildren) row;

					ixPoi.childrenMap.put(obj.getRowId(), obj);
				}
			}
		}
	}

	public IRow getRow() {
		return row;
	}

	public void setRow(IRow row) {
		this.row = row;
	}

	public Class<?> getCls() {
		return cls;
	}

	public void setCls(Class<?> cls) {
		this.cls = cls;
	}
}
