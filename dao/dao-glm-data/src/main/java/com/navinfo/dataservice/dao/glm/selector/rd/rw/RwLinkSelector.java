package com.navinfo.dataservice.dao.glm.selector.rd.rw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLinkName;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

public class RwLinkSelector implements ISelector {
	
	private Connection conn;

	public RwLinkSelector(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		RwLink rwLink = new RwLink();

		StringBuilder sb = new StringBuilder(
				"select * from " + rwLink.tableName() + " where link_pid = :1 and u_record !=2");

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
				
				rwLink.setPid(id);
				
				rwLink.setFeaturePid(resultSet.getInt("feature_pid"));
				
				rwLink.setsNodePid(resultSet.getInt("s_node_pid"));
				
				rwLink.seteNodePid(resultSet.getInt("e_node_pid"));
				
				rwLink.setKind(resultSet.getInt("kind"));
				
				rwLink.setForm(resultSet.getInt("form"));
				
				rwLink.setLength(resultSet.getDouble("length"));
				
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
				
				rwLink.setGeometry(geometry);
				
				rwLink.setMeshId(resultSet.getInt("mesh_id"));
				
				rwLink.setScale(resultSet.getInt("scale"));
				
				rwLink.setDetailFlag(resultSet.getInt("detail_flag"));
				
				rwLink.setEditFlag(resultSet.getInt("edit_flag"));
				
				rwLink.setColor(resultSet.getString("color"));
				
				rwLink.setRowId(resultSet.getString("row_id"));
				
				List<IRow> nodes = new RwNodeSelector(conn).loadRowsByParentId(id, isLock);

				rwLink.setRwNodes(nodes);

				for (IRow row : rwLink.getRwNodes()) {
					RwNode obj = (RwNode) row;

					rwLink.nodeMap.put(obj.rowId(), obj);
				}
				
				List<IRow> names = new RwLinkNameSelector(conn).loadRowsByParentId(id, isLock);

				rwLink.setRwLinkNames(names);

				for (IRow row : rwLink.getRwLinkNames()) {
					RwLinkName obj = (RwLinkName) row;

					rwLink.linkNameMap.put(obj.rowId(), obj);
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

		return rwLink;
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
