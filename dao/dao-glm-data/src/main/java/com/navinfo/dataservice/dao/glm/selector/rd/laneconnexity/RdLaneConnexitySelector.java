package com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class RdLaneConnexitySelector extends AbstractSelector {

	private static Logger logger = Logger.getLogger(RdLaneConnexitySelector.class);

	private Connection conn;

	public RdLaneConnexitySelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdLaneConnexity.class);
	}

	@Override
	public IRow loadById(int id, boolean isLock,boolean ... loadChild) throws Exception {

		RdLaneConnexity connexity = new RdLaneConnexity();

		String sql = "select * from " + connexity.tableName() + " where pid=:1 and u_record!=2";

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

				ReflectionAttrUtils.executeResultSet(connexity, resultSet);

				RdLaneTopologySelector topoSelector = new RdLaneTopologySelector(conn);

				connexity.setTopos(topoSelector.loadRowsByParentId(id, isLock));

				for (IRow row : connexity.getTopos()) {

					RdLaneTopology topo = (RdLaneTopology) row;

					connexity.topologyMap.put(topo.getPid(), topo);

					for (IRow row2 : topo.getVias()) {

						RdLaneVia via = (RdLaneVia) row2;

						connexity.viaMap.put(via.getRowId(), via);
					}
				}
			} else {

				throw new DataNotFoundException("数据不存在");
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return connexity;
	}

	public List<RdLaneConnexity> loadRdLaneConnexityByLinkPid(int linkPid, boolean isLock) throws Exception {
		List<RdLaneConnexity> laneConns = new ArrayList<RdLaneConnexity>();

		String sql = "select * from rd_lane_connexity where in_link_pid = :1 and u_record!=2 ";

		if (isLock) {
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

				ReflectionAttrUtils.executeResultSet(laneConn, resultSet);

				RdLaneTopologySelector topoSelector = new RdLaneTopologySelector(conn);

				laneConn.setTopos(topoSelector.loadRowsByParentId(laneConn.pid(), isLock));

				for (IRow row : laneConn.getTopos()) {

					RdLaneTopology topo = (RdLaneTopology) row;

					laneConn.topologyMap.put(topo.getPid(), topo);

					for (IRow row2 : topo.getVias()) {

						RdLaneVia via = (RdLaneVia) row2;

						laneConn.viaMap.put(via.getRowId(), via);
					}
				}

				laneConns.add(laneConn);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return laneConns;
	}

	public List<RdLaneConnexity> loadRdLaneConnexityByOutLinkPid(int linkPid, boolean isLock) throws Exception {
		List<RdLaneConnexity> laneConns = new ArrayList<RdLaneConnexity>();

		String sql = "select * from rd_lane_connexity  where pid in (  select b.CONNEXITY_PID from rd_lane_topology b where b.CONNEXITY_PID in (    select CONNEXITY_PID from rd_lane_topology where u_record!=2 and out_link_pid=:1 )     group by b.CONNEXITY_PID) and  u_record != 2";

		if (isLock) {
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

				ReflectionAttrUtils.executeResultSet(laneConn, resultSet);

				int meshId = resultSet.getInt("mesh_id");

				laneConn.setMesh(meshId);

				RdLaneTopologySelector topoSelector = new RdLaneTopologySelector(conn);

				laneConn.setTopos(topoSelector.loadRowsByParentId(laneConn.pid(), isLock));

				for (IRow row : laneConn.getTopos()) {
					row.setMesh(meshId);

					RdLaneTopology topo = (RdLaneTopology) row;

					laneConn.topologyMap.put(topo.getPid(), topo);

					for (IRow row2 : topo.getVias()) {
						row2.setMesh(meshId);

						RdLaneVia via = (RdLaneVia) row2;

						laneConn.viaMap.put(via.getRowId(), via);
					}
				}

				laneConns.add(laneConn);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return laneConns;
	}

	public List<RdLaneConnexity> loadRdLaneConnexityByNodePid(int nodePid, boolean isLock) throws Exception {
		List<RdLaneConnexity> laneConns = new ArrayList<RdLaneConnexity>();

		 String sql = "select * from rd_lane_connexity where node_pid = :1 and u_record!=2 ";

		if (isLock) {
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

				ReflectionAttrUtils.executeResultSet(laneConn, resultSet);

				laneConns.add(laneConn);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return laneConns;
	}

	public List<RdLaneConnexity> loadRdLaneConnexityByLinkNode(int linkPid, int nodePid1, int nodePid2, boolean isLock)
			throws Exception {
		List<RdLaneConnexity> laneConns = new ArrayList<RdLaneConnexity>();

		String sql = "select * from rd_lane_connexity where a.node_pid in (:1,:2) and a.in_link_pid=:3 and a.u_record!=2 ";

		if (isLock) {
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

				ReflectionAttrUtils.executeResultSet(laneConn, resultSet);

				laneConns.add(laneConn);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return laneConns;
	}
}