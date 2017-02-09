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

	private static Logger logger = Logger
			.getLogger(RdLaneConnexitySelector.class);

	private Connection conn;

	public RdLaneConnexitySelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdLaneConnexity.class);
	}

	@Override
	public IRow loadById(int id, boolean isLock, boolean... loadChild)
			throws Exception {

		RdLaneConnexity connexity = new RdLaneConnexity();

		String sql = "select * from " + connexity.tableName()
				+ " where pid=:1 and u_record!=2";

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

				RdLaneTopologySelector topoSelector = new RdLaneTopologySelector(
						conn);

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

	public List<RdLaneConnexity> loadRdLaneConnexityByLinkPid(int linkPid,
			boolean isLock) throws Exception {
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

				RdLaneTopologySelector topoSelector = new RdLaneTopologySelector(
						conn);

				laneConn.setTopos(topoSelector.loadRowsByParentId(
						laneConn.pid(), isLock));

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

	public List<RdLaneConnexity> loadRdLaneConnexityByOutLinkPid(int linkPid,
			boolean isLock) throws Exception {
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

				RdLaneTopologySelector topoSelector = new RdLaneTopologySelector(
						conn);

				laneConn.setTopos(topoSelector.loadRowsByParentId(
						laneConn.pid(), isLock));

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

	public List<RdLaneConnexity> loadRdLaneConnexityByNodePid(int nodePid,
			boolean isLock) throws Exception {
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

	public List<RdLaneConnexity> loadRdLaneConnexityByLinkNode(int linkPid,
			int nodePid1, int nodePid2, boolean isLock) throws Exception {
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

	/**
	 * 根据link类型获取车信
	 * 
	 * @param linkPid
	 * @param linkType
	 *            1：进入线；2：退出线，3：经过线
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdLaneConnexity> loadByLink(int linkPid, int linkType,
			boolean isLock) throws Exception {

		List<RdLaneConnexity> laneConns = new ArrayList<RdLaneConnexity>();

		String sql = "";

		if (linkType == 1) {
			sql = "SELECT * FROM RD_LANE_CONNEXITY WHERE IN_LINK_PID = :1 AND U_RECORD !=2 ";
		} else if (linkType == 2) {

			sql = "SELECT * FROM RD_LANE_CONNEXITY WHERE U_RECORD != 2  AND PID IN (SELECT DISTINCT (CONNEXITY_PID) FROM RD_LANE_TOPOLOGY WHERE U_RECORD != 2  AND OUT_LINK_PID = :1)";
		}

		else if (linkType == 3) {

			sql = "SELECT * FROM RD_LANE_CONNEXITY WHERE U_RECORD != 2 AND PID IN (SELECT DISTINCT (CONNEXITY_PID) FROM RD_LANE_TOPOLOGY WHERE U_RECORD != 2 AND TOPOLOGY_ID IN (SELECT DISTINCT (TOPOLOGY_ID) FROM RD_LANE_VIA WHERE U_RECORD != 2 AND LINK_PID = :1))";
		} else {
			return laneConns;
		}

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			RdLaneTopologySelector topoSelector = new RdLaneTopologySelector(
					conn);

			RdLaneViaSelector viaSelector = new RdLaneViaSelector(conn);

			while (resultSet.next()) {

				RdLaneConnexity laneConn = new RdLaneConnexity();

				ReflectionAttrUtils.executeResultSet(laneConn, resultSet);

				laneConn.setTopos(topoSelector.loadRowsByParentId(
						laneConn.pid(), isLock));

				for (IRow row : laneConn.getTopos()) {

					RdLaneTopology topo = (RdLaneTopology) row;

					topo.setVias(viaSelector.loadRowsByParentId(topo.getPid(),
							isLock));

					laneConn.topologyMap.put(topo.getPid(), topo);

					for (IRow row2 : topo.getVias()) {

						RdLaneVia via = (RdLaneVia) row2;

						laneConn.viaMap.put(via.getRowId(), via);

						topo.viaMap.put(via.getRowId(), via);
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

	/**
	 * 根据路口pid查询车信信息
	 * 
	 * @param crossPid
	 *            路口pid
	 * @param isLock
	 *            是否加锁
	 * @return 车信对象集合
	 * @throws Exception
	 */
	public List<RdLaneConnexity> getRdLaneConnexityByCrossPid(int crossPid,
			boolean isLock) throws Exception {
		List<RdLaneConnexity> result = new ArrayList<RdLaneConnexity>();

		String sql = "select * from rd_lane_connexity a where exists (select null from rd_cross_node b where b.pid=:1 and a.node_pid=b.node_pid) and u_record!=2";
		if (isLock) {
			sql = sql + " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = getConn().prepareStatement(sql);

			pstmt.setInt(1, crossPid);

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

				RdLaneTopologySelector topoSelector = new RdLaneTopologySelector(
						getConn());

				laneConn.setTopos(topoSelector.loadRowsByParentId(
						laneConn.getPid(), true));

				result.add(laneConn);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return result;
	}

	/**
	 * 根据link类型获取车信
	 * 
	 * @param linkPids
	 * @param linkType
	 *            1：进入线；2：退出线，3：经过线
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdLaneConnexity> loadByLinks(List<Integer> linkPids,
			int linkType, boolean isLock) throws Exception {

		List<RdLaneConnexity> rows = new ArrayList<RdLaneConnexity>();

		if (linkPids == null || linkPids.size() == 0) {
			return rows;
		}

		List<Integer> linkPidTemp = new ArrayList<Integer>();

		linkPidTemp.addAll(linkPids);

		int dataLimit = 100;

		while (linkPidTemp.size() >= dataLimit) {

			List<Integer> listPid = linkPidTemp.subList(0, dataLimit);

			rows.addAll(loadByLinkPids(listPid, linkType, isLock));

			linkPidTemp.subList(0, dataLimit).clear();
		}

		if (!linkPidTemp.isEmpty()) {
			rows.addAll(loadByLinkPids(linkPidTemp, linkType, isLock));
		}

		return rows;
	}

	private List<RdLaneConnexity> loadByLinkPids(List<Integer> linkPids,
			int linkType, boolean isLock) throws Exception {

		List<RdLaneConnexity> laneConns = new ArrayList<RdLaneConnexity>();

		if (linkPids == null || linkPids.isEmpty()) {

			return laneConns;
		}

		String pids = org.apache.commons.lang.StringUtils.join(linkPids, ",");

		String sql = "";

		if (linkType == 1) {
			sql = "SELECT * FROM RD_LANE_CONNEXITY WHERE U_RECORD !=2 AND IN_LINK_PID IN ("
					+ pids + ")  ";

		} else if (linkType == 2) {

			sql = "SELECT * FROM RD_LANE_CONNEXITY WHERE U_RECORD != 2  AND PID IN (SELECT DISTINCT (CONNEXITY_PID) FROM RD_LANE_TOPOLOGY WHERE U_RECORD != 2  AND OUT_LINK_PID  IN ("
					+ pids + "))";
		}

		else if (linkType == 3) {

			sql = "SELECT * FROM RD_LANE_CONNEXITY WHERE U_RECORD != 2 AND PID IN (SELECT DISTINCT (CONNEXITY_PID) FROM RD_LANE_TOPOLOGY WHERE U_RECORD != 2 AND TOPOLOGY_ID IN (SELECT DISTINCT (TOPOLOGY_ID) FROM RD_LANE_VIA WHERE U_RECORD != 2 AND LINK_PID  IN ("
					+ pids + ")))";
		} else {
			return laneConns;
		}

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			RdLaneTopologySelector topoSelector = new RdLaneTopologySelector(
					conn);

			RdLaneViaSelector viaSelector = new RdLaneViaSelector(conn);

			while (resultSet.next()) {

				RdLaneConnexity laneConn = new RdLaneConnexity();

				ReflectionAttrUtils.executeResultSet(laneConn, resultSet);

				laneConn.setTopos(topoSelector.loadRowsByParentId(
						laneConn.pid(), isLock));

				for (IRow row : laneConn.getTopos()) {

					RdLaneTopology topo = (RdLaneTopology) row;

					topo.setVias(viaSelector.loadRowsByParentId(topo.getPid(),
							isLock));

					laneConn.topologyMap.put(topo.getPid(), topo);

					for (IRow row2 : topo.getVias()) {

						RdLaneVia via = (RdLaneVia) row2;

						laneConn.viaMap.put(via.getRowId(), via);

						topo.viaMap.put(via.getRowId(), via);
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

	/**
	 * 根据经过点获取车信pid
	 * 
	 * @param nodePids
	 *            不能超过1000个
	 * @param isLock
	 * @return
	 */
	public List<Integer> getPidByPassNode(List<Integer> nodePids)
			throws Exception {
		
		List<Integer> connexityPids = new ArrayList<Integer>();

		if (nodePids == null || nodePids.isEmpty()) {

			return connexityPids;
		}

		String pids = org.apache.commons.lang.StringUtils.join(nodePids, ",");

		String sql = "SELECT DISTINCT C.PID FROM RD_LANE_VIA V, RD_LINK L, RD_LANE_TOPOLOGY T,RD_LANE_CONNEXITY C WHERE ( ";

		sql += " L.S_NODE_PID IN (" + pids + ")  OR ";

		sql += " L.E_NODE_PID IN (" + pids + ") ";

		sql += " ) AND L.LINK_PID = V.LINK_PID AND V.TOPOLOGY_ID = T.TOPOLOGY_ID AND T.CONNEXITY_PID = C.PID  AND L.U_RECORD <> 2 AND V.U_RECORD <> 2 AND T.U_RECORD <> 2 AND C.U_RECORD <> 2 ";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				connexityPids.add(resultSet.getInt("PID"));
			}

		} catch (Exception e) {
			
			throw e;
			
		} finally {
			
			DBUtils.closeResultSet(resultSet);
			
			DBUtils.closeStatement(pstmt);
		}
		
		return connexityPids;
	}
	
	/**
	 * 根据进入点获取车信pid
	 * 
	 * @param nodePids
	 *            不能超过1000个
	 * @param isLock
	 * @return
	 */
	public List<Integer> getPidByInNode(List<Integer> nodePids) throws Exception {
		
		List<Integer> connexityPids = new ArrayList<Integer>();
		
		String pids = org.apache.commons.lang.StringUtils.join(nodePids, ",");

		String sql = " SELECT DISTINCT PID FROM RD_LANE_CONNEXITY WHERE NODE_PID IN ( "
				+ pids + " ) AND U_RECORD!=2 ";
	
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			
			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				connexityPids.add(resultSet.getInt("PID"));
			}

		} catch (Exception e) {
			
			throw e;
			
		} finally {
			
			DBUtils.closeResultSet(resultSet);
			
			DBUtils.closeStatement(pstmt);
		}
		
		return connexityPids;
	}
}