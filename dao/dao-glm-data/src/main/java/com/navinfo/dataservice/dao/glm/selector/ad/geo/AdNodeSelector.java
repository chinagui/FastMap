package com.navinfo.dataservice.dao.glm.selector.ad.geo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNodeMesh;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

public class AdNodeSelector implements ISelector {

	private static Logger logger = Logger.getLogger(AdFaceSelector.class);

	private Connection conn;

	public AdNodeSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		AdNode adNode = new AdNode();

		StringBuilder sb = new StringBuilder(
				"SELECT a.*,b.mesh_id FROM ad_node a,ad_node_mesh b WHERE a.node_pid = b.node_pid AND a.node_pid = :1");

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
				adNode.setPid(id);

				adNode.setKind(resultSet.getInt("kind"));

				adNode.setForm(resultSet.getInt("form"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);

				adNode.setGeometry(geometry);

				adNode.setEditFlag(resultSet.getInt("edit_flag"));

				// 获取AD_Node对应的关联数据

				// ad_node_mesh
				List<IRow> forms = new AdNodeMeshSelector(conn).loadRowsByParentId(id, isLock);

				// loadRowsByParentId已经查询了mesh,是否可以不做设置
				for (IRow row : forms) {
					row.setMesh(adNode.getMeshId());
				}

				adNode.setMeshes(forms);

				for (IRow row : adNode.getMeshes()) {
					AdNodeMesh mesh = (AdNodeMesh) row;

					adNode.meshMap.put(mesh.rowId(), mesh);
				}

				return adNode;
			} else {

				throw new Exception("对应AD_LINK不存在!");
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
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		return null;
	}

}
