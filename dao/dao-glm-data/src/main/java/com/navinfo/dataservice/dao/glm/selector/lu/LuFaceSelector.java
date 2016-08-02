package com.navinfo.dataservice.dao.glm.selector.lu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceName;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceTopo;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

public class LuFaceSelector extends AbstractSelector {

	private Connection conn;
	
	public LuFaceSelector(Connection conn) throws Exception {
		super(LuFace.class, conn);
		this.conn = conn;
	}


	private void setChildren(boolean isLock, LuFace face, ResultSet resultSet) throws SQLException, Exception {
		List<IRow> luFaceTopo = new LuFaceTopoSelector(conn).loadRowsByParentId(face.getPid(), isLock);
		for (IRow row : luFaceTopo) {
			row.setMesh(face.mesh());
		}
		face.setFaceTopos(luFaceTopo);
		for (IRow row : luFaceTopo) {
			LuFaceTopo obj = (LuFaceTopo) row;
			face.luFaceTopoMap.put(obj.rowId(), obj);
		}

		List<IRow> luFaceNames = new LuFaceNameSelector(conn).loadRowsByParentId(face.getPid(), isLock);
		for (IRow row : luFaceNames) {
			LuFaceName obj = (LuFaceName) row;
			face.luFaceNameMap.put(obj.rowId(), obj);
		}
		face.setFaceNames(luFaceNames);

	}
	

	public List<LuFace> loadLuFaceByLinkId(int linkPid, boolean isLock) throws Exception {
		List<LuFace> faces = new ArrayList<LuFace>();
		StringBuilder bf = new StringBuilder();
		bf.append("select b.* from lu_face b where b.face_pid in (select a.face_pid ");
		bf.append("  FROM lu_face a, lu_face_topo t");
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
				LuFace face = new LuFace();
				ReflectionAttrUtils.executeResultSet(face, resultSet);
				this.setChildren(isLock, face, resultSet);
				faces.add(face);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return faces;
	}

	public List<LuFace> loadLuFaceByNodeId(int nodePid, boolean isLock) throws Exception {
		List<LuFace> faces = new ArrayList<LuFace>();
		StringBuilder builder = new StringBuilder();
		builder.append(" SELECT b.* from lu_face b where b.face_pid in (select a.face_pid ");
		builder.append(" FROM lu_face a, lu_face_topo t, lu_link l ");
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
				LuFace face = new LuFace();
				ReflectionAttrUtils.executeResultSet(face, resultSet);
				this.setChildren(isLock, face, resultSet);
				faces.add(face);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return faces;
	}

}
