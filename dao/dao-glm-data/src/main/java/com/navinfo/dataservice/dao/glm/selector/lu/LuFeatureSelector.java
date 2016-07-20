package com.navinfo.dataservice.dao.glm.selector.lu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuFeature;

public class LuFeatureSelector implements ISelector {

	private Logger logger = Logger.getLogger(LuFeatureSelector.class);

	private Connection conn;

	public LuFeatureSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		LuFeature feature = new LuFeature();

		StringBuilder sb = new StringBuilder("select * from "
				+ feature.tableName()
				+ " WHERE feature_pid = :1 and  u_record !=2");

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
				setAttr(feature, resultSet, isLock);

				return feature;
			} else {

				throw new Exception("对应LU_FEATURE不存在!");
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

	private void setAttr(LuFeature feature, ResultSet resultSet, boolean isLock)
			throws SQLException, Exception {
		feature.setPid(resultSet.getInt("feature_pid"));
		feature.setRowId(resultSet.getString("row_id"));

		List<IRow> faces = new LuFaceSelector(this.conn).loadRowsByParentId(
				feature.pid(), isLock);
		feature.setFaces(faces);

		for (IRow row : faces) {
			LuFace face = (LuFace) row;
			feature.faceMap.put(face.rowId(), face);
		}
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		return null;
	}

}
