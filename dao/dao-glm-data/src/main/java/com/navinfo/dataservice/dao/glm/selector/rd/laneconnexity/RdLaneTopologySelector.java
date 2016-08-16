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
		
		String sql = "SELECT a.*, b.node_pid out_node_pid FROM rd_lane_topology a, rd_cross_node b,rd_link e WHERE a.out_link_pid = :1 AND a.u_record != 2 AND a.relationship_type = 1 AND EXISTS (SELECT NULL FROM rd_lane_connexity C, rd_cross_node d WHERE a.connexity_pid = C.pid AND C.node_pid = d.node_pid AND d.pid = b.pid AND C.in_link_pid = e.link_pid ) AND EXISTS (SELECT NULL FROM rd_link e WHERE a.out_link_pid = e.link_pid AND b.node_pid IN (e.s_node_pid, e.e_node_pid)) UNION ALL SELECT a.*, d.node_pid out_node_pid FROM rd_lane_topology a, rd_lane_connexity d,rd_link e WHERE a.relationship_type = 2 AND a.u_record != 2 AND a.out_link_pid = :2 AND NOT EXISTS (SELECT NULL FROM rd_lane_via C WHERE a.topology_id = C.topology_id) AND a.connexity_pid = d.pid AND d.in_link_pid = e.link_pid UNION ALL SELECT a.*, CASE WHEN b.s_node_pid IN (e.s_node_pid, e.e_node_pid) THEN b. s_node_pid ELSE b.e_node_pid END out_node_pid FROM rd_lane_topology a, rd_lane_via C, rd_link b, rd_link e,rd_lane_connexity f ,rd_link G WHERE a.relationship_type = 2 AND a.u_record != 2 AND a.out_link_pid = :3 AND a.topology_id = C.topology_id AND a.out_link_pid = b.link_pid AND C.link_pid = e.link_pid AND (b.s_node_pid IN (e.s_node_pid, e.e_node_pid) OR b.e_node_pid IN (e.s_node_pid, e.e_node_pid)) AND a.connexity_pid = f.pid AND f.in_link_pid = G.link_pid ";
		
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
