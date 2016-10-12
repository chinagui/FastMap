package com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class RdLaneViaSelector extends AbstractSelector {

	private Connection conn;

	public RdLaneViaSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdLaneVia.class);
	}

	public List<List<Entry<Integer, RdLaneVia>>> loadRdLaneViaByLinkPid(int linkPid, boolean isLock) throws Exception {
		List<List<Entry<Integer, RdLaneVia>>> list = new ArrayList<List<Entry<Integer, RdLaneVia>>>();

		List<Entry<Integer, RdLaneVia>> listVia = new ArrayList<Entry<Integer, RdLaneVia>>();

		String sql = "select a.*, b.s_node_pid, b.e_node_pid,d.pid, d.node_pid in_node_pid,f.mesh_id   from rd_lane_via    a,    "
				+ "    rd_link               b,        rd_lane_connexity        d,        rd_lane_topology e,rd_link f "
				+ " where a.link_pid = b.link_pid    and a.topology_id = e.topology_id    and e.connexity_pid = d.pid    and exists (select null           from rd_lane_via c          where link_pid = :1   "
				+ "         and a.topology_id = c.topology_id) and d.in_link_pid = f.link_pid  order by a.topology_id, a.seq_num ";

		if (isLock) {
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

			ReflectionAttrUtils.executeResultSet(via, resultSet);

			int pid = resultSet.getInt("pid");

			if (!isChanged) {
				listVia.add(new AbstractMap.SimpleEntry(pid, via));
			} else {

				listVia = this.repaireViaDirect(listVia, preSNodePid, preENodePid, linkPid);

				list.add(listVia);

				listVia = new ArrayList<Entry<Integer, RdLaneVia>>();

				listVia.add(new AbstractMap.SimpleEntry(pid, via));

				isChanged = false;
			}

		}

		if (listVia.size() > 0) {
			listVia = this.repaireViaDirect(listVia, preSNodePid, preENodePid, linkPid);

			list.add(listVia);
		}
		DBUtils.closeResultSet(resultSet);
		DBUtils.closeStatement(pstmt);
		return list;
	}

	// 维护经过线方向
	public List<Entry<Integer, RdLaneVia>> repaireViaDirect(List<Entry<Integer, RdLaneVia>> vias, int preSNodePid,
			int preENodePid, int linkPid) {
		List<Entry<Integer, RdLaneVia>> newVias = new ArrayList<Entry<Integer, RdLaneVia>>();

		for (Entry<Integer, RdLaneVia> entry : vias) {

			RdLaneVia v = entry.getValue();

			if (v.getLinkPid() == linkPid) {

				if (preSNodePid != 0 && preENodePid != 0) {
					if (v.igetsNodePid() == preSNodePid || v.igetsNodePid() == preENodePid) {

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

	/**
	 * 计算经过线
	 * 
	 * @param inLinkPid 进入线
	 * @param nodePid 进入点
	 * @param outLinkPid 退出线
	 * @throws Exception
	 */
	public List<Integer> calViaLinks(int inLinkPid, int nodePid, int outLinkPid) throws Exception {

		String sql = "select * from table(package_utils.get_restrict_points(:1,:2,:3))";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		List<Integer> viaLinks = new ArrayList<Integer>();

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, inLinkPid);

			pstmt.setInt(2, nodePid);

			pstmt.setInt(3, outLinkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				String viaPath = resultSet.getString("via_path");

				if (viaPath != null) {

					String splits[] = viaPath.split(",");

					for (String s : splits) {
						if (!s.equals("")) {

							int viaPid = Integer.valueOf(s);

							if (viaPid == inLinkPid || viaPid == outLinkPid) {
								continue;
							}

							viaLinks.add(viaPid);
						}
					}

				}
			}
		} catch (Exception e) {
			return viaLinks;
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {
			}

		}
		return viaLinks;
	}
}
