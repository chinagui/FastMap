package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;

/**
 * POI父子关系子表 查询
 * @author luyao
 *
 */
public class IxPoiChildrenSelector implements ISelector {

	private static Logger logger = Logger.getLogger(IxPoiChildrenSelector.class);

	private Connection conn;
	
	public IxPoiChildrenSelector(Connection conn) {
		super();
		this.conn = conn;
	}
	
	
	
	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		
		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		
		IxPoiChildren poiChildren = new IxPoiChildren();

		String sql = "select * from " + poiChildren.tableName() + " where row_id=hextoraw(:1)";

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

				poiChildren.setGroupId (resultSet.getInt("group_id"));

				poiChildren.setChildPoiPid(resultSet.getInt("child_poi_pid"));
				
				poiChildren.setRelationType(resultSet.getInt("relation_type"));

				poiChildren.setRowId(resultSet.getString("row_id"));
			} else {
				
				throw new DataNotFoundException("数据不存在");
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

		return poiChildren;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from ix_poi_children where group_id=:1 and u_record!=:2";

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

				IxPoiChildren poiChildren = new IxPoiChildren();
				
				poiChildren.setGroupId (resultSet.getInt("group_id"));

				poiChildren.setChildPoiPid(resultSet.getInt("child_poi_pid"));
				
				poiChildren.setRelationType(resultSet.getInt("relation_type"));

				poiChildren.setRowId(resultSet.getString("row_id"));

				rows.add(poiChildren);
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
