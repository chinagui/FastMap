package com.navinfo.dataservice.dao.glm.selector.lu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import oracle.sql.STRUCT;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceName;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceTopo;

public class LuFaceSelector implements ISelector {

	private Logger logger = Logger.getLogger(LuFaceSelector.class);

	private Connection conn;

	public LuFaceSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		LuFace face = new LuFace();

		StringBuilder sb = new StringBuilder("select * from "
				+ face.tableName() + " WHERE face_pid = :1 and  u_record !=2");

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
				setAttr(isLock, face, resultSet);

				return face;
			} else {

				throw new Exception("对应LU_FACE不存在!");
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

	private void setAttr(boolean isLock, LuFace face, ResultSet resultSet)
			throws SQLException, Exception {
		face.setPid(resultSet.getInt("face_pid"));

		face.setFeaturePid(resultSet.getInt("feature_pid"));

		STRUCT struct = (STRUCT) resultSet.getObject("geometry");

		face.setGeometry(GeoTranslator.struct2Jts(struct, 100000, 0));

		face.setKind(resultSet.getInt("kind"));

		face.setPerimeter(resultSet.getDouble("perimeter"));

		face.setMeshId(resultSet.getInt("mesh_id"));

		face.setEditFlag(resultSet.getInt("edit_flag"));

		face.setDetailFlag(resultSet.getInt("detail_flag"));

		face.setRowId(resultSet.getString("row_id"));

		List<IRow> luFaceTopo = new LuFaceTopoSelector(conn)
				.loadRowsByParentId(face.pid(), isLock);

		for (IRow row : luFaceTopo) {
			row.setMesh(face.mesh());
		}

		face.setFaceTopos(luFaceTopo);

		for (IRow row : luFaceTopo) {
			LuFaceTopo obj = (LuFaceTopo) row;

			face.luFaceTopoMap.put(obj.rowId(), obj);
		}

		List<IRow> luFaceNames = new LuFaceNameSelector(conn)
				.loadRowsByParentId(face.getPid(), isLock);

		for (IRow row : luFaceNames) {
			LuFaceName obj = (LuFaceName) row;

			face.luFaceNameMap.put(obj.rowId(), obj);
		}
		face.setFaceNames(luFaceNames);

	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		List<IRow> faces = new ArrayList<IRow>();

		String sql = "select  a.*  from lu_face a where a.u_record != 2  and a.feature_pid = :1 ";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				LuFace face = new LuFace();

				this.setAttr(isLock, face, resultSet);

				faces.add(face);

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

		return faces;
	}

	public List<LuFace> loadLuFaceByLinkId(int linkPid, boolean isLock)
			throws Exception {

		List<LuFace> faces = new ArrayList<LuFace>();

		String sql = "select  a.*  from lu_face a ,lu_face_topo t where a.u_record != 2  and a.face_pid = t.face_pid and t.link_pid = :1 ";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				LuFace face = new LuFace();

				this.setAttr(isLock, face, resultSet);

				faces.add(face);

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

		return faces;
	}

	public List<LuFace> loadLuFaceByNodeId(int nodePid, boolean isLock)
			throws Exception {

		List<LuFace> faces = new ArrayList<LuFace>();

		String sql = "select  a.*  from lu_face a ,lu_face_topo t,lu_link l,lu_node n where a.u_record != 2  and a.face_pid = t.face_pid and t.link_pid = l.link_pid and (l.s_node_pid = :1 or l.e_node_pid = :2) ";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, nodePid);
			
			pstmt.setInt(2, nodePid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				LuFace face = new LuFace();

				this.setAttr(isLock, face, resultSet);

				faces.add(face);
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

		return faces;
	}

}
