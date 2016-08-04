package com.navinfo.dataservice.dao.glm.selector.ad.geo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;

import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class AdFaceSelector extends AbstractSelector {

	private Connection conn;

	public AdFaceSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(AdFace.class);
	}

	public List<AdFace> loadAdFaceByLinkGeometry(String wkt, boolean isLock)
			throws Exception {

		List<AdFace> faces = new ArrayList<AdFace>();

		String sql = "select  a.*  from ad_face a where a.u_record != 2   and sdo_within_distance(a.geometry,  sdo_geom.sdo_mbr(sdo_geometry(:1, 8307)), 'DISTANCE=0') = 'TRUE'";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				AdFace face = new AdFace();
				ReflectionAttrUtils.executeResultSet(face, resultSet);
				this.setChildData(face, isLock);
				faces.add(face);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return faces;
	}

	public List<AdFace> loadAdFaceByLinkId(int linkPid, boolean isLock)
			throws Exception {

		List<AdFace> faces = new ArrayList<AdFace>();

		StringBuilder bf = new StringBuilder();
		bf.append("select b.* from ad_face b where b.face_pid in (select a.face_pid ");
		bf.append("  FROM ad_face a, ad_face_topo t");
		bf.append(" WHERE     a.u_record != 2  AND T.U_RECORD != 2");
		bf.append("  AND a.face_pid = t.face_pid");
		bf.append(" AND t.link_pid = :1 group by a.face_pid)");

		if (isLock) {
			bf.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(bf.toString());

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				AdFace face = new AdFace();
				ReflectionAttrUtils.executeResultSet(face, resultSet);
				this.setChildData(face, isLock);
				faces.add(face);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return faces;
	}

	public List<AdFace> loadAdFaceByNodeId(int nodePid, boolean isLock)
			throws Exception {

		List<AdFace> faces = new ArrayList<AdFace>();
		StringBuilder builder = new StringBuilder();
		builder.append(" SELECT b.* from ad_face b where b.face_pid in (select a.face_pid ");
		builder.append(" FROM ad_face a, ad_face_topo t, ad_link l ");
		builder.append(" WHERE a.u_record != 2 and t.u_record != 2 and l.u_record != 2");
		builder.append(" AND a.face_pid = t.face_pid");
		builder.append(" AND t.link_pid = l.link_pid");
		builder.append(" AND (l.s_node_pid = :1 OR l.e_node_pid = :2) group by a.face_pid)");

		if (isLock) {
			builder.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(builder.toString());

			pstmt.setInt(1, nodePid);

			pstmt.setInt(2, nodePid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				AdFace face = new AdFace();
				ReflectionAttrUtils.executeResultSet(face, resultSet);
				this.setChildData(face, isLock);
				faces.add(face);

			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);

		}

		return faces;
	}

	private void setChildData(AdFace face, boolean isLock) throws Exception {
		// ad_face_topo
		List<IRow> adFaceTopo = new AbstractSelector(AdFaceTopo.class, conn)
				.loadRowsByParentId(face.getPid(), isLock);

		for (IRow row : adFaceTopo) {
			row.setMesh(face.mesh());
		}

		face.setFaceTopos(adFaceTopo);

		for (IRow row : adFaceTopo) {
			AdFaceTopo obj = (AdFaceTopo) row;

			face.adFaceTopoMap.put(obj.rowId(), obj);
		}
	}
}
