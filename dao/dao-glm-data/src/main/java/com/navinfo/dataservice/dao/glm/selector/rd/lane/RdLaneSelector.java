package com.navinfo.dataservice.dao.glm.selector.rd.lane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

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
			String sql = "SELECT lane_pid FROM rd_lane WHERE link_pid in (" + ids + ") and  u_record !=2 ";
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
				RdLane rdlane = (RdLane) this.loadById(resultSet.getInt("lane_pid"), false);
				lanes.add(rdlane);
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
	 * 通过Link查找车道信息 zhaokk 组装LINK对应的车道信息
	 * 
	 * @param linkPid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public JSONArray loadByLinks(List<Integer> linkPids, boolean isLock) throws Exception {
		JSONArray arrayResult = new JSONArray();
		List<Integer> pids = new ArrayList<Integer>();

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			String ids = org.apache.commons.lang.StringUtils.join(linkPids, ",");
			String sql = "SELECT ra.link_pid, ra.lane_pid,rl.geometry,rl.s_node_pid,rl.e_node_pid,rl.direct, rl.length FROM rd_lane ra,rd_link rl WHERE ra.link_pid = rl.link_pid and ra.link_pid in ("
					+ ids + ") and  rl.u_record !=2 and  ra.u_record !=2 ";

			sql += " order by ra.link_pid";
			if (isLock) {
				sql += " for update nowait";
			}
			pstmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

			resultSet = pstmt.executeQuery();
			JSONArray array = new JSONArray();
			JSONObject jsonObject = null;
			while (resultSet.next()) {
				// 初始 赋值rdlink值
				if (resultSet.isFirst()) {
					jsonObject = new JSONObject();
					this.setAttr(resultSet, pids, jsonObject);
				}
				// 合并相同link的车道信息
				if (pids.contains(resultSet.getInt("link_pid"))) {
					this.setRdlanesToArray(resultSet, array);
					if (resultSet.isLast()) {
						jsonObject.put("lanes", array);
						arrayResult.add(jsonObject);
					}

				} else {
					jsonObject.put("lanes", array);
					arrayResult.add(jsonObject);
					array = new JSONArray();
					this.setRdlanesToArray(resultSet, array);
					jsonObject = new JSONObject();
					this.setAttr(resultSet, pids, jsonObject);
					if (resultSet.isLast()) {
						jsonObject.put("lanes", array);
						arrayResult.add(jsonObject);
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return arrayResult;
	}

	/***
	 * 结果集赋值
	 * 
	 * @param resultSet
	 * @param jsonObject
	 * @throws SQLException
	 */
	private void setAttr(ResultSet resultSet, List<Integer> pids, JSONObject jsonObject) throws Exception {
		STRUCT struct = (STRUCT) resultSet.getObject("geometry");
		jsonObject.put("linkPid", resultSet.getInt("link_pid"));
		jsonObject.put("direct", resultSet.getInt("direct"));
		jsonObject.put("length", resultSet.getInt("length"));
		jsonObject.put("geometry", Geojson.spatial2Geojson(struct));
		jsonObject.put("sNodePid", resultSet.getInt("s_node_pid"));
		jsonObject.put("eNodePid", resultSet.getInt("e_node_pid"));
		pids.add(resultSet.getInt("link_pid"));
	}

	/***
	 * rdlane 加載array
	 * 
	 * @param resultSet
	 * @param array
	 * @throws Exception
	 */
	private void setRdlanesToArray(ResultSet resultSet, JSONArray array) throws Exception {
		RdLane slope = (RdLane) this.loadById(resultSet.getInt("lane_pid"), false);
		array.add(slope);
	}

	/**
	 * 根据linkPid查询该link上存在的影响详细车道的其他要素pid集合
	 * @param linkPid linkPid
	 * @param type 要素类型
	 * @param pidList 排除的要素类型
	 * @return 影响详细车道的其他要素pid集合
	 * @throws Exception
	 */
	public Map<Integer, List<Integer>> getLaneInfoByLinkPid(int linkPid,int level,List<Integer> pidList) throws Exception {
		Map<Integer, List<Integer>> laneInfo = new HashMap<Integer, List<Integer>>();
		
		StringBuilder sb = new StringBuilder(
				"WITH TMP1 AS (SELECT 21, PID, IN_LINK_PID AS LINK_PID FROM RD_TOLLGATE WHERE IN_LINK_PID = :1 AND U_RECORD != 2 AND PASSAGE_NUM >0 ");
		String pidNotInSql = " AND PID not in( "+StringUtils.getInteStr(pidList)+") ";
		if(level == 21 && CollectionUtils.isNotEmpty(pidList))
		{
			sb.append(pidNotInSql);
		}
		sb.append(" UNION SELECT 21, PID, OUT_LINK_PID AS LINK_PID FROM RD_TOLLGATE WHERE OUT_LINK_PID = :2 AND U_RECORD != 2 ");
		if(level == 21 && CollectionUtils.isNotEmpty(pidList))
		{
			sb.append(pidNotInSql);
		}
		sb.append(" ), TMP2 AS (SELECT 22, PID, IN_LINK_PID AS LINK_PID FROM RD_LANE_CONNEXITY WHERE IN_LINK_PID = :3 AND U_RECORD != 2 ");
		if(level == 22 && CollectionUtils.isNotEmpty(pidList))
		{
			sb.append(pidNotInSql);
		}
		sb.append(" ), TMP3 AS (SELECT 23, LINK_PID AS PID, LINK_PID AS LINK_PID FROM RD_LINK_FORM WHERE LINK_PID = :4 AND FORM_OF_WAY = 50 AND U_RECORD != 2 ");
		if(level == 23 && CollectionUtils.isNotEmpty(pidList))
		{
			sb.append(" AND LINK_PID not in( "+StringUtils.getInteStr(pidList)+") ");
		}
		sb.append(" ), TMP4 AS (SELECT 24, A.BRANCH_PID as PID, IN_LINK_PID AS LINK_PID FROM RD_BRANCH A, RD_BRANCH_DETAIL B WHERE A.IN_LINK_PID = :5 AND A.BRANCH_PID = B.BRANCH_PID AND B.BRANCH_TYPE = 0 ");
		if(level == 24 && CollectionUtils.isNotEmpty(pidList))
		{
			sb.append(" AND BRANCH_PID not in( "+StringUtils.getInteStr(pidList)+") ");
		}
		sb.append(" UNION SELECT 24, A.BRANCH_PID as PID, IN_LINK_PID AS LINK_PID FROM RD_BRANCH A, RD_BRANCH_DETAIL B WHERE A.OUT_LINK_PID = :6 AND A.BRANCH_PID = B.BRANCH_PID AND B.BRANCH_TYPE = 0 ");
		if(level == 24 && CollectionUtils.isNotEmpty(pidList))
		{
			sb.append(" AND BRANCH_PID not in( "+StringUtils.getInteStr(pidList)+") ");
		}
		sb.append(" ) SELECT * FROM TMP1 UNION SELECT * FROM TMP2 UNION SELECT * FROM TMP3 UNION SELECT * FROM TMP4 ");
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, linkPid);

			pstmt.setInt(2, linkPid);
			
			pstmt.setInt(3, linkPid);
			
			pstmt.setInt(4, linkPid);
			
			pstmt.setInt(5, linkPid);
			
			pstmt.setInt(6, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				int laneLevel = resultSet.getInt(1);
				
				List<Integer> infoList = laneInfo.get(level);
				
				if(infoList == null)
				{
					infoList = new ArrayList<>();
					
					laneInfo.put(laneLevel, infoList);
				}
				
				int pid = resultSet.getInt(2);
				
				infoList.add(pid);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return laneInfo;
	}
}
