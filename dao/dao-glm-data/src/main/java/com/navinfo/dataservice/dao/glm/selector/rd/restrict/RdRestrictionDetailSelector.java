package com.navinfo.dataservice.dao.glm.selector.rd.restrict;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;

import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class RdRestrictionDetailSelector extends AbstractSelector {

	private Connection conn;

	public RdRestrictionDetailSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdRestrictionDetail.class);
	}

	public List<RdRestrictionDetail> loadDetailsByLinkPid(int linkPid,
			boolean isLock) throws Exception {
		List<RdRestrictionDetail> rows = new ArrayList<RdRestrictionDetail>();
		String sql = "SELECT a.*, b.node_pid out_node_pid FROM rd_restriction_detail a, rd_cross_node b,rd_link f WHERE a.out_link_pid = :1 AND a.u_record != 2 AND a.relationship_type = 1 AND EXISTS (SELECT NULL FROM rd_restriction C, rd_cross_node d WHERE a.restric_pid = C.pid AND C.node_pid = d.node_pid AND d.pid = b.pid AND C.in_link_pid = f.link_pid ) AND EXISTS (SELECT NULL FROM rd_link e WHERE a.out_link_pid = e.link_pid AND b.node_pid IN (e.s_node_pid, e.e_node_pid)) UNION ALL SELECT a.*, d.node_pid out_node_pid FROM rd_restriction_detail a, rd_restriction d,rd_link f WHERE a.relationship_type = 2 AND a.u_record != 2 AND a.out_link_pid = :2 AND NOT EXISTS (SELECT NULL FROM rd_restriction_via C WHERE a.detail_id = C.detail_id) AND a.restric_pid = d.pid AND d.in_link_pid = f.link_pid UNION ALL SELECT a.*, CASE WHEN b.s_node_pid IN (e.s_node_pid, e.e_node_pid) THEN b. s_node_pid ELSE b.e_node_pid END out_node_pid FROM rd_restriction_detail a, rd_restriction_via C, rd_link b, rd_link e, rd_restriction f,rd_link G WHERE a.relationship_type = 2 AND a.u_record != 2 AND a.out_link_pid = :3 AND a.detail_id = C.detail_id AND a.out_link_pid = b.link_pid AND C.link_pid = e.link_pid AND (b.s_node_pid IN (e.s_node_pid, e.e_node_pid) OR b.e_node_pid IN (e.s_node_pid, e.e_node_pid)) AND a.restric_pid = f.pid AND f.in_link_pid = G.link_pid";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			pstmt.setInt(2, linkPid);

			pstmt.setInt(3, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdRestrictionDetail detail = new RdRestrictionDetail();

				ReflectionAttrUtils.executeResultSet(detail, resultSet);

				RdRestrictionViaSelector via = new RdRestrictionViaSelector(
						conn);
				detail.setVias(via.loadRowsByParentId(detail.getPid(), isLock));

				detail.setConditions(new AbstractSelector(
						RdRestrictionCondition.class, conn).loadRowsByParentId(
						detail.getPid(), isLock));

				rows.add(detail);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return rows;
	}
}
