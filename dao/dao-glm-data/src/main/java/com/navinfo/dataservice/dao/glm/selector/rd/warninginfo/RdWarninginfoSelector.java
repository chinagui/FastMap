package com.navinfo.dataservice.dao.glm.selector.rd.warninginfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.model.rd.warninginfo.RdWarninginfo;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

public class RdWarninginfoSelector extends  AbstractSelector{

	private Connection conn;

	public RdWarninginfoSelector(Connection conn) {
		super(conn);
		this.setCls(RdWarninginfo.class);
		this.conn = conn;
	}

	public List<Integer> loadPidByNode(int nodePid, boolean isLock) throws Exception 
	{
		List<Integer> infectList = new ArrayList<Integer>();
		
		List<RdWarninginfo> warninginfos = loadByNode(
				nodePid, isLock);

		for (RdWarninginfo warninginfo : warninginfos) {
			infectList.add(warninginfo.getPid());
		}
		
		return infectList;		
	}
	

	public List<RdWarninginfo> loadByNode(int nodePid, boolean isLock)
			throws Exception {
		List<RdWarninginfo> rows = new ArrayList<RdWarninginfo>();

		String sql = "select a.* from rd_warninginfo a where a.u_record!=:1 and a.node_pid=:2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, 2);

			pstmt.setInt(2, nodePid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdWarninginfo obj = new RdWarninginfo();

				ReflectionAttrUtils.executeResultSet(obj, resultSet);

				rows.add(obj);
			}

		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return rows;
	}
	
	
	public List<Integer> loadPidByLink(int linkPid, boolean isLock) throws Exception 
	{
		List<Integer> infectList = new ArrayList<Integer>();
		
		List<RdWarninginfo> warninginfos = loadByLink(
				linkPid, isLock);

		for (RdWarninginfo warninginfo : warninginfos) {
			infectList.add(warninginfo.getPid());
		}
		
		return infectList;		
	}

	public List<RdWarninginfo> loadByLink(int linkPid, boolean isLock)
			throws Exception {
		List<RdWarninginfo> rows = new ArrayList<RdWarninginfo>();

		String sql = "select a.* from rd_warninginfo a where a.u_record!=:1 and a.link_pid=:2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, 2);

			pstmt.setInt(2, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdWarninginfo obj = new RdWarninginfo();

				ReflectionAttrUtils.executeResultSet(obj, resultSet);

				rows.add(obj);
			}

		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);

		}

		return rows;
	}
	
	public List<RdWarninginfo> loadByLinks(List<Integer> linkPids,
			boolean isLock) throws Exception {
		
		List<RdWarninginfo> rows = new ArrayList<RdWarninginfo>();
		
		if(linkPids==null ||linkPids.size()==0)
		{
			return rows;
		}
		
		int pointsDataLimit = 100;
		
		while (linkPids.size() >= pointsDataLimit) {
			List<Integer> listPid = linkPids.subList(0, pointsDataLimit);

			rows.addAll(loadByLinkPids(listPid, isLock));
			
			linkPids.subList(0, pointsDataLimit).clear();
		}

		if (!linkPids.isEmpty()) {
			rows.addAll(loadByLinkPids(linkPids, isLock));
		}
	
		return rows;
	}

	private List<RdWarninginfo> loadByLinkPids(List<Integer> linkPids,
			boolean isLock) throws Exception {
		List<RdWarninginfo> rows = new ArrayList<RdWarninginfo>();

		if(linkPids==null ||linkPids.size()==0)
		{
			return rows;
		}
		
		StringBuilder strLinkPids = new StringBuilder();

		for (Integer pid : linkPids) {
			strLinkPids.append(" " + pid.toString() +",");
		}

		String sql = "select a.* from rd_warninginfo a where a.u_record!=:1 and a.link_pid in ( "
				+ strLinkPids.toString() + ") ";

		if (isLock) {
			sql += " for update nowait";
		}
		
		sql=sql.replace(",)", ")");
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, 2);		

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdWarninginfo obj = new RdWarninginfo();

				ReflectionAttrUtils.executeResultSet(obj, resultSet);

				rows.add(obj);
			}

		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);

		}

		return rows;
	}

	

}
