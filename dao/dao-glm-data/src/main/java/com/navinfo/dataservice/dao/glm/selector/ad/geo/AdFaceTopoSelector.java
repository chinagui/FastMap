package com.navinfo.dataservice.dao.glm.selector.ad.geo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;

public class AdFaceTopoSelector implements ISelector {

	private static Logger logger = Logger.getLogger(AdFaceSelector.class);

	private Connection conn;

	public AdFaceTopoSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		StringBuilder sb = new StringBuilder(
				"SELECT a.*,b.mesh_id FROM ad_face_topo a,ad_face b WHERE a.face_pid=:1 AND a.face_pid = b.face_pid");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		List<IRow> list = new ArrayList<IRow>();

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				AdFaceTopo adFaceTopo = new AdFaceTopo();
				
				adFaceTopo.setFacePid(resultSet.getInt("face_pid"));
				
				adFaceTopo.setLinkPid(resultSet.getInt("link_pid"));
				
				adFaceTopo.setMesh(resultSet.getInt("mesh_id"));
				
				adFaceTopo.setRowId(resultSet.getString("row_id"));
				
				adFaceTopo.setSeqNum(resultSet.getInt("seq_num"));
				
				list.add(adFaceTopo);
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

		return list;
	}

}
