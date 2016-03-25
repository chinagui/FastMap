package com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;

public class RdLaneConnexitySelector implements ISelector {

	private static Logger logger = Logger
			.getLogger(RdLaneConnexitySelector.class);

	private Connection conn;

	public RdLaneConnexitySelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		RdLaneConnexity connexity = new RdLaneConnexity();

//		String sql = "select * from " + connexity.tableName()
//				+ " where pid=:1 and u_record!=2";
		
		String sql = "select a.*,b.mesh_id from " + connexity.tableName()
				+ " a,rd_link b where a.pid=:1 and a.u_record!=2 and a.in_link_pid = b.link_pid ";

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

				connexity.setPid(resultSet.getInt("pid"));

				connexity.setInLinkPid(resultSet.getInt("in_link_pid"));

				connexity.setNodePid(resultSet.getInt("node_pid"));
				
				connexity.setLaneInfo(resultSet.getString("lane_info"));
				
				connexity.setConflictFlag(resultSet.getInt("conflict_flag"));
				
				connexity.setKgFlag(resultSet.getInt("kg_flag"));
				
				connexity.setLaneNum(resultSet.getInt("lane_num"));
				
				connexity.setLeftExtend(resultSet.getInt("left_extend"));
				
				connexity.setRightExtend(resultSet.getInt("right_extend"));
				
				connexity.setSrcFlag(resultSet.getInt("src_flag"));
				
				connexity.setRowId(resultSet.getString("row_id"));
				
				int meshId = resultSet.getInt("mesh_id");
				
				connexity.setMesh(meshId);

				RdLaneTopologySelector topoSelector = new RdLaneTopologySelector(
						conn);

				connexity.setTopos(topoSelector.loadRowsByParentId(id, isLock));
				
				for(IRow row : connexity.getTopos()){
					row.setMesh(meshId);
					
					RdLaneTopology topo = (RdLaneTopology)row;
					
					connexity.topologyMap.put(topo.getPid(), topo);
					
					for(IRow row2 : topo.getVias()){
						row2.setMesh(meshId);
						
						RdLaneVia via = (RdLaneVia)row2;
						
						connexity.viaMap.put(via.getRowId(), via);
					}
				}
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

		return connexity;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		return null;
	}
	
	public List<RdLaneConnexity> loadRdLaneConnexityByLinkPid(int linkPid,boolean isLock) throws Exception
	{
		List<RdLaneConnexity> laneConns = new ArrayList<RdLaneConnexity>();
		
		String sql = "select a.*,b.mesh_id from rd_lane_connexity a,rd_link b where a.in_link_pid = :1 and a.u_record!=2 and a.in_link_pid = b.link_pid ";
		
		if (isLock){
			sql += " for update nowait";
		}
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdLaneConnexity laneConn = new RdLaneConnexity();
				
				laneConn.setPid(resultSet.getInt("pid"));
				
				laneConn.setRowId(resultSet.getString("row_id"));
				
				laneConn.setInLinkPid(resultSet.getInt("in_link_pid"));
				
				laneConn.setNodePid(resultSet.getInt("node_pid"));
				
				laneConn.setLaneInfo(resultSet.getString("lane_info"));
				
				laneConn.setConflictFlag(resultSet.getInt("conflict_flag"));
				
				laneConn.setKgFlag(resultSet.getInt("kg_flag"));
				
				laneConn.setLaneNum(resultSet.getInt("lane_num"));
				
				laneConn.setLeftExtend(resultSet.getInt("left_extend"));
				
				laneConn.setRightExtend(resultSet.getInt("right_extend"));
				
				laneConn.setSrcFlag(resultSet.getInt("src_flag"));
				
				laneConn.setMesh(resultSet.getInt("mesh_id"));
				
				laneConns.add(laneConn);
			}
		} catch (Exception e) {
			
			throw e;
		} finally {
			try {
				resultSet.close();
			} catch (Exception e) {
				
			}

			try {
				pstmt.close();
			} catch (Exception e) {
				
			}
		}
		
		return laneConns;
	}
	
	public List<RdLaneConnexity> loadRdLaneConnexityByNodePid(int nodePid,boolean isLock) throws Exception
	{
		List<RdLaneConnexity> laneConns = new ArrayList<RdLaneConnexity>();
		
//		String sql = "select * from rd_lane_connexity where node_pid = :1 and u_record!=2 ";

		String sql = "select a.*,b.mesh_id from rd_lane_connexity a,rd_link b where a.node_pid = :1 and a.u_record!=2 and a.in_link_pid = b.link_pid ";
		
		if (isLock){
			sql += " for update nowait";
		}
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, nodePid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdLaneConnexity laneConn = new RdLaneConnexity();
				
				laneConn.setPid(resultSet.getInt("pid"));
				
				laneConn.setRowId(resultSet.getString("row_id"));
				
				laneConn.setInLinkPid(resultSet.getInt("in_link_pid"));
				
				laneConn.setNodePid(resultSet.getInt("node_pid"));
				
				laneConn.setLaneInfo(resultSet.getString("lane_info"));
				
				laneConn.setConflictFlag(resultSet.getInt("conflict_flag"));
				
				laneConn.setKgFlag(resultSet.getInt("kg_flag"));
				
				laneConn.setLaneNum(resultSet.getInt("lane_num"));
				
				laneConn.setLeftExtend(resultSet.getInt("left_extend"));
				
				laneConn.setRightExtend(resultSet.getInt("right_extend"));
				
				laneConn.setSrcFlag(resultSet.getInt("src_flag"));
				
				laneConn.setMesh(resultSet.getInt("mesh_id"));
				
				laneConns.add(laneConn);
			}
		} catch (Exception e) {
			
			throw e;
		} finally {
			try {
				resultSet.close();
			} catch (Exception e) {
				
			}

			try {
				pstmt.close();
			} catch (Exception e) {
				
			}
		}
		
		return laneConns;
	}
	
	public List<RdLaneConnexity> loadRdLaneConnexityByLinkNode(int linkPid, int nodePid1, int nodePid2,
			boolean isLock) throws Exception {
		List<RdLaneConnexity> laneConns = new ArrayList<RdLaneConnexity>();
		
		String sql = "select a.*,b.mesh_id from rd_lane_connexity a,rd_link b where a.node_pid in (:1,:2) and a.in_link_pid=:3 and a.u_record!=2 and a.in_link_pid = b.link_pid ";
		
		if (isLock){
			sql += " for update nowait";
		}
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, nodePid1);
			
			pstmt.setInt(2, nodePid2);
			
			pstmt.setInt(3, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdLaneConnexity laneConn = new RdLaneConnexity();
				
				laneConn.setPid(resultSet.getInt("pid"));
				
				laneConn.setRowId(resultSet.getString("row_id"));
				
				laneConn.setInLinkPid(resultSet.getInt("in_link_pid"));
				
				laneConn.setNodePid(resultSet.getInt("node_pid"));
				
				laneConn.setLaneInfo(resultSet.getString("lane_info"));
				
				laneConn.setConflictFlag(resultSet.getInt("conflict_flag"));
				
				laneConn.setKgFlag(resultSet.getInt("kg_flag"));
				
				laneConn.setLaneNum(resultSet.getInt("lane_num"));
				
				laneConn.setLeftExtend(resultSet.getInt("left_extend"));
				
				laneConn.setRightExtend(resultSet.getInt("right_extend"));
				
				laneConn.setSrcFlag(resultSet.getInt("src_flag"));
				
				laneConn.setMesh(resultSet.getInt("mesh_id"));
				
				laneConns.add(laneConn);
			}
		} catch (Exception e) {
			
			throw e;
		} finally {
			try {
				resultSet.close();
			} catch (Exception e) {
				
			}

			try {
				pstmt.close();
			} catch (Exception e) {
				
			}
		}
		
		return laneConns;
	}
}
