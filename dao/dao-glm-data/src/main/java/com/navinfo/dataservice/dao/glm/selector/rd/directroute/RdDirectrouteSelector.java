package com.navinfo.dataservice.dao.glm.selector.rd.directroute;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectrouteVia;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class RdDirectrouteSelector extends AbstractSelector {

	private static Logger logger = Logger
			.getLogger(RdDirectrouteSelector.class);

	public RdDirectrouteSelector(Connection conn) {
		super(RdDirectroute.class, conn);
	}

	public RdDirectroute loadByLinkNodeLink(int inLinkPid, int nodePid,
			int outLinkPid, boolean isLock) throws Exception {

		RdDirectroute directroute = new RdDirectroute();

		String sql = "select a.* from rd_directroute a where a.in_link_pid=:1 and a.node_pid=:2 and a.out_link_pid=:3 and a.u_record!=2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = getConn().prepareStatement(sql);

			pstmt.setInt(1, inLinkPid);

			pstmt.setInt(2, nodePid);

			pstmt.setInt(3, outLinkPid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				ReflectionAttrUtils.executeResultSet(directroute, resultSet);

				directroute.setVias(new AbstractSelector(
						RdDirectrouteVia.class, getConn()).loadRowsByParentId(
						directroute.getPid(), isLock));

			} else {
				return null;
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return directroute;

	}

	/**
	 * 通过link获取顺行。link分别为进入线或退出线
	 * 
	 * @param inLinkPid
	 * @param nodePid
	 * @param outLinkPid
	 * @param isLock
	 * @return 。
	 * @throws Exception
	 */
	public List<RdDirectroute> loadByInOutLink(int linkPid, boolean isLock)
			throws Exception {

		List<RdDirectroute> rows = new ArrayList<RdDirectroute>();

		String sql = "select a.* from rd_directroute a where a.in_link_pid=:1 or a.out_link_pid=:2 and a.u_record!=2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = getConn().prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			pstmt.setInt(2, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdDirectroute directroute = new RdDirectroute();

				ReflectionAttrUtils.executeResultSet(directroute, resultSet);

				directroute.setVias(new AbstractSelector(
						RdDirectrouteVia.class, getConn()).loadRowsByParentId(
						directroute.getPid(), isLock));

				rows.add(directroute);
			}

		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return rows;

	}

	/**
	 * 通过link获取顺行。link是顺行的经过线
	 * 
	 * @param inLinkPid
	 * @param nodePid
	 * @param outLinkPid
	 * @param isLock
	 * @return 。
	 * @throws Exception
	 */
	public List<RdDirectroute> loadByPassLink(int linkPid, boolean isLock)
			throws Exception {

		List<RdDirectroute> rows = new ArrayList<RdDirectroute>();

		String sql = "select a.* from rd_directroute a,rd_directroute_via b where a.pid=b.pid and b.link_pid=:1 and a.u_record!=2 and b.u_record!=2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = getConn().prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdDirectroute directroute = new RdDirectroute();

				ReflectionAttrUtils.executeResultSet(directroute, resultSet);

				directroute.setVias(new AbstractSelector(
						RdDirectrouteVia.class, getConn()).loadRowsByParentId(
						directroute.getPid(), isLock));

				rows.add(directroute);
			}

		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return rows;

	}

	/**
	 * 通过link获取顺行pid。link为顺行的进入线、退出线、经过线
	 * 
	 * @param linkPid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<Integer> loadPidByLink(int linkPid, boolean isLock)
			throws Exception {
		List<Integer> pids = new ArrayList<Integer>();

		List<RdDirectroute> directroutes = loadByInOutLink(linkPid, isLock);

		directroutes.addAll(loadByPassLink(linkPid, isLock));

		for (RdDirectroute directroute : directroutes) {
			pids.add(directroute.getPid());
		}

		return pids;
	}
	
	/**
	 * 根据路口pid查询路口关系的顺行
	 * @param crossPid 路口pid
	 * @param isLock 是否加锁
	 * @return 交限集合
	 * @throws Exception
	 */
	public List<RdDirectroute> getRestrictionByCrossPid(int crossPid,boolean isLock) throws Exception {

		List<RdDirectroute> result = new ArrayList<RdDirectroute>();

		String sql = "select * from RD_DIRECTROUTE a where exists (select null from rd_cross_node b where b.pid=:1 and a.node_pid=b.node_pid) and u_record!=2";

		sql = sql + " for update nowait";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = getConn().prepareStatement(sql);

			pstmt.setInt(1, crossPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdDirectroute directroute = new RdDirectroute();

				ReflectionAttrUtils.executeResultSet(directroute, resultSet);

				directroute.setVias(new AbstractSelector(
						RdDirectrouteVia.class, getConn()).loadRowsByParentId(
						directroute.getPid(), isLock));
				
				result.add(directroute);
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return result;
	}
	
	
	
	public List<RdDirectroute> loadByLinkPid(int linkPid, int linkType,
			boolean isLock) throws Exception {
		
		List<RdDirectroute> rows = new ArrayList<RdDirectroute>();

		String sql = "";

		if (linkType == 1) {

			sql = "SELECT * FROM RD_DIRECTROUTE WHERE U_RECORD!=2  AND IN_LINK_PID  = :1";

		} else if (linkType == 2) {

			sql = "SELECT * FROM RD_DIRECTROUTE WHERE U_RECORD!=2  AND OUT_LINK_PID = :1";
			
		} else if (linkType == 3) {

			sql = "SELECT * FROM RD_DIRECTROUTE WHERE U_RECORD != 2 AND PID IN (SELECT DISTINCT (PID) FROM RD_DIRECTROUTE_VIA WHERE U_RECORD != 2 AND LINK_PID = :1)";
		
		} else {
			return rows;
		}

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = getConn().prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdDirectroute directroute = new RdDirectroute();

				ReflectionAttrUtils.executeResultSet(directroute, resultSet);

				directroute.setVias(new AbstractSelector(
						RdDirectrouteVia.class, getConn()).loadRowsByParentId(
						directroute.getPid(), isLock));

				rows.add(directroute);
			}

		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return rows;

	}

	
	/**
	 * 根据link类型获取顺行
	 * 
	 * @param linkPids
	 * @param linkType
	 *            1：进入线；2：退出线，3：经过线
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdDirectroute> loadByLinks(List<Integer> linkPids, int linkType,
			boolean isLock) throws Exception {

		List<RdDirectroute> rows = new ArrayList<RdDirectroute>();

		if (linkPids == null || linkPids.size() == 0) {
			return rows;
		}

		List<Integer> pidTemp = new ArrayList<Integer>();

		pidTemp.addAll(linkPids);

		int dataLimit = 100;

		while (pidTemp.size() >= dataLimit) {

			List<Integer> listPid = pidTemp.subList(0, dataLimit);

			rows.addAll(loadByLinkPids(listPid, linkType, isLock));

			pidTemp.subList(0, dataLimit).clear();
		}

		if (!pidTemp.isEmpty()) {
			rows.addAll(loadByLinkPids(pidTemp, linkType, isLock));
		}

		return rows;
	}
	
	
	public List<RdDirectroute> loadByLinkPids(List<Integer> linkPids, int linkType,
			boolean isLock) throws Exception {
		
		List<RdDirectroute> rows = new ArrayList<RdDirectroute>();

		if (linkPids == null || linkPids.isEmpty()) {

			return rows;
		}

		String pids = org.apache.commons.lang.StringUtils.join(linkPids, ",");

		String sql = "";


		if (linkType == 1) {

			sql = "SELECT * FROM RD_DIRECTROUTE WHERE U_RECORD!=2  AND IN_LINK_PID  IN ("
					+ pids + ")  ";

		} else if (linkType == 2) {

			sql = "SELECT * FROM RD_DIRECTROUTE WHERE U_RECORD!=2  AND OUT_LINK_PID  IN ("
					+ pids + ")  ";
			
		} else if (linkType == 3) {

			sql = "SELECT * FROM RD_DIRECTROUTE WHERE U_RECORD != 2 AND PID IN (SELECT DISTINCT (PID) FROM RD_DIRECTROUTE_VIA WHERE U_RECORD != 2 AND LINK_PID IN ("
					+ pids + "))";
		
		} else {
			return rows;
		}

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = getConn().prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdDirectroute directroute = new RdDirectroute();

				ReflectionAttrUtils.executeResultSet(directroute, resultSet);

				directroute.setVias(new AbstractSelector(
						RdDirectrouteVia.class, getConn()).loadRowsByParentId(
						directroute.getPid(), isLock));

				rows.add(directroute);
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
