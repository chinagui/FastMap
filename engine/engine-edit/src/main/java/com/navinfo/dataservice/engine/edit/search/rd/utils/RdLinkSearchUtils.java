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
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.variablespeed.RdVariableSpeedSelector;
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

	public final int variableSpeedNextLinkCount = 99;

	RdLinkSelector linkSelector = null;

	RdVariableSpeedSelector variableSpeedSelector = null;

	/**
	 * 推荐可变限速接续link。
	 * 
	 * @param linkPid
	 *            link
	 * @param nodePid
	 *            方向点
	 * @return
	 * @throws Exception
	 */
	public List<RdLink> variableSpeedNextLinks(int linkPid, int nodePid)
			throws Exception {

		// 初始化查询类
		linkSelector = new RdLinkSelector(conn);

		variableSpeedSelector = new RdVariableSpeedSelector(this.conn);

		List<Integer> nextLinkPids = new ArrayList<Integer>();

		RdLink link = (RdLink) linkSelector.loadById(linkPid, true, true);

		Map<Integer, RdLink> linkStorage = variableSpeedNextLinks(nextLinkPids,
				linkPid, nodePid, link.getMeshId());

		List<RdLink> links = new ArrayList<RdLink>();

		for (int pid : nextLinkPids) {

			if (linkStorage.containsKey(pid)) {

				links.add(linkStorage.get(pid));
			}
		}

		return links;
	}

	/**
	 * 推荐可变限速接续link。
	 * 
	 * @param preLinkPid
	 *            link
	 * @param preNodePid
	 *            方向点
	 * @return
	 * @throws Exception
	 */
	private Map<Integer, RdLink> variableSpeedNextLinks(
			List<Integer> nextLinkPids, int preLinkPid, int preNodePid,
			int meshId) throws Exception {

		Map<Integer, RdLink> linkStorage = new HashMap<Integer, RdLink>();

		List<RdLink> links = linkSelector.loadByNodePidOnlyRdLink(preNodePid,
				false);

		List<RdLink> linksTmp = new ArrayList<RdLink>();

		for (RdLink link : links) {

			if (meshId != link.getMeshId()) {
				continue;
			}

			// 特殊交通类型不可作为可变限速的接续link；
			if (1 == link.getSpecialTraffic()) {
				continue;
			}

			if (preLinkPid == link.getPid()) {
				continue;
			}

			if (link.getDirect() == 0) {
				continue;
			}
			if (link.getDirect() == 2 && link.geteNodePid() == preNodePid) {
				continue;
			}
			if (link.getDirect() == 3 && link.getsNodePid() == preNodePid) {
				continue;
			}

			int kind = link.getKind();

			if (kind != 8 && kind != 9 && kind != 10 && kind != 11
					&& kind != 13) {
				linksTmp.add(link);
			}
		}
		if (linksTmp.size() != 1) {
			return linkStorage;
		}

		RdLink linkTmp = linksTmp.get(0);

		if (!isVariableSpeedLink(linkTmp, preNodePid)) {
			return linkStorage;
		}

		if (nextLinkPids.contains(linkTmp.getPid())) {
			return linkStorage;
		}

		nextLinkPids.add(linkTmp.getPid());

		linkStorage.put(linkTmp.getPid(), linkTmp);

		if (nextLinkPids.size() >= variableSpeedNextLinkCount) {
			return linkStorage;
		}

		int nextNodePid = preNodePid == linkTmp.getsNodePid() ? linkTmp
				.geteNodePid() : linkTmp.getsNodePid();

		Map<Integer, RdLink> nextStorage = variableSpeedNextLinks(nextLinkPids,
				linkTmp.getPid(), nextNodePid, meshId);

		linkStorage.putAll(nextStorage);

		return linkStorage;
	}

	/**
	 * 判断link能否做可变限速的接续线
	 * 
	 * @param link
	 * @param nodePid
	 * @return
	 * @throws Exception
	 */
	private boolean isVariableSpeedLink(RdLink link, int nodePid)
			throws Exception {

		List<IRow> forms = new AbstractSelector(RdLinkForm.class, conn)
				.loadRowsByParentId(link.getPid(), true);

		for (IRow row : forms) {

			RdLinkForm form = (RdLinkForm) row;

			// 特殊交通类型、交叉口内道路的LINK不可作为可变限速的接续link
			if (form.getFormOfWay() == 33 || form.getFormOfWay() == 50) {
				return false;
			}
		}

		int nextNodePid = nodePid == link.getsNodePid() ? link.geteNodePid()
				: link.getsNodePid();

		List<RdVariableSpeed> variableSpeeds = variableSpeedSelector
				.loadRdVariableSpeedByParam(link.getPid(), nextNodePid, null,
						true);

		if (variableSpeeds.size() > 0) {
			return false;
		}

		return true;
	}

	/**
	 * 查询link串，批量修改限速使用。
	 * 
	 * @param linkPid
	 *            关联link
	 * @param direct
	 *            限速方向
	 * @param queryType
	 *            限速类型：1:RDSPEEDLIMIT(普通点限速);2：RDSPEEDLIMIT_DEPENDENT(条件点限速)
	 * @return
	 * @throws Exception
	 */
	public List<Integer> getConnectLinks(int linkPid, int direct,
			int speedDependnt) throws Exception {

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

		getConnectLink(targetLink, nodePid, nextLinkPids,  speedDependnt);

		return nextLinkPids;
	}	
	
	private void getConnectLink(RdLink targetLink, int connectNodePid,
			List<Integer> linkPids, int speedDependnt) throws Exception {

		if (!linkPids.contains(targetLink.getPid())) {

			linkPids.add(targetLink.getPid());
		} else {
			return;
		}
		if (linkPids.size() > 29) {

			return;
		}

		String sql = "WITH TMP1 AS (SELECT LINK_PID, S_NODE_PID, E_NODE_PID, DIRECT, GEOMETRY FROM RD_LINK T WHERE ((S_NODE_PID = :1 AND DIRECT = 2) OR (E_NODE_PID = :2 AND DIRECT = 3) OR ((S_NODE_PID = :3 OR E_NODE_PID = :4) AND DIRECT = 1)) AND U_RECORD != 2) SELECT B.*, (SELECT COUNT(1) FROM RD_SPEEDLIMIT S WHERE S.LINK_PID = B.LINK_PID AND S.U_RECORD != 2 AND (B.DIRECT = S.DIRECT OR (B.DIRECT = 1 AND ((B.S_NODE_PID = :5 AND S.DIRECT = 2) OR (B.E_NODE_PID = :6 AND S.DIRECT = 3)))) ";

		if (speedDependnt >= 0) {

			sql += " AND S.SPEED_TYPE = 3 ";
		}
		
		sql += "  ) RDSPEEDLIMIT FROM TMP1 B ";

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

			resultSet = pstmt.executeQuery();

			int speedlimitCount = -1;

			RdLink nextLink = null;

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
				}
			}

			if (speedlimitCount > 0 || nextLink == null) {
				return;
			}

			int targetNodePid = nextLink.getsNodePid() == connectNodePid ? nextLink
					
					.geteNodePid() : nextLink.getsNodePid();

			getConnectLink(nextLink, targetNodePid, linkPids, speedDependnt);

		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}
	}

	public JSONArray getRdLinkSpeedlimit(List<Integer> linkPids,
			int speedDependnt) throws Exception {

		AbstractSelector speedlimitSelector = new AbstractSelector(
				RdLinkSpeedlimit.class, conn);

		List<IRow> rows = speedlimitSelector
				.loadRowsByParentIds(linkPids, true);

		JSONArray array = new JSONArray();

		for (IRow row : rows) {

			RdLinkSpeedlimit speedlimit = (RdLinkSpeedlimit) row;

			if (speedDependnt < 0) {
				
				if (speedlimit.getSpeedType() == 0) {

					array.add(speedlimit.Serialize(ObjLevel.FULL));
				}
				
				continue;
			}

			if (speedlimit.getSpeedType() == 3
					&& speedlimit.getSpeedDependent() == speedDependnt) {

				array.add(speedlimit.Serialize(ObjLevel.FULL));
			}
		}

		return array;
	}
	

	/***
	 * @查询上下线分离关联的link 1.关联link数量不能超过maxNum 2.关联查找link必须联通link方向一致
	 *                 3.关联link必须是夹角最小的一个link
	 * @param cuurentLinkPid
	 * @param cruuentNodePidDir
	 * @param maxNum
	 * @param loadChild
	 *            是否加载子表
	 * @return 查找所有联通link
	 * @throws Exception
	 */
	public List<RdLink> getNextTrackLinks(int cuurentLinkPid,
			int cruuentNodePidDir, int maxNum, boolean loadChild)
			throws Exception {
		RdLinkSelector linkSelector = new RdLinkSelector(conn);
		List<RdLink> tracks = new ArrayList<RdLink>();
		Set<Integer> nodes = new HashSet<Integer>();

		// 添加当前选中的link
		RdLink fristLink = (RdLink) linkSelector.loadById(cuurentLinkPid,
				!loadChild);
		nodes.add(fristLink.getsNodePid());
		nodes.add(fristLink.geteNodePid());
		tracks.add(fristLink);
		// 查找当前link联通的links
		List<RdLink> nextLinks = linkSelector.loadTrackLink(cuurentLinkPid,
				cruuentNodePidDir, true);
		while (nextLinks.size() > 0) {
			// 加载当前link
			RdLink currentLink = (RdLink) linkSelector.loadById(cuurentLinkPid,
					!loadChild);
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
		List<RdLink> tmpLinks = new ArrayList<RdLink>();
		List<RdLink> resultLinks = new ArrayList<RdLink>();
		Set<Integer> nodes = new HashSet<Integer>();

		// 添加当前选中的link
		RdLink fristLink = (RdLink) linkSelector.loadByIdOnlyRdLink(
				cuurentLinkPid, true);
		nodes.add(fristLink.getsNodePid());
		nodes.add(fristLink.geteNodePid());
		// 查找当前link联通的links
		List<RdLink> nextLinks = linkSelector.loadTrackLinkNoDirect(
				cuurentLinkPid, cruuentNodePidDir, true);

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
			tmpLinks = linkSelector.loadTrackLinkNoDirect(cuurentLinkPid,
					cruuentNodePidDir, true);
			// 清空当前link
			resultLinks.clear();
			// 10级路不计算挂接个数
			for (RdLink link : tmpLinks) {
				if (link.getKind() < 10) {
					resultLinks.add(link);
				}
			}
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
