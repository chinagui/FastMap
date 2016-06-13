package com.navinfo.dataservice.dao.glm.selector.rd.rw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwFeature;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;

public class RwFeatureSelector implements ISelector {

	private Connection conn;

	public RwFeatureSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		RwFeature rwFeature = new RwFeature();

		StringBuilder sb = new StringBuilder(
				"select * from " + rwFeature.tableName() + " where feature_pid = :1 and u_record !=2");

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
				rwFeature.setPid(id);
				
				rwFeature.setRowId(resultSet.getString("row_id"));
				
				List<IRow> links = new RwLinkSelector(conn).loadRowsByParentId(id, isLock);

				rwFeature.setRwLinks(links);

				for (IRow row : rwFeature.getRwLinks()) {
					RwLink obj = (RwLink) row;

					rwFeature.rwLinkMap.put(obj.rowId(), obj);
				}
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

		return rwFeature;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		return null;
	}

}
