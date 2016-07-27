package com.navinfo.dataservice.dao.glm.selector.lu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.lu.LuNodeMesh;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

public class LuNodeMeshSelector implements ISelector {

	private Logger logger = Logger.getLogger(LuNodeMeshSelector.class);

	private Connection conn;

	public LuNodeMeshSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		LuNodeMesh mesh = new LuNodeMesh();

		String sql = "select * from " + mesh.tableName()
				+ " where row_id=hextoraw(:1)";

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
//				setAttr(mesh, resultSet);
				ReflectionAttrUtils.executeResultSet(mesh, resultSet);
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

		return mesh;
	}

	private void setAttr(LuNodeMesh mesh, ResultSet resultSet)
			throws SQLException {
		mesh.setNodePid(resultSet.getInt("node_pid"));

		mesh.setMeshId(resultSet.getInt("mesh_id"));

		mesh.setRowId(resultSet.getString("row_id"));
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from " + new LuNodeMesh().tableName()
				+ " where node_pid =:1 and u_record!=:2";

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

				LuNodeMesh mesh = new LuNodeMesh();

//				setAttr(mesh, resultSet);
				ReflectionAttrUtils.executeResultSet(mesh, resultSet);

				rows.add(mesh);
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
