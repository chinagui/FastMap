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
		String sql = "select a.*, b.node_pid out_node_pid   from rd_restriction_detail a, rd_cross_node b,rd_link f  where a.out_link_pid = :1    and a.u_record != 2    and a.relationship_type = 1    and exists (select null           from rd_restriction c, rd_cross_node d          where a.restric_pid = c.pid            and c.node_pid = d.node_pid            and d.pid = b.pid            and c.in_link_pid = f.link_pid            )    and exists  (select null           from rd_link e          where a.out_link_pid = e.link_pid            and b.node_pid in (e.s_node_pid, e.e_node_pid)) union all select a.*, d.node_pid out_node_pid,f.mesh_id   from rd_restriction_detail a, rd_restriction d,rd_link f  where a.relationship_type = 2    and a.u_record != 2    and a.out_link_pid = :2    and not exists (select null           from rd_restriction_via c          where a.detail_id = c.detail_id)    and a.restric_pid = d.pid    and d.in_link_pid = f.link_pid union all select a.*,        case          when b.s_node_pid in (e.s_node_pid, e.e_node_pid) then           b.s_node_pid          else           b.e_node_pid        end out_node_pid,g.mesh_id   from rd_restriction_detail a, rd_restriction_via c, rd_link b, rd_link e,rd_restriction f,rd_link g  where a.relationship_type = 2    and a.u_record != 2    and a.out_link_pid = :3    and a.detail_id = c.detail_id    and a.out_link_pid = b.link_pid    and c.link_pid = e.link_pid    and (b.s_node_pid in (e.s_node_pid, e.e_node_pid) or        b.e_node_pid in (e.s_node_pid, e.e_node_pid))        and a.restric_pid = f.pid        and f.in_link_pid = g.link_pid";

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
