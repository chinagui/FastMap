package com.navinfo.dataservice.dao.glm.selector.rd.eleceye;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePart;

/**
 * @Title: RdEleceyePartSelector.java
 * @Prject: dao-glm-data
 * @Package: com.navinfo.dataservice.dao.glm.selector.rd.eleceye
 * @Description: 查询区间测速电子眼组成
 * @author zhangyt
 * @date: 2016年7月20日 下午5:45:47
 * @version: v1.0
 *
 */
public class RdEleceyePartSelector implements ISelector {

	private Connection conn;

	public RdEleceyePartSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		return null;
	}
	
	/**
	 * 根据RdEleceyePart的RowId查询
	 */
	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		RdEleceyePart part = new RdEleceyePart();

		String sql = "select a.*,c.mesh_id from " + part.tableName()
				+ " a, rd_electroniceye b, rd_link c where a.row_id = :1 and a.u_record != 2 and a.eleceye_pid = b.eleceye_pid and b.link_pid = c.link_pid";

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

				setAttr(part, resultSet);

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

		return part;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		return null;
	}
	
	/**
	 * 根据RdEleceyePart的GroupId查询
	 */
	public List<IRow> loadRowsByGroupId(int groupId, boolean isLock) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select a.*,c.mesh_id from rd_eleceye_part a, rd_electroniceye b, rd_link c where a.group_id = :1 and a.u_record != 2 and a.eleceye_pid = b.pid and b.link_pid = c.link_pid";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, groupId);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdEleceyePart part = new RdEleceyePart();

				setAttr(part, resultSet);

				rows.add(part);
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
	
	/**
	 * 根据RdEleceyePart的EleceyePid查询
	 */
	public List<IRow> loadRowsByEleceyePid(int eleceyePid, boolean isLock) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select a.*,c.mesh_id from rd_eleceye_part a, rd_electroniceye b, rd_link c where a.eleceye_pid = :1 and a.u_record != 2 and a.eleceye_pid = b.pid and b.link_pid = c.link_pid";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, eleceyePid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdEleceyePart part = new RdEleceyePart();

				setAttr(part, resultSet);

				rows.add(part);
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

	private void setAttr(RdEleceyePart part, ResultSet resultSet) throws SQLException {
		part.setGroupId(resultSet.getInt("group_id"));
		part.setEleceyePid(resultSet.getInt("eleceye_pid"));
		part.setMesh(resultSet.getInt("mesh_id"));
		part.setRowId(resultSet.getString("row_id"));
	}
}
