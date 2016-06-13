package com.navinfo.dataservice.dao.glm.selector.rd.rw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNodeMesh;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

public class RwNodeSelector implements ISelector {
	
	private Connection conn;

	public RwNodeSelector(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		RwNode rwNode = new RwNode();

		StringBuilder sb = new StringBuilder(
				"select * from " + rwNode.tableName() + " where node_pid = :1 and u_record !=2");

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
				
				rwNode.setPid(id);
				
				rwNode.setKind(resultSet.getInt("kind"));
				
				rwNode.setForm(resultSet.getInt("form"));
				
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
				
				rwNode.setGeometry(geometry);
				
				rwNode.setEditFlag(resultSet.getInt("edit_flag"));
				
				rwNode.setRowId(resultSet.getString("row_id"));
				
				List<IRow> meshs = new RwNodeMeshSelector(conn).loadRowsByParentId(id, isLock);

				rwNode.setMeshs(meshs);

				for (IRow row : rwNode.getMeshs()) {
					RwNodeMesh obj = (RwNodeMesh) row;

					rwNode.meshMap.put(obj.rowId(), obj);
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
		
		return rwNode;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
