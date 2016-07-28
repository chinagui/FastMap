package com.navinfo.dataservice.dao.glm.selector.rd.warninginfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.warninginfo.RdWarninginfo;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

public class RdWarninginfoSelector implements ISelector {

	private Connection conn;

	public RdWarninginfoSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		RdWarninginfo obj = new RdWarninginfo();

		String sql = "select a.* from " + obj.tableName()
				+ " a where a.pid=:1 and a.u_record!=2 ";

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

				ReflectionAttrUtils.executeResultSet(obj, resultSet);

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

		return obj;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
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

	/*
	 * private void setAttr(RdWarninginfo obj, ResultSet resultSet) throws
	 * SQLException {
	 * 
	 * obj.setPid(resultSet.getInt("pid"));
	 * 
	 * obj.setLinkPid(resultSet.getInt("link_pid"));
	 * 
	 * obj.setNodePid(resultSet.getInt("node_pid"));
	 * 
	 * obj.setTypeCode(resultSet.getString("type_code"));
	 * 
	 * obj.setValidDis(resultSet.getInt("valid_dis"));
	 * 
	 * obj.setWarnDis(resultSet.getInt("warn_dis"));
	 * 
	 * obj.setTimeDomain(resultSet.getString("time_domain"));
	 * 
	 * obj.setVehicle(resultSet.getInt("vehicle"));
	 * 
	 * obj.setDescript(resultSet.getString("descript"));
	 * 
	 * obj.setRowId(resultSet.getString("row_id")); }
	 */

}
