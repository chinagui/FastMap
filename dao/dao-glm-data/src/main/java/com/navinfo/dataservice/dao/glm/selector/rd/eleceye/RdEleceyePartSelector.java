package com.navinfo.dataservice.dao.glm.selector.rd.eleceye;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePart;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

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
public class RdEleceyePartSelector extends AbstractSelector {

	private Connection conn;

	public RdEleceyePartSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdEleceyePart.class);
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

				ReflectionAttrUtils.executeResultSet(part, resultSet);

				rows.add(part);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
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

				ReflectionAttrUtils.executeResultSet(part, resultSet);

				rows.add(part);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return rows;
	}
}
