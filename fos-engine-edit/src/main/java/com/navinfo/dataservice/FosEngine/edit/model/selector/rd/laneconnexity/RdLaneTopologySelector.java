package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.laneconnexity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.comm.exception.DataNotFoundException;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneVia;

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
}
