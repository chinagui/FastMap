package com.navinfo.dataservice.engine.edit.edit.model.selector.rd.branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.engine.edit.edit.model.IRow;
import com.navinfo.dataservice.engine.edit.edit.model.ISelector;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.branch.RdBranchVia;
import com.navinfo.dataservice.engine.edit.edit.model.operator.rd.branch.RdBranchViaOperator;

public class RdBranchViaSelector implements ISelector {

	private Connection conn;

	public RdBranchViaSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		RdBranchVia via = new RdBranchVia();

		String sql = "select a.*,c.mesh_id from " + via.tableName()
				+ " a,rd_branch b,rd_link c where a.row_id=hextoraw('" +rowId +"') and a.u_record!=2 and a.branch_pid = b.branch_pid and b.in_link_pid = c.link_pid ";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				via.setBranchPid(resultSet.getInt("branch_pid"));

				via.setLinkPid(resultSet.getInt("link_pid"));

				via.setGroupId(resultSet.getInt("group_id"));

				via.setSeqNum(resultSet.getInt("seq_num"));

				via.setRowId(resultSet.getString("row_id"));
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

		return via;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from rd_branch_via where branch_pid=:1 and u_record!=:2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdBranchVia via = new RdBranchVia();

				via.setBranchPid(resultSet.getInt("branch_pid"));

				via.setLinkPid(resultSet.getInt("link_pid"));

				via.setGroupId(resultSet.getInt("group_id"));

				via.setSeqNum(resultSet.getInt("seq_num"));

				via.setRowId(resultSet.getString("row_id"));

				rows.add(via);
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
	
	
	public List<List<RdBranchVia>> loadRdBranchViaByLinkPid(
			int linkPid, boolean isLock) throws Exception {
		List<List<RdBranchVia>> list = new ArrayList<List<RdBranchVia>>();

		List<RdBranchVia> listVia = new ArrayList<RdBranchVia>();

		String sql = "select a.*,b.s_node_pid,b.e_node_pid,c.node_pid in_node_pid,d.mesh_id from rd_branch_via a,rd_link b,rd_branch c,rd_link d " +
				" where a.link_pid = b.link_pid and a.link_pid = :1 and a.branch_pid = c.branch_pid and c.in_link_pid = d.link_pid " +
				" order by a.branch_pid,a.seq_num  ";
		
		if (isLock){
			sql += " for update nowait ";
		}

		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, linkPid);

		ResultSet resultSet = pstmt.executeQuery();

		int preBranchPid = 0;

		boolean isChanged = false;

		int preSNodePid = 0;

		int preENodePid = 0;

		int viaSeqNum = 0;

		while (resultSet.next()) {
			RdBranchVia via = new RdBranchVia();

			int tmpBranchPid = resultSet.getInt("branch_pid");

			if (preBranchPid == 0) {
				preBranchPid = tmpBranchPid;
			} else if (preBranchPid != tmpBranchPid) {
				isChanged = true;

				preBranchPid = tmpBranchPid;
			}

			int tempLinkPid = resultSet.getInt("link_pid");

			if (tempLinkPid == linkPid) {
				viaSeqNum = resultSet.getInt("seq_num");

			} else {
				preSNodePid = resultSet.getInt("s_node_pid");

				preENodePid = resultSet.getInt("e_node_pid");
			}

			if (viaSeqNum == 0) {
				continue;
			}

			via.setBranchPid(resultSet.getInt("topology_id"));

			via.setLinkPid(resultSet.getInt("link_pid"));

			via.setGroupId(resultSet.getInt("group_id"));

			via.setSeqNum(resultSet.getInt("seq_num"));

			via.setRowId(resultSet.getString("row_id"));

			via.iseteNodePid(resultSet.getInt("e_node_pid"));

			via.isetsNodePid(resultSet.getInt("s_node_pid"));
			
			via.setMesh(resultSet.getInt("mesh_id"));

			if (!isChanged) {
				listVia.add(via);

			} else {

				RdBranchViaOperator op = new RdBranchViaOperator(
						conn, via);
				

				listVia = op.repaireViaDirect(listVia, preSNodePid,
						preENodePid, linkPid);

				list.add(listVia);

				listVia = new ArrayList<RdBranchVia>();

				listVia.add(via);

				isChanged = false;
			}

		}

		if (listVia.size() > 0) {
			RdBranchViaOperator op = new RdBranchViaOperator(conn,
					null);

			listVia = op.repaireViaDirect(listVia, preSNodePid, preENodePid,
					linkPid);

			list.add(listVia);
		}
		try {

			resultSet.close();

			pstmt.close();

		} catch (Exception e) {
			
		}

		return list;
	}
	


}
