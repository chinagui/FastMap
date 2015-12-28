package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.laneconnexity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.restrict.RdRestrictionConditionSelector;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.restrict.RdRestrictionViaSelector;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;

public class RdLaneTopologySelector implements ISelector {

	private static Logger logger = Logger
			.getLogger(RdLaneTopologySelector.class);

	private Connection conn;

	public RdLaneTopologySelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		RdLaneTopology topo = new RdLaneTopology();

		String sql = "select * from " + topo.tableName()
				+ " where topology_id=:1 and u_record!=2";

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

				topo.setPid(resultSet.getInt("topology_id"));
				
				topo.setConnexityPid(resultSet.getInt("connexity_pid"));

				topo.setOutLinkPid(resultSet.getInt("out_link_pid"));
				
				topo.setInLaneInfo(resultSet.getInt("in_lane_info"));
				
				topo.setBusLaneInfo(resultSet.getInt("bus_lane_info"));
				
				topo.setReachDir(resultSet.getInt("reach_dir"));
				
				topo.setRelationshipType(resultSet.getInt("relationship_type"));

				RdLaneViaSelector viaSelector = new RdLaneViaSelector(
						conn);

				topo.setVias(viaSelector.loadRowsByParentId(id, isLock));
				
				for(IRow row : topo.getVias()){
					RdLaneVia via = (RdLaneVia)row;
					
					topo.viaMap.put(via.getRowId(), via);
				}

			} else {
				
				throw new DataNotFoundException(null);
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

		return topo;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from rd_lane_topology where connexity_id=:1 and u_record!=:2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdLaneTopology topo = new RdLaneTopology();

				topo.setPid(resultSet.getInt("topology_id"));
				
				topo.setConnexityPid(resultSet.getInt("connexity_pid"));

				topo.setOutLinkPid(resultSet.getInt("out_link_pid"));
				
				topo.setInLaneInfo(resultSet.getInt("in_lane_info"));
				
				topo.setBusLaneInfo(resultSet.getInt("bus_lane_info"));
				
				topo.setReachDir(resultSet.getInt("reach_dir"));
				
				topo.setRelationshipType(resultSet.getInt("relationship_type"));

				RdLaneViaSelector viaSelector = new RdLaneViaSelector(
						conn);

				topo.setVias(viaSelector.loadRowsByParentId(id, isLock));
				
				for(IRow row : topo.getVias()){
					RdLaneVia via = (RdLaneVia)row;
					
					topo.viaMap.put(via.getRowId(), via);
				}
				
				rows.add(topo);
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

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		return null;
	}
	
	
	public List<RdLaneTopology> loadToposByLinkPid(int linkPid,
			boolean isLock) throws Exception {
		List<RdLaneTopology> rows = new ArrayList<RdLaneTopology>();

		String sql = "select a.*, b.node_pid out_node_pid   from " +
				"rd_lane_topology a, rd_cross_node b  " +
				"where a.out_link_pid = :1    and a.u_record != 2    and a.relationship_type = 1    " +
				"and exists (select null           from rd_lane_connexity c, rd_cross_node d          where a.connexity_pid = c.pid            and c.node_pid = d.node_pid            and d.pid = b.pid)    and exists  (select null           from rd_link e          where a.out_link_pid = e.link_pid            and b.node_pid in (e.s_node_pid, e.e_node_pid)) " +
				"union all select a.*, d.node_pid out_node_pid   from rd_lane_topology a, rd_lane_connexity d  " +
				"where a.relationship_type = 2    and a.u_record != 2    and a.out_link_pid = :2    and not exists (select null           from rd_lane_via c          where a.topology_id = c.topology_id)    and a.connexity_pid = d.pid union all select a.*,        case          when b.s_node_pid in (e.s_node_pid, e.e_node_pid) then           b.s_node_pid          else           b.e_node_pid        end out_node_pid   from rd_lane_topology a, rd_lane_via c, rd_link b, rd_link e  where a.relationship_type = 2    and a.u_record != 2    and a.out_link_pid = :3    and a.topology_id = c.topology_id    and a.out_link_pid = b.link_pid    and c.link_pid = e.link_pid    and" +
				" (b.s_node_pid in (e.s_node_pid, e.e_node_pid) or        b.e_node_pid in (e.s_node_pid, e.e_node_pid)) ";
		
		if (isLock){
			sql += " for update nowait ";
		}
		
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
				
				topo.setRowId(resultSet.getString("row_id"));
				
				topo.setPid(resultSet.getInt("pid"));
				
				topo.setConnexityPid(resultSet.getInt("connexity_pid"));
				
				topo.setOutLinkPid(resultSet.getInt("out_link_pid"));
				
				topo.setInLaneInfo(resultSet.getInt("in_lane_info"));
				
				topo.setBusLaneInfo(resultSet.getInt("bus_lane_info"));
				
				topo.setReachDir(resultSet.getInt("reach_dir"));
				
				topo.setRelationshipType(resultSet.getInt("relationshit_type"));
				
				topo.isetOutNodePid(resultSet.getInt("out_node_pid"));
				
				RdLaneViaSelector viaSelector = new RdLaneViaSelector(conn);
				
				topo.setVias(viaSelector.loadRowsByParentId(topo.getPid(), isLock));
				
				rows.add(topo);;
				
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
}
