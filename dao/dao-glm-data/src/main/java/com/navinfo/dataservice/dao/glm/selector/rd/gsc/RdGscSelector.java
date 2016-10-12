package com.navinfo.dataservice.dao.glm.selector.rd.gsc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

public class RdGscSelector extends AbstractSelector {

	private static Logger logger = Logger.getLogger(RdCrossSelector.class);

	private Connection conn;

	public RdGscSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdGsc.class);
	}

	/**
	 * 根据linkPid和组成link的表名称查询立交,返回的是立交下的所有组成线
	 * @param linkPid link的pid
	 * @param tableName 组成link的表名称
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdGsc> loadRdGscLinkByLinkPid(int linkPid, String tableName,boolean isLock) throws Exception {
		List<RdGsc> rdGscList = new ArrayList<RdGsc>();

		String sql = "SELECT a.* FROM rd_gsc a,rd_gsc_link b WHERE a.pid = b.pid AND b.link_pid = :1 and a.u_record!=2 and b.table_name = :2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);
			
			pstmt.setString(2, tableName);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdGsc rdGsc = new RdGsc();

				ReflectionAttrUtils.executeResultSet(rdGsc, resultSet);

				setChild(rdGsc, isLock);

				rdGscList.add(rdGsc);
			}

		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return rdGscList;
	}

	/**
	 * 根据linkPid和组成link的表名称查询立交，返回立交下的所有组成线
	 * @param linkPids link的pids
	 * @param tableName 组成link的表名称
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdGsc> loadRdGscByLinkPids(String linkPids, String tableName,boolean isLock) throws Exception {
		List<RdGsc> rdgscs = new ArrayList<RdGsc>();

		if (StringUtils.isEmpty(linkPids)) {
			return rdgscs;
		}

		String sql = "SELECT a.* FROM rd_gsc a,rd_gsc_link b WHERE a.pid = b.pid AND b.link_pid in (" + linkPids
				+ ") and a.u_record!=2 and b.table_name = :1";

		if (isLock) {
			sql = sql + " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, tableName);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdGsc rdGsc = new RdGsc();

				ReflectionAttrUtils.executeResultSet(rdGsc, resultSet);

				setChild(rdGsc, isLock);

				rdgscs.add(rdGsc);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return rdgscs;
	}

	/**
	 * 根据立交组成线返回立交，返回的立交中的组成线只有传入的立交组成线
	 * @param linkPid
	 * @param tableName
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdGsc> onlyLoadRdGscLinkByLinkPid(int linkPid, String tableName,boolean isLock) throws Exception {
		List<RdGsc> rdGscList = new ArrayList<RdGsc>();

		String sql = "SELECT a.* FROM rd_gsc a,rd_gsc_link b WHERE a.pid = b.pid AND b.link_pid = :1 and a.u_record!=2 and b.table_name = :2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);
			
			pstmt.setString(2, tableName);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdGsc rdGsc = new RdGsc();

				ReflectionAttrUtils.executeResultSet(rdGsc, resultSet);

				setChild(rdGsc, isLock);

				rdGscList.add(rdGsc);
			}

		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return rdGscList;
	}
	
	
	
	/**
	 * 根据立交的线反向获取立交
	 * 
	 * @param linkPid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdGsc> loadRdGscByInterLinkPids(List<Integer> linkPidList, boolean isLock) throws Exception {
		List<RdGsc> rdGscList = new ArrayList<RdGsc>();

		if (linkPidList.size() < 2) {
			return rdGscList;
		}
		String sql = "  SELECT * FROM rd_gsc WHERE pid in(SELECT pid  FROM RD_GSC_LINK WHERE  LINK_PID = :1 AND pid IN (SELECT pid FROM RD_GSC_LINK WHERE LINK_PID = :2 AND u_record !=2) AND u_record !=2)AND u_record !=2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPidList.get(0));

			pstmt.setInt(2, linkPidList.get(1));

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdGsc rdGsc = new RdGsc();

				ReflectionAttrUtils.executeResultSet(rdGsc, resultSet);
				
				setChild(rdGsc, isLock);

				rdGscList.add(rdGsc);
			}

		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return rdGscList;
	}
	
	/*
	 * 仅加载rd_gsc表，其他子表若有需要，请单独加载
	 */
	public List<RdGsc> loadBySql(String sql, boolean isLock)
			throws Exception {

		List<RdGsc> rdGscList = new ArrayList<RdGsc>();
		StringBuilder sb = new StringBuilder(sql);
		if (isLock) {
			sb.append(" for update nowait");
		}
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdGsc rdGsc = new RdGsc();
				ReflectionAttrUtils.executeResultSet(rdGsc, resultSet);
				rdGscList.add(rdGsc);
			}

		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return rdGscList;
	}
	
	/**
	 * 设置子表数据
	 * @param rdGsc
	 * @param isLock
	 * @throws Exception 
	 */
	private void setChild(RdGsc rdGsc,boolean isLock) throws Exception
	{
		List<IRow> links = new RdGscLinkSelector(conn).loadRowsByClassParentId(RdGscLink.class, rdGsc.getPid(), isLock, "zlevel");

		rdGsc.setLinks(links);

		for (IRow row : rdGsc.getLinks()) {
			RdGscLink obj = (RdGscLink) row;

			rdGsc.rdGscLinkMap.put(obj.rowId(), obj);
		}
	}
}
