package com.navinfo.dataservice.engine.edit.search.rd.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;

import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;

/**
 * 
 * @author zhaokk RDLINK 复杂查询公共类
 * 
 */
public class RdLinkSearchUtils {
	private Connection conn;

	public RdLinkSearchUtils(Connection conn) throws Exception {
		this.conn = conn;
	}

	/**
	 * 查询link串，批量修改限速使用。
	 * 
	 * @param linkPid
	 *            关联link
	 * @param direct
	 *            限速方向
	 * @param queryType
	 *            限速类型：1点限速，2线限速
	 * @return
	 * @throws Exception
	 */
	public List<Integer> getConnectLinks(int linkPid, int direct,
			String queryType) throws Exception {

		List<Integer> nextLinkPids = new ArrayList<Integer>();

		RdLinkSelector linkSelector = new RdLinkSelector(conn);

		RdLink targetLink = (RdLink) linkSelector.loadByIdOnlyRdLink(linkPid,
				false);

		if (targetLink == null) {

			return nextLinkPids;
		}

		int nodePid = 0;

		if (direct == 2) {

			nodePid = targetLink.geteNodePid();

		} else if (direct == 3) {

			nodePid = targetLink.getsNodePid();
		}

		getConnectLink(targetLink, nodePid, nextLinkPids, queryType);

		return nextLinkPids;
	}

	public JSONArray getRdLinkSpeedlimit(List<Integer> linkPids)
			throws Exception {
		AbstractSelector speedlimitSelector = new AbstractSelector(
				RdLinkSpeedlimit.class, conn);

		List<IRow> rows = speedlimitSelector
				.loadRowsByParentIds(linkPids, true);

		JSONArray array = new JSONArray();

		for (IRow row : rows) {

			RdLinkSpeedlimit speedlimit = (RdLinkSpeedlimit) row;

			if (speedlimit.getSpeedType() == 0) {

				array.add(speedlimit.Serialize(ObjLevel.FULL));
			}
		}

		return array;
	}

	private void getConnectLink(RdLink targetLink, int connectNodePid,
			List<Integer> linkPids, String type) throws Exception {

		if (!linkPids.contains(targetLink.getPid())) {

			linkPids.add(targetLink.getPid());
		} else {
			return;
		}
		if (linkPids.size() > 998) {

			return;
		}

		String sql = "WITH TMP1 AS (SELECT LINK_PID, S_NODE_PID, E_NODE_PID, DIRECT, GEOMETRY FROM RD_LINK T WHERE ((S_NODE_PID = :1 AND DIRECT = 2) OR (E_NODE_PID = :2 AND DIRECT = 3) OR ((S_NODE_PID = :3 OR E_NODE_PID = :4) AND DIRECT = 1)) AND U_RECORD != 2)  SELECT B.*, (SELECT COUNT(1) FROM RD_SPEEDLIMIT S WHERE S.LINK_PID = B.LINK_PID AND S.U_RECORD != 2 AND (B.DIRECT = S.DIRECT OR (B.DIRECT = 1 AND ((B.S_NODE_PID = :5 AND S.DIRECT = 2) OR (B.E_NODE_PID = :6 AND S.DIRECT = 3))))) RDSPEEDLIMIT, (SELECT COUNT(1) FROM RD_LINK_SPEEDLIMIT L WHERE L.LINK_PID = B.LINK_PID AND L.U_RECORD != 2 AND L.SPEED_TYPE = 0 AND ((B.DIRECT = 2 AND L.FROM_SPEED_LIMIT > 0) OR (B.DIRECT = 3 AND L.TO_SPEED_LIMIT > 0) OR (B.DIRECT = 1 AND ((B.S_NODE_PID = :7 AND L.FROM_SPEED_LIMIT > 0) OR (B.E_NODE_PID = :8 AND L.TO_SPEED_LIMIT > 0))))) RDLINKSPEEDLIMIT FROM TMP1 B";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, connectNodePid);
			pstmt.setInt(2, connectNodePid);
			pstmt.setInt(3, connectNodePid);
			pstmt.setInt(4, connectNodePid);
			pstmt.setInt(5, connectNodePid);
			pstmt.setInt(6, connectNodePid);
			pstmt.setInt(7, connectNodePid);
			pstmt.setInt(8, connectNodePid);

			resultSet = pstmt.executeQuery();

			int speedlimitCount = -1;

			int linkspeedlimitCount = -1;

			RdLink nextLink = new RdLink();

			double minAngle = Double.MAX_VALUE;

			LineSegment targetlineSegment = getLineSegment(targetLink,
					connectNodePid);

			while (resultSet.next()) {

				int linkPid = resultSet.getInt("LINK_PID");

				if (linkPid == targetLink.getPid()) {
					continue;
				}

				Geometry linkGeometry = GeoTranslator.struct2Jts(
						(STRUCT) resultSet.getObject("GEOMETRY"), 100000, 0);

				int sNodePid = resultSet.getInt("S_NODE_PID");

				int eNodePid = resultSet.getInt("E_NODE_PID");

				RdLink link = new RdLink();

				link.setPid(linkPid);

				link.setGeometry(linkGeometry);

				link.setsNodePid(sNodePid);

				link.seteNodePid(eNodePid);

				LineSegment lineSegment = getLineSegment(link, connectNodePid);

				double angle = Math.abs(180 - AngleCalculator
						.getConnectLinksAngle(targetlineSegment, lineSegment));

				if (angle < minAngle) {

					minAngle = angle;

					nextLink = link;

					speedlimitCount = resultSet.getInt("RDSPEEDLIMIT");

					linkspeedlimitCount = resultSet.getInt("RDLINKSPEEDLIMIT");
				}
			}

			if (speedlimitCount == -1 || linkspeedlimitCount == -1) {
				return;
			}

			if (type.equals("RDSPEEDLIMIT") && speedlimitCount > 0) {
				return;
			}

			if (type.equals("RDLINKSPEEDLIMIT")
					&& (speedlimitCount > 0 || linkspeedlimitCount > 0)) {
				return;
			}

			int targetNodePid = nextLink.getsNodePid() == connectNodePid ? nextLink
					.geteNodePid() : nextLink.getsNodePid();

			getConnectLink(nextLink, targetNodePid, linkPids, type);

		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

	}

	/***
	 * @查询上下线分离关联的link 1.关联link数量不能超过maxNum 2.关联查找link必须联通link方向一致
	 *                 3.关联link必须是夹角最小的一个link
	 * @param cuurentLinkPid
	 * @param cruuentNodePidDir
	 * @param maxNum
	 * @return 查找所有联通link
	 * @throws Exception
	 */
	public List<RdLink> getNextTrackLinks(int cuurentLinkPid,
			int cruuentNodePidDir, int maxNum) throws Exception {
		RdLinkSelector linkSelector = new RdLinkSelector(conn);
		List<RdLink> tracks = new ArrayList<RdLink>();
		Set<Integer> nodes = new HashSet<Integer>();

		// 添加当前选中的link
		RdLink fristLink = (RdLink) linkSelector.loadByIdOnlyRdLink(
				cuurentLinkPid, true);
		nodes.add(fristLink.getsNodePid());
		nodes.add(fristLink.geteNodePid());
		tracks.add(fristLink);
		// 查找当前link联通的links
		List<RdLink> nextLinks = linkSelector.loadTrackLink(cuurentLinkPid,
				cruuentNodePidDir, true);
		while (nextLinks.size() > 0) {
			// 加载当前link
			RdLink currentLink = (RdLink) linkSelector.loadById(cuurentLinkPid,
					true);
			// 计算当前link直线的几何属性
			LineSegment currentLinklineSegment = getLineSegment(currentLink,
					cruuentNodePidDir);
			// 如果当前link的起点等于当前联通方向
			// 取当前link的最后两个形状点组成直线

			Map<Double, RdLink> map = new HashMap<Double, RdLink>();
			for (RdLink ad : nextLinks) {
				// 获取联通links直线的几何
				LineSegment nextLinklineSegment = getLineSegment(ad,
						cruuentNodePidDir);
				// 计算当前线直线和联通直线夹角 选出当前线延长线夹角最小
				double minAngle = Math.abs(180 - AngleCalculator
						.getConnectLinksAngle(currentLinklineSegment,
								nextLinklineSegment));

				if (map.size() > 0) {
					if (map.keySet().iterator().next() > minAngle) {
						map.clear();
						map.put(minAngle, ad);
					}

				} else {
					map.put(minAngle, ad);
				}
			}
			// 获取联通线中夹角最小的link
			// 赋值给当前cuurentLinkPid 确定方向
			RdLink link = map.values().iterator().next();
			cuurentLinkPid = link.getPid();
			cruuentNodePidDir = (cruuentNodePidDir == link.getsNodePid()) ? link
					.geteNodePid() : link.getsNodePid();
			if (nodes.contains(cruuentNodePidDir)) {
				break;
			}
			if (tracks.size() >= maxNum || tracks.contains(link)) {
				break;
			}
			nodes.add(cruuentNodePidDir);
			tracks.add(link);
			// 赋值查找下一组联通links
			nextLinks = linkSelector.loadTrackLink(cuurentLinkPid,
					cruuentNodePidDir, true);
		}
		return tracks;
	}

	/***
	 * @推荐坡度查询的link接续 沿着坡度点到坡度退出线的方向追踪计算，追踪至退出link和接续link的长度总和大于100米小于150米处停止；
	 *                如果按照link既有的节点计算link长度大于150米，则在长度总和为130米处提示打断点位，
	 *                确认后在130米的提示点位处自动打断
	 *                按照上述方法追踪接续link的过程中，如果在为满足总和长度距离要求之前遇到了挂接
	 *                ，则停止追踪，将目前追踪到的link/link串作为该坡度的接续link；
	 *                如果退出link挂接了两条或两条以上的link（10级路不计算挂接个数）则不推荐接续link
	 * @author zhaokk
	 * @param cuurentLinkPid
	 * @param cruuentNodePidDir
	 * @param maxNum
	 * @param length
	 *            退出线的长度
	 * @return 查找所有联通link
	 * @throws Exception
	 */
	public List<RdLink> getNextLinksForSlope(double length, int cuurentLinkPid,
			int cruuentNodePidDir) throws Exception {
		RdLinkSelector linkSelector = new RdLinkSelector(conn);
		List<RdLink> tracks = new ArrayList<RdLink>();
		List<RdLink> resultLinks = new ArrayList<RdLink>();
		Set<Integer> nodes = new HashSet<Integer>();

		// 添加当前选中的link
		RdLink fristLink = (RdLink) linkSelector.loadByIdOnlyRdLink(
				cuurentLinkPid, true);
		nodes.add(fristLink.getsNodePid());
		nodes.add(fristLink.geteNodePid());
		// 查找当前link联通的links
		List<RdLink> nextLinks = linkSelector.loadTrackLink(cuurentLinkPid,
				cruuentNodePidDir, true);

		// 10级路不计算挂接个数
		for (RdLink link : nextLinks) {
			if (link.getKind() < 10) {
				resultLinks.add(link);
			}
		}
		while (resultLinks.size() == 1) {
			RdLink currentLink = resultLinks.get(0);
			if (this.getLinksLength(tracks) + currentLink.getLength() + length > 100) {
				tracks.add(currentLink);
				break;
			}
			tracks.add(currentLink);

			// 计算
			cuurentLinkPid = currentLink.getPid();
			cruuentNodePidDir = (cruuentNodePidDir == currentLink.getsNodePid()) ? currentLink
					.geteNodePid() : currentLink.getsNodePid();
			// 防止闭环
			if (nodes.contains(cruuentNodePidDir)) {
				break;
			}

			nodes.add(cruuentNodePidDir);
			// 赋值查找下一组联通links
			resultLinks = linkSelector.loadTrackLink(cuurentLinkPid,
					cruuentNodePidDir, true);
		}
		return tracks;
	}

	/***
	 * 计算link串的长度
	 * 
	 * @param links
	 * @return
	 */
	private double getLinksLength(List<RdLink> links) {
		double length = 0.0;
		if (links != null && links.size() > 0) {
			for (RdLink link : links) {
				length += link.getLength();
			}
		}
		return length;
	}

	/**
	 * 获取link指定端点处的直线几何
	 * 
	 * @param link
	 *            线
	 * @param nodePidDir
	 *            指定端点
	 * @return 以指定端点为起点的直线几何
	 */
	private LineSegment getLineSegment(RdLink link, int nodePidDir) {
		LineSegment lineSegment = null;
		if (link.getsNodePid() == nodePidDir) {
			lineSegment = new LineSegment(
					link.getGeometry().getCoordinates()[0], link.getGeometry()
							.getCoordinates()[1]);
		}
		if (link.geteNodePid() == nodePidDir) {
			lineSegment = new LineSegment(
					link.getGeometry().getCoordinates()[link.getGeometry()
							.getCoordinates().length - 2], link.getGeometry()
							.getCoordinates()[link.getGeometry()
							.getCoordinates().length - 1]);
		}
		return lineSegment;
	}
}
