package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;

import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;

/**
 * POI父子关系父表 查询
 * @author luyao
 *
 */
public class IxPoiParentSelector implements ISelector{

	private static Logger logger = Logger.getLogger(IxPoiParentSelector.class);

	private Connection conn;

	public IxPoiParentSelector(Connection conn) {
		super();
		this.conn = conn;
	}
	
	
	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		
		IxPoiParent poiParent = new IxPoiParent();

		StringBuilder sb = new StringBuilder(
				 "select * from " + poiParent.tableName() + " WHERE link_pid = :1 and  u_record !=2");

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
				poiParent.setPid(id);

				poiParent.setPid(resultSet.getInt("group_id"));

				poiParent.setParentPoiPid(resultSet.getInt("parent_poi_pid"));

				poiParent.setTenantFlag(resultSet.getInt("tenant_flag"));
				
				poiParent.setMemo (resultSet.getString("memo"));
				
				poiParent.setRowId(resultSet.getString("row_id"));
				
				// 获取IX_POI_PARENT对应的关联数据
				// ix_poi_children
				List<IRow> poiChildrens = new IxPoiChildrenSelector(conn).loadRowsByParentId(poiParent.getPid(), isLock);
		
				poiParent.setPoiChildrens(poiChildrens);

				for (IRow row : poiParent.getPoiChildrens()) {
					IxPoiChildren children = (IxPoiChildren) row;

					poiParent.poiChildrenMap.put(children.rowId(), children);
				}

				return poiParent;
			} else {

				throw new Exception("对应IX_POI_PARENT不存在!");
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
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from ix_poi_parent where parent_poi_pid=:1 and u_record!=:2";

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

				poiParent.setPid(resultSet.getInt("group_id"));

				poiParent.setParentPoiPid(resultSet.getInt("parent_poi_pid"));

				poiParent.setTenantFlag(resultSet.getInt("tenant_flag"));
				
				poiParent.setMemo (resultSet.getString("memo"));
				
				poiParent.setRowId(resultSet.getString("row_id"));
				
				// 获取IX_POI_PARENT对应的关联数据
				// ix_poi_children
				List<IRow> poiChildrens = new IxPoiChildrenSelector(conn).loadRowsByParentId(poiParent.getPid(), isLock);
		
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

}
