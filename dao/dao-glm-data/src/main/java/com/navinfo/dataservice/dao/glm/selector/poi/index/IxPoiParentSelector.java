package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParentForAndroid;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

/**
 * POI父子关系父表 查询
 * 
 * @author luyao
 *
 */
public class IxPoiParentSelector extends AbstractSelector {

	private static Logger logger = Logger.getLogger(IxPoiParentSelector.class);

	private Connection conn;

	public IxPoiParentSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(IxPoiParent.class);
	}

	/**
	 * 加载poi做为父poi时，所有的poi父子关系
	 * 
	 * @param id
	 *            被查poi的pid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock,boolean...delFlag) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		StringBuilder sb = new StringBuilder();
		if (delFlag == null || delFlag.length == 0 || !delFlag[0]) {
			sb.append(" SELECT * FROM ix_poi_parent WHERE parent_poi_pid=:1 AND u_record!=2");
		}else{
			sb.append(" SELECT * FROM ix_poi_parent WHERE parent_poi_pid=:1 ");
		}

		sb.append(" AND EXISTS (SELECT null FROM ix_poi_children c WHERE c.group_id IN ");

		sb.append(" (SELECT group_id FROM ix_poi_parent where Parent_Poi_Pid =:2))");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			pstmt.setInt(2, id);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				IxPoiParent poiParent = new IxPoiParent();

				poiParent.setPid(resultSet.getInt("group_id"));

				poiParent.setParentPoiPid(resultSet.getInt("parent_poi_pid"));

				poiParent.setTenantFlag(resultSet.getInt("tenant_flag"));

				poiParent.setMemo(resultSet.getString("memo"));

				poiParent.setRowId(resultSet.getString("row_id"));

				poiParent.setuDate(resultSet.getString("u_date"));

				// 获取IX_POI_PARENT对应的关联数据
				// ix_poi_children
				List<IRow> poiChildrens = new IxPoiChildrenSelector(conn).loadRowsByParentId(poiParent.getPid(),
						isLock,delFlag);

				poiParent.setPoiChildrens(poiChildrens);

				for (IRow row : poiParent.getPoiChildrens()) {
					IxPoiChildren children = (IxPoiChildren) row;

					poiParent.poiChildrenMap.put(children.rowId(), children);
				}

				rows.add(poiParent);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {

			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}

		return rows;
	}

	/**
	 * 加载poi做为子poi时，所有的poi父子关系
	 * 
	 * @param id
	 *            被查poi的pid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<IRow> loadParentRowsByChildrenId(int id, boolean isLock) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "SELECT * FROM ix_poi_parent WHERE group_id IN (SELECT group_id FROM ix_poi_children WHERE child_poi_pid = :1 AND u_record != :2)";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				IxPoiParent poiParent = new IxPoiParent();

				ReflectionAttrUtils.executeResultSet(poiParent, resultSet);

				// 获取IX_POI_PARENT对应的关联数据
				// ix_poi_children
				List<IRow> poiChildrens = new IxPoiChildrenSelector(conn).loadRowsByParentId(poiParent.getPid(),
						isLock);

				poiParent.setPoiChildrens(poiChildrens);

				for (IRow row : poiParent.getPoiChildrens()) {
					IxPoiChildren children = (IxPoiChildren) row;

					poiParent.poiChildrenMap.put(children.rowId(), children);
				}

				rows.add(poiParent);
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return rows;
	}

	/**
	 * 加载且仅加载poi做为父poi时，IX_POI_PARENT表
	 * 
	 * @param id
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<IRow> loadParentRowsByPoiId(int id, boolean isLock,boolean ...delFlag) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();
		String sql =null;
		if (delFlag == null || delFlag.length == 0 || !delFlag[0]) {
			sql = "SELECT * FROM IX_POI_PARENT WHERE group_id IN (SELECT group_id FROM IX_POI_CHILDREN WHERE CHILD_POI_PID = :1 AND U_RECORD != 2)";
		}else{
			sql = "SELECT * FROM IX_POI_PARENT WHERE group_id IN (SELECT group_id FROM IX_POI_CHILDREN WHERE CHILD_POI_PID = :1)";
		}
		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				IxPoiParent poiParent = new IxPoiParent();

				ReflectionAttrUtils.executeResultSet(poiParent, resultSet);

				rows.add(poiParent);
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return rows;
	}

	/**
	 * add by wangdongbin for android download
	 * 
	 * @param id
	 * @return IxPoiParent
	 * @throws Exception
	 */
	public List<IRow> loadByIdForAndroid(int id) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();
		IxPoiParentForAndroid poiParent = new IxPoiParentForAndroid();
		IxPoiChildren poiCheildre = new IxPoiChildren();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			// 直接进行联结查询，对结果进行判断
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT i.poi_num,c.relation_type");
			sb.append(" FROM " + poiParent.tableName() + " p");
			sb.append(" ," + poiCheildre.tableName() + " c");
			sb.append(" ,ix_poi i");
			sb.append(" WHERE p.group_id=c.group_id");
			sb.append(" AND c.child_poi_pid = :1");
			sb.append(" AND c.u_record !=2");
			sb.append(" AND p.parent_poi_pid=i.pid");
			sb.append(" ORDER BY p.parent_poi_pid ASC");
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, id);
			resultSet = pstmt.executeQuery();
			String poiNum = "";
			if (resultSet.next()) {
				poiNum = resultSet.getString("poi_num");
			}
			while (resultSet.next()) {
				if (resultSet.getInt("relation_type") == 2) {
					poiNum = resultSet.getString("poi_num");
					break;
				}
			}
			poiParent.setPoiNum(poiNum);
			rows.add(poiParent);
		} catch (Exception e) {
			throw e;
		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}
		return rows;
	}
}
