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
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceName;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

public class LuFaceNameSelector implements ISelector {

	private static Logger logger = Logger.getLogger(LuFaceNameSelector.class);

	private Connection conn;

	public LuFaceNameSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		LuFaceName faceName = new LuFaceName();

		StringBuilder sb = new StringBuilder(
				 "select * from " + faceName.tableName() + " WHERE name_id = :1 and  u_record !=2");

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
//				setAttr(faceName, resultSet);
				ReflectionAttrUtils.executeResultSet(faceName, resultSet);
				return faceName;
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

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		LuFaceName luFaceName = new LuFaceName();

		String sql = "SELECT a.*,b.mesh_id FROM lu_face_name a,lu_face b WHERE a.row_id=hextoraw(:1) AND a.face_pid = b.face_pid and a.u_record !=2 ";

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

//				setAttr(luFaceName, resultSet);
				ReflectionAttrUtils.executeResultSet(luFaceName, resultSet);
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

		return luFaceName;
	}

	private void setAttr(LuFaceName luFaceName, ResultSet resultSet)
			throws SQLException {
		luFaceName.setNameId(resultSet.getInt("name_id"));
		luFaceName.setFacePid(resultSet.getInt("face_pid"));
		luFaceName.setNameGroupid(resultSet.getInt("name_groupid"));
		luFaceName.setLangCode(resultSet.getString("lang_code"));
		luFaceName.setName(resultSet.getString("name"));
		luFaceName.setPhonetic(resultSet.getString("phonetic"));
		luFaceName.setSrcFlag(resultSet.getInt("src_flag"));
		luFaceName.setMesh(resultSet.getInt("mesh_id"));
		luFaceName.setRowId(resultSet.getString("row_id"));
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		StringBuilder sb = new StringBuilder(
				"SELECT a.*,b.mesh_id FROM lu_face_name a,lu_face b WHERE a.face_pid=:1 AND a.face_pid = b.face_pid and  a.u_record !=2");

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
				LuFaceName luFaceName = new LuFaceName();

//				this.setAttr(luFaceName, resultSet);
				ReflectionAttrUtils.executeResultSet(luFaceName, resultSet);

				list.add(luFaceName);
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
