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
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceTopo;

public class LuFaceTopoSelector implements ISelector {

	private static Logger logger = Logger.getLogger(LuFaceTopoSelector.class);

	private Connection conn;

	public LuFaceTopoSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		LuFaceTopo luFaceTopo = new LuFaceTopo();

		String sql = "SELECT a.*,b.mesh_id FROM lu_face_topo a,lu_face b WHERE a.row_id=hextoraw(:1) AND a.face_pid = b.face_pid and a.u_record !=2 ";

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

				setAttr(luFaceTopo, resultSet);

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

		return luFaceTopo;
	}

	private void setAttr(LuFaceTopo luFaceTopo, ResultSet resultSet)
			throws SQLException {
		luFaceTopo.setFacePid(resultSet.getInt("face_pid"));

		luFaceTopo.setLinkPid(resultSet.getInt("seq_num"));
		
		luFaceTopo.setLinkPid(resultSet.getInt("link_pid"));

		luFaceTopo.setMesh(resultSet.getInt("mesh_id"));

		luFaceTopo.setRowId(resultSet.getString("row_id"));
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		StringBuilder sb = new StringBuilder(
				"SELECT a.*,b.mesh_id FROM lu_face_topo a,lu_face b WHERE a.face_pid=:1 AND a.face_pid = b.face_pid and  a.u_record !=2");

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
				LuFaceTopo luFaceTopo = new LuFaceTopo();

				this.setAttr(luFaceTopo, resultSet);

				list.add(luFaceTopo);
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
	
	public List<LuFaceTopo> loadByLinkPid(Integer linkPid, boolean isLock) throws Exception {
		
		List<LuFaceTopo> luFaceTopos = new ArrayList<LuFaceTopo>();
		String sql = "SELECT a.* FROM lu_face_topo a WHERE a.link_pid =:1 and  a.u_record !=2 ";

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
				LuFaceTopo luFaceTopo = new LuFaceTopo();
				
				this.setAttr(luFaceTopo, resultSet);
				
				luFaceTopos.add(luFaceTopo);
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

		return luFaceTopos;
	}


}
