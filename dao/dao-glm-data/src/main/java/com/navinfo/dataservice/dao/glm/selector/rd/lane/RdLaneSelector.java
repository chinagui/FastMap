package com.navinfo.dataservice.dao.glm.selector.rd.lane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

/***
 * 
 * @author zhaokk
 * 
 */
public class RdLaneSelector extends AbstractSelector {

	private Connection conn;

	public RdLaneSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdLane.class);
	}

	/***
	 * 
	 * 通过Link查找车道信息 0是查询link上所有车道信息
	 * 
	 * @param linkPid
	 * @param isLock
	 * @param laneDir
	 *            车道方向 1 无 2 顺方向 3 逆方向
	 * @return
	 * @throws Exception
	 */
	public List<RdLane> loadByLink(int linkPid, int laneDir, boolean isLock) throws Exception {

		List<RdLane> lanes = new ArrayList<RdLane>();

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			String sql = "SELECT lane_pid FROM rd_lane WHERE link_pid =:1 and  u_record !=2 ";
			if (laneDir != 0) {
				sql += " and lane_dir = :2 ";
			}
			sql += " order by seq_num";
			if (isLock) {
				sql += " for update nowait";
			}

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);
			if (laneDir != 0) {
				pstmt.setInt(2, laneDir);
			}

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdLane slope = (RdLane) this.loadById(resultSet.getInt("lane_pid"), false);
				lanes.add(slope);
			}

			return lanes;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/***
	 * 
	 * 通过Link查找车道信息 0是查询link上所有车道信息
	 * 
	 * @param linkPid
	 * @param isLock
	 * @param laneDir
	 *            车道方向 1 无 2 顺方向 3 逆方向
	 * @return
	 * @throws Exception
	 */
	public List<RdLane> loadByLinks(List<Integer> linkPids, int laneDir, boolean isLock) throws Exception {

		List<RdLane> lanes = new ArrayList<RdLane>();

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			String ids = org.apache.commons.lang.StringUtils.join(linkPids, ",");
			String sql = "SELECT lane_pid FROM rd_lane WHERE link_pid in ("+ ids+") and  u_record !=2 ";
			if (laneDir != 0) {
				sql += " and lane_dir = :1 ";
			}
			sql += " order by link_pid";
			if (isLock) {
				sql += " for update nowait";
			}
			pstmt = conn.prepareStatement(sql);
			if (laneDir != 0) {
				pstmt.setInt(1, laneDir);
			}

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdLane slope = (RdLane) this.loadById(resultSet.getInt("lane_pid"), false);
				lanes.add(slope);
			}

			return lanes;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	public static void main(String[] args) {
		JSONArray condition = new JSONArray();
		condition.add(23);
		condition.add(24);
		List<Integer> pids = (List<Integer>)JSONArray.toCollection(condition, String.class);
		System.out.println(pids);
	}


}
