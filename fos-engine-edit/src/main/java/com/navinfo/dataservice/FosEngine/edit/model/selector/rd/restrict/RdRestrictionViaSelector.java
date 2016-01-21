package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.restrict;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.FosEngine.edit.model.operator.rd.restrict.RdRestrictionViaOperator;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;

public class RdRestrictionViaSelector implements ISelector {

	private static Logger logger = Logger
			.getLogger(RdRestrictionViaSelector.class);

	private Connection conn;

	public RdRestrictionViaSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		RdRestrictionVia via = new RdRestrictionVia();

		String sql = "select * from " + via.tableName()
				+ " where row_id=:1 and u_record!=2";

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

				via.setDetailId(resultSet.getInt("detail_id"));

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

		String sql = "select * from rd_restriction_via where detail_id=:1 and u_record!=:2 order by seq_num";

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

				RdRestrictionVia via = new RdRestrictionVia();

				via.setDetailId(resultSet.getInt("detail_id"));

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

	public List<List<RdRestrictionVia>> loadRestrictionViaByLinkPid(
			int linkPid, boolean isLock) throws Exception {
		List<List<RdRestrictionVia>> list = new ArrayList<List<RdRestrictionVia>>();

		List<RdRestrictionVia> listVia = new ArrayList<RdRestrictionVia>();

//		String sql = "select a.*, b.s_node_pid, b.e_node_pid, d.node_pid in_node_pid   from rd_restriction_via    a,    "
//				+ "    rd_link               b,        rd_restriction        d,        "
//				+ "rd_restriction_detail e  where a.link_pid = b.link_pid   "
//				+ " and a.detail_id = e.detail_id    and e.restric_pid = d.pid  "
//				+ "  and exists (select null           from rd_restriction_via c        "
//				+ "  where link_pid = :1            "
//				+ "and a.detail_id = c.detail_id)  order by a.detail_id, a.seq_num";
		
		
		String sql = "select a.*, b.s_node_pid, b.e_node_pid, d.node_pid in_node_pid,f.mesh_id   from rd_restriction_via    a,    "
				+ "    rd_link               b,        rd_restriction        d,        "
				+ "rd_restriction_detail e ,rd_link f where a.link_pid = b.link_pid   "
				+ " and a.detail_id = e.detail_id    and e.restric_pid = d.pid  "
				+ "  and exists (select null           from rd_restriction_via c        "
				+ "  where link_pid = :1            "
				+ "and a.detail_id = c.detail_id) and d.in_link_pid = f.link_pid order by a.detail_id, a.seq_num";

		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, linkPid);

		ResultSet resultSet = pstmt.executeQuery();

		int preDetailId = 0;

		boolean isChanged = false;

		int preSNodePid = 0;

		int preENodePid = 0;

		int viaSeqNum = 0;

		while (resultSet.next()) {
			RdRestrictionVia via = new RdRestrictionVia();

			int tempDetailId = resultSet.getInt("detail_id");

			if (preDetailId == 0) {
				preDetailId = tempDetailId;
			} else if (preDetailId != tempDetailId) {
				isChanged = true;

				preDetailId = tempDetailId;
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

			via.setDetailId(tempDetailId);

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

				RdRestrictionViaOperator op = new RdRestrictionViaOperator(
						conn, via);

				listVia = op.repaireViaDirect(listVia, preSNodePid,
						preENodePid, linkPid);

				list.add(listVia);

				listVia = new ArrayList<RdRestrictionVia>();

				listVia.add(via);

				isChanged = false;
			}

		}

		if (listVia.size() > 0) {
			RdRestrictionViaOperator op = new RdRestrictionViaOperator(conn,
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
