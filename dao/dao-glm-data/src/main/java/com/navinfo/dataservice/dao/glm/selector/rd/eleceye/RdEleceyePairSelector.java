package com.navinfo.dataservice.dao.glm.selector.rd.eleceye;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePair;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePart;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

/**
 * @Title: RdEleceyePairSelector.java
 * @Prject: dao-glm-data
 * @Package: com.navinfo.dataservice.dao.glm.selector.rd.eleceye
 * @Description: 查询区间测速电子眼
 * @author zhangyt
 * @date: 2016年7月20日 下午5:45:28
 * @version: v1.0
 *
 */
public class RdEleceyePairSelector implements ISelector {

	private Connection conn;

	public RdEleceyePairSelector(Connection conn) {
		this.conn = conn;
	}

	/**
	 * 根据RdEleceyePair的GroupId查询
	 */
	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		RdEleceyePair pair = new RdEleceyePair();

		// String sql = "select a.group_id,a.u_record,a.u_fields,a.u_date,
		// a.row_id,c.mesh_id from "
		// + pair.tableName()
		// + " a,rd_eleceye_part b,rd_electroniceye c, rd_link d where
		// a.group_id = :1 and a.u_record != 2 and a.group_id = b.group_id and
		// b.eleceye_pid = c.pid and c.link_pid = d.link_pid";

		String sql = "select a.group_id,a.u_record,a.u_fields,a.u_date, a.row_id from " + pair.tableName() + " a where a.group_id = :1 and a.u_record != 2" ;
		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				// setAttr(pair, resultSet);
				ReflectionAttrUtils.executeResultSet(pair, resultSet);

			} else {

				throw new DataNotFoundException("数据不存在");
			}

			List<IRow> parts = new RdEleceyePartSelector(conn).loadRowsByGroupId(pair.pid(), isLock);
			for (IRow row : parts) {
				RdEleceyePart part = (RdEleceyePart) row;
				pair.partMap.put(part.rowId(), part);
			}
			pair.setParts(parts);

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

		return pair;
	}

	/**
	 * 根据RdEleceyePair的RowId查询
	 */
	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		RdEleceyePair pair = new RdEleceyePair();

		String sql = "select a.*,c.mesh_id from " + pair.tableName()
				+ " a,rd_eleceye_part b,rd_electroniceye c, rd_link d where a.row_id = :1 and a.u_record != 2 and a.group_id = b.group_id and b.eleceye_pid = c.pid and c.link_pid = d.link_pid";

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

				// setAttr(pair, resultSet);
				ReflectionAttrUtils.executeResultSet(pair, resultSet);

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

		return pair;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		return null;
	}

	private void setAttr(RdEleceyePair pair, ResultSet resultSet) throws SQLException {
		pair.setPid(resultSet.getInt("group_id"));
		pair.setMesh(resultSet.getInt("mesh_id"));
		pair.setRowId(resultSet.getString("row_id"));
	}
}
