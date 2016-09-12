package com.navinfo.dataservice.dao.glm.selector.rd.lane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoDetail;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoVia;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

/***
 * 
 * @author zhaokk
 * 
 */
public class RdLaneTopoDetailSelector extends AbstractSelector {

	private Connection conn;

	public RdLaneTopoDetailSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdLaneTopoDetail.class);
	}

	/***
	 * 
	 * 通过车道信息查询车道联通信息
	 */
	public List<IRow> loadByLanePid(int lanePid, boolean isLock)
			throws Exception {
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		List<Integer> topoPids = new ArrayList<Integer>();

		try {
			String sql = "SELECT topo_id FROM rd_lane_topo_detail WHERE (out_lane_pid =:1 or in_lane_pid = :2) and u_record !=2";

			if (isLock) {
				sql += " for update nowait";
			}

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, lanePid);
			pstmt.setInt(2, lanePid);
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				topoPids.add(resultSet.getInt("topo_id"));
			}
			if (topoPids.size() > 0) {
				return new RdLaneTopoDetailSelector(conn).loadByIds(topoPids,
						true, true);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return new ArrayList<IRow>();
	}

	/***
	 * 
	 * 通过LINK信息查询车道联通信息
	 */
	public List<IRow> loadByLinkPids(List<Integer> linkPids, int nodePid,
			boolean isLock) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		List<Integer> topoPids = new ArrayList<Integer>();
		List<Integer> viaLinkPids = new ArrayList<Integer>();
		List<IRow> topos = new ArrayList<IRow>();

		try {
			String sql = "SELECT topo_id FROM rd_lane_topo_detail WHERE IN_LINK_PID = :1  and NODE_PID = :2 and u_record !=2";

			if (isLock) {
				sql += " for update nowait";
			}

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPids.get(0));
			pstmt.setInt(2, nodePid);
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				topoPids.add(resultSet.getInt("topo_id"));
			}
			if (topoPids.size() > 0) {
				List<IRow> rows = new RdLaneTopoDetailSelector(conn).loadByIds(
						topoPids, true, true);
				for (IRow row : rows) {
					RdLaneTopoDetail detail = (RdLaneTopoDetail) row;
					if (detail.getTopoVias().size() > 0) {
						List<IRow> vias = detail.getTopoVias();
						for (IRow via : vias) {
							RdLaneTopoVia laneTopoVia = (RdLaneTopoVia) via;
							viaLinkPids.add(laneTopoVia.getViaLinkPid());
						}
						if (linkPids.contains(detail.getOutLinkPid())
								&& linkPids.containsAll(viaLinkPids)) {
							topos.add(row);
						}
					} else {
						if (linkPids.contains(detail.getOutLinkPid())) {
							topos.add(row);
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return topos;
	}

}
