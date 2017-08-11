package com.navinfo.dataservice.dao.glm.selector.rd.restrict;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

public class RdRestrictionViaSelector extends AbstractSelector {

	private Connection conn;

	public RdRestrictionViaSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdRestrictionVia.class);
	}

	public List<List<Entry<Integer, RdRestrictionVia>>> loadRestrictionViaByLinkPid(
			int linkPid, boolean isLock) throws Exception {
		List<List<Entry<Integer, RdRestrictionVia>>> list = new ArrayList<List<Entry<Integer, RdRestrictionVia>>>();

		List<Entry<Integer, RdRestrictionVia>> listVia = new ArrayList<Entry<Integer, RdRestrictionVia>>();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		String sql = "select a.*, b.s_node_pid, b.e_node_pid, d.pid, d.node_pid in_node_pid,f.mesh_id   from rd_restriction_via    a,    "
				+ "    rd_link               b,        rd_restriction        d,        "
				+ "rd_restriction_detail e ,rd_link f where a.link_pid = b.link_pid   "
				+ " and a.detail_id = e.detail_id    and e.restric_pid = d.pid  "
				+ "  and exists (select null           from rd_restriction_via c        "
				+ "  where link_pid = :1            "
				+ "and a.detail_id = c.detail_id) and d.in_link_pid = f.link_pid order by a.detail_id, a.seq_num";
		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

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

				int pid = resultSet.getInt("pid");

				if (!isChanged) {
					listVia.add(new AbstractMap.SimpleEntry(pid, via));

				} else {

					listVia = repaireViaDirect(listVia, preSNodePid,
							preENodePid, linkPid);

					list.add(listVia);

					listVia = new ArrayList<Entry<Integer, RdRestrictionVia>>();

					listVia.add(new AbstractMap.SimpleEntry(pid, via));

					isChanged = false;
				}

			}

			if (listVia.size() > 0) {
				listVia = this.repaireViaDirect(listVia, preSNodePid,
						preENodePid, linkPid);

				list.add(listVia);
			}

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	// 维护经过线方向
	public List<Entry<Integer, RdRestrictionVia>> repaireViaDirect(
			List<Entry<Integer, RdRestrictionVia>> vias, int preSNodePid,
			int preENodePid, int linkPid) {
		List<Entry<Integer, RdRestrictionVia>> newVias = new ArrayList<Entry<Integer, RdRestrictionVia>>();

		for (Entry<Integer, RdRestrictionVia> entry : vias) {
			RdRestrictionVia v = entry.getValue();

			if (v.getLinkPid() == linkPid) {

				if (preSNodePid != 0 && preENodePid != 0) {
					if (v.igetsNodePid() == preSNodePid
							|| v.igetsNodePid() == preENodePid) {

					} else {
						int tempPid = v.igetsNodePid();

						v.isetsNodePid(v.igeteNodePid());

						v.iseteNodePid(tempPid);
					}
				} else {
					if (v.igeteNodePid() == v.igetInNodePid()) {
						int tempPid = v.igetsNodePid();

						v.isetsNodePid(v.igeteNodePid());

						v.iseteNodePid(tempPid);
					}
				}
			}

			newVias.add(new AbstractMap.SimpleEntry(entry.getKey(), v));
		}

		return newVias;
	}
}
