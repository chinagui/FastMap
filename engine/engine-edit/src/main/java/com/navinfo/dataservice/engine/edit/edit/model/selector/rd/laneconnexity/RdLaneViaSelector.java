package com.navinfo.dataservice.engine.edit.edit.model.selector.rd.laneconnexity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.engine.edit.edit.model.IRow;
import com.navinfo.dataservice.engine.edit.edit.model.ISelector;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.engine.edit.edit.model.operator.rd.laneconnexity.RdLaneViaOperator;

public class RdLaneViaSelector implements ISelector {

	private Connection conn;

	public RdLaneViaSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		RdLaneVia via = new RdLaneVia();

		String sql = "select * from " + via.tableName()
				+ " where row_id=hextoraw(:1) and u_record!=2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, rowId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				via.setTopologyId(resultSet.getInt("topology_id"));

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

		String sql = "select * from rd_lane_via where topology_id=:1 and u_record!=:2 order by seq_num";

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

				RdLaneVia via = new RdLaneVia();

				via.setTopologyId(resultSet.getInt("topology_id"));

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

	
	public List<List<RdLaneVia>> loadRdLaneViaByLinkPid(
			int linkPid, boolean isLock) throws Exception {
		List<List<RdLaneVia>> list = new ArrayList<List<RdLaneVia>>();

		List<RdLaneVia> listVia = new ArrayList<RdLaneVia>();

//		String sql = "select a.*, b.s_node_pid, b.e_node_pid, d.node_pid in_node_pid   from rd_lane_via    a,    " +
//				"    rd_link               b,        rd_lane_connexity        d,        rd_lane_topology e " +
//				" where a.link_pid = b.link_pid    and a.topology_id = e.topology_id    and e.connexity_pid = d.pid    and exists (select null           from rd_lane_via c          where link_pid = :1   " +
//				"         and a.topology_id = c.topology_id)  order by a.topology_id, a.seq_num ";
		
		String sql = "select a.*, b.s_node_pid, b.e_node_pid, d.node_pid in_node_pid,f.mesh_id   from rd_lane_via    a,    " +
				"    rd_link               b,        rd_lane_connexity        d,        rd_lane_topology e,rd_link f " +
				" where a.link_pid = b.link_pid    and a.topology_id = e.topology_id    and e.connexity_pid = d.pid    and exists (select null           from rd_lane_via c          where link_pid = :1   " +
				"         and a.topology_id = c.topology_id) and d.in_link_pid = f.link_pid  order by a.topology_id, a.seq_num ";
		
		if (isLock){
			sql += " for update nowait ";
		}

		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, linkPid);

		ResultSet resultSet = pstmt.executeQuery();

		int preTopoId = 0;

		boolean isChanged = false;

		int preSNodePid = 0;

		int preENodePid = 0;

		int viaSeqNum = 0;

		while (resultSet.next()) {
			RdLaneVia via = new RdLaneVia();

			int tmpTopoId = resultSet.getInt("topology_id");

			if (preTopoId == 0) {
				preTopoId = tmpTopoId;
			} else if (preTopoId != tmpTopoId) {
				isChanged = true;

				preTopoId = tmpTopoId;
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

			via.setTopologyId(resultSet.getInt("topology_id"));

			via.setLinkPid(resultSet.getInt("link_pid"));

			via.setGroupId(resultSet.getInt("group_id"));

			via.setSeqNum(resultSet.getInt("seq_num"));

			via.setRowId(resultSet.getString("row_id"));

			via.iseteNodePid(resultSet.getInt("e_node_pid"));

			via.isetsNodePid(resultSet.getInt("s_node_pid"));

			via.isetInNodePid(resultSet.getInt("in_node_pid"));
			
			via.setMesh(resultSet.getInt("mesh_id"));

			if (!isChanged) {
				listVia.add(via);

			} else {

				RdLaneViaOperator op = new RdLaneViaOperator(
						conn, via);

				listVia = op.repaireViaDirect(listVia, preSNodePid,
						preENodePid, linkPid);

				list.add(listVia);

				listVia = new ArrayList<RdLaneVia>();

				listVia.add(via);

				isChanged = false;
			}

		}

		if (listVia.size() > 0) {
			RdLaneViaOperator op = new RdLaneViaOperator(conn,
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
