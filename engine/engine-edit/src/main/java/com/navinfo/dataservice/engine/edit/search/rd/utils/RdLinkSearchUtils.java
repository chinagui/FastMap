package com.navinfo.dataservice.engine.edit.search.rd.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
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

			System.out.println(sql);

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

	/*
	 * @查询上下线分离关联的link 1.关联link数量不能超过999 2.关联查找link 必须联通link方向一致
	 * 3.关联link必须是夹角最小的一个link
	 * 
	 * @param cuurentLinkPid 当前link 当前方向node
	 * 
	 * @return 查找所有联通link
	 */
	public List<RdLink> getNextTrackLinks(int cuurentLinkPid,
			int cruuentNodePidDir) throws Exception {
		RdLinkSelector linkSelector = new RdLinkSelector(conn);
		List<RdLink> tracks = new ArrayList<RdLink>();
		int fristNodePid =0;
		// 添加当前选中的link
		RdLink fristLink = (RdLink) linkSelector.loadByIdOnlyRdLink(
				cuurentLinkPid, true);
		if(fristLink.getsNodePid() ==cruuentNodePidDir){
			fristNodePid =fristLink.geteNodePid();
		}else{
			fristNodePid =fristLink.getsNodePid();
		}
		
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
			if(link.getsNodePid() ==fristNodePid||link.geteNodePid() ==fristNodePid){
				break;
			}
			if (tracks.size() >= 99 || tracks.contains(link)) {
				break;
			}
			tracks.add(link);
			// 赋值查找下一组联通links
			nextLinks = linkSelector.loadTrackLink(cuurentLinkPid,
					cruuentNodePidDir, true);
		}
		return tracks;
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
