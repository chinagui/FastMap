package com.navinfo.dataservice.dao.glm.selector.rd.same;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNode;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNodePart;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.vividsolutions.jts.geom.Geometry;

public class RdSameLinkSelector extends AbstractSelector {

	private Connection conn;

	/**
	 * @param cls
	 * @param conn
	 */
	public RdSameLinkSelector(Connection conn) {

		super(RdSameLink.class, conn);

		this.conn = conn;
	}

	public Geometry getMainLinkGeometry(int linkPid, String tableName,
			boolean isLock) throws Exception {

		StringBuilder sb = new StringBuilder("select geometry from "
				+ tableName + " where link_pid = :1 and u_record!=2");

		if (isLock) {

			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				return GeoTranslator.struct2Jts((STRUCT) resultSet
						.getObject("geometry"));
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return null;
	}

	/**
	 * 根据nodePid和表名称查询同一点关系组成点
	 * 
	 * @param nodePid
	 * @param tableName
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public RdSameNodePart loadByNodePidAndTableName(int nodePid,
			String tableName, boolean isLock) throws Exception {
		RdSameNodePart sameNodePart = new RdSameNodePart();

		StringBuilder sb = new StringBuilder(
				"select group_id from "
						+ sameNodePart.tableName()
						+ " where node_pid = :1 and upper(table_name) = :2 and u_record!=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, nodePid);

			pstmt.setString(2, tableName.toUpperCase());

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				sameNodePart.setGroupId(resultSet.getInt("group_id"));

				sameNodePart.setNodePid(nodePid);

				sameNodePart.setTableName(tableName);
			} else {
				return null;
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return sameNodePart;
	}

	/**
	 * 根据表名称和nodePid查询几何
	 * 
	 * @param nodePid
	 * @param tableName
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public Geometry getGeoByNodePidAndTableName(int nodePid, String tableName,
			boolean isLock) throws Exception {
		StringBuilder sb = new StringBuilder("select geometry from "
				+ tableName + " where node_pid = :1 and u_record!=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, nodePid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				return GeoTranslator.struct2Jts((STRUCT) resultSet
						.getObject("geometry"));
			} else {
				return null;
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}

	/**
	 * 根据nodePid查询CRF交叉点对象
	 * 
	 * @param nodePidStr
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdSameNode> loadSameNodeByNodePids(String nodePidStr,
			String tableName, boolean isLock) throws Exception {
		List<RdSameNode> sameNodeList = new ArrayList<>();

		HashSet<Integer> pidSet = new HashSet<Integer>();

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			StringBuilder sb = new StringBuilder(
					"select a.* from rd_samenode a,rd_samenode_part b where a.group_id = b.group_id and b.NODE_PID in("
							+ nodePidStr
							+ ") and upper(b.table_name) = '"
							+ tableName.toUpperCase()
							+ "' and a.u_record !=2 and b.u_record !=2");
			if (isLock) {
				sb.append(" for update nowait");
			}
			pstmt = conn.prepareStatement(sb.toString());

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdSameNode sameNode = new RdSameNode();

				int pid = resultSet.getInt("pid");

				if (!pidSet.contains(pid)) {
					ReflectionAttrUtils.executeResultSet(sameNode, resultSet);

					List<IRow> parts = new AbstractSelector(
							RdSameNodePart.class, conn).loadRowsByParentId(
							sameNode.getPid(), isLock);

					sameNode.setParts(parts);

					sameNodeList.add(sameNode);

					pidSet.add(pid);
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return sameNodeList;
	}

	public List<Integer> loadLinkByNodePids(String tableName, String nodePids,
			boolean isLock) throws Exception {
		List<Integer> linkPidList = new ArrayList<>();

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			StringBuilder sb = new StringBuilder("select link_pid from "
					+ tableName + " where s_node_pid in(" + nodePids
					+ ") and e_node_pid in(" + nodePids + ") and u_record !=2");
			if (isLock) {
				sb.append(" for update nowait");
			}
			pstmt = conn.prepareStatement(sb.toString());

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				int pid = resultSet.getInt("link_pid");

				linkPidList.add(pid);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return linkPidList;
	}
}
