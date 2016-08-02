package com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class RdLaneTopologySelector extends AbstractSelector {

	private Connection conn;

	public RdLaneTopologySelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdLaneTopology.class);
	}
	
	public List<RdLaneTopology> loadToposByLinkPid(int linkPid,
			boolean isLock) throws Exception {
		List<RdLaneTopology> rows = new ArrayList<RdLaneTopology>();
		
		String sql = "select a.*, b.node_pid out_node_pid from rd_lane_topology a, rd_cross_node b,rd_link e  where a.out_link_pid = :1    and a.u_record != 2    and a.relationship_type = 1    and exists (select null           from rd_lane_connexity c, rd_cross_node d          where a.connexity_pid = c.pid            and c.node_pid = d.node_pid            and d.pid = b.pid            and c.in_link_pid = e.link_pid            )    and exists  (select null           from rd_link e          where a.out_link_pid = e.link_pid            and b.node_pid in (e.s_node_pid, e.e_node_pid)) union all select a.*, d.node_pid out_node_pid,e.mesh_id   from rd_lane_topology a, rd_lane_connexity d,rd_link e  where a.relationship_type = 2    and a.u_record != 2    and a.out_link_pid = :2    and not exists  (select null from rd_lane_via c where a.topology_id = c.topology_id)    and a.connexity_pid = d.pid    and d.in_link_pid = e.link_pid union all select a.*,        case          when b.s_node_pid in (e.s_node_pid, e.e_node_pid) then           b.s_node_pid          else           b.e_node_pid        end out_node_pid,g.mesh_id   from rd_lane_topology a, rd_lane_via c, rd_link b, rd_link e,rd_lane_connexity f,rd_link g  where a.relationship_type = 2    and a.u_record != 2    and a.out_link_pid = :3    and a.topology_id = c.topology_id    and a.out_link_pid = b.link_pid    and c.link_pid = e.link_pid    and (b.s_node_pid in (e.s_node_pid, e.e_node_pid) or        b.e_node_pid in (e.s_node_pid, e.e_node_pid))        and a.connexity_pid = f.pid        and f.in_link_pid = g.link_pid ";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			pstmt.setInt(2, linkPid);

			pstmt.setInt(3, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdLaneTopology topo = new RdLaneTopology();
				
				ReflectionAttrUtils.executeResultSet(topo, resultSet);
				
				RdLaneViaSelector viaSelector = new RdLaneViaSelector(conn);
				
				topo.setVias(viaSelector.loadRowsByParentId(topo.getPid(), isLock));
				
				rows.add(topo);;
				
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
