package com.navinfo.dataservice.engine.edit.utils;

import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.commons.geom.AngleCalculator.LngLatPoint;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.search.rd.utils.RdLinkSearchUtils;
import com.vividsolutions.jts.geom.LineSegment;
import org.apache.commons.collections.CollectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class CalLinkOperateUtils {
	private Connection conn;

	public CalLinkOperateUtils() {
	}

	public CalLinkOperateUtils(Connection conn) {
		this.conn = conn;
	}

	/**
	 * 计算关系类型
	 * 
	 * @param conn
	 * @param nodePid
	 *            进入点
	 * @param outLinkPid
	 *            退出线
	 * @return
	 * @throws Exception
	 */
	public int getRelationShipType(int nodePid, int outLinkPid)
			throws Exception {

		String sql = "WITH c1 AS (SELECT node_pid FROM rd_cross_node a WHERE EXISTS (SELECT null FROM rd_cross_node b WHERE a.pid=b.pid AND b.node_pid=:1 AND b.U_RECORD !=2) AND a.U_RECORD !=2) SELECT count(1) count FROM rd_link c WHERE c.link_pid=:2 AND (c.s_node_pid=:3 OR c.e_node_pid=:4 OR EXISTS(SELECT null FROM c1 WHERE c.s_node_pid=c1.node_pid OR c.e_node_pid=c1.node_pid)) AND c.U_RECORD !=2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, nodePid);

			pstmt.setInt(2, outLinkPid);

			pstmt.setInt(3, nodePid);

			pstmt.setInt(4, nodePid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				int count = resultSet.getInt("count");

				if (count > 0) {
					return 1;
				} else {
					return 2;
				}
			}

		} catch (Exception e) {

			throw e;
		} finally {
			try {
				resultSet.close();
			} catch (Exception e) {

			}

			try {
				pstmt.close();
			} catch (Exception e) {

			}
		}

		return 1;
	}

	/**
	 * 计算经过线
	 * 
	 * @param conn
	 * @param inLinkPid
	 *            进入线
	 * @param nodePid
	 *            进入点
	 * @param outLinkPid
	 *            退出线
	 * @return
	 * @throws Exception
	 */
	public List<Integer> calViaLinks(Connection conn, int inLinkPid,
			int nodePid, int outLinkPid) throws Exception {

		CalPassLinkUtils p = new CalPassLinkUtils(conn);

		List<Integer> passLinkPids = p.calcPassLinks(inLinkPid,
				nodePid, outLinkPid);

		return passLinkPids;
	}

	public List<Integer> calViaLinks(Connection conn, RdLink inLink,
									  int outLinkPid) throws Exception {

		CalPassLinkUtils p = new CalPassLinkUtils(conn);

		List<Integer> passLinkPids = p.calcPassLinks(inLink, outLinkPid);


		return passLinkPids;
	}

	/***
	 * 判断所给有序link是否连通
	 * 
	 * @param linkpids
	 * @param intersectFlag
	 * @return
	 * @throws Exception
	 */
	public boolean isConnect(List<Integer> linkpids, int intersectFlag)
			throws Exception {
		RdLinkSelector linkSelector = new RdLinkSelector(this.conn);

		List<IRow> linkRows = linkSelector.loadByIds(linkpids, true, false);

		Map<Integer, RdLink> linkMap = new HashMap<Integer, RdLink>();

		for (IRow linkRow : linkRows) {

			RdLink link = (RdLink) linkRow;

			linkMap.put(link.getPid(), link);
		}

		if (linkMap.size() != linkpids.size()) {
			return false;
		}

		for (int i = 0; i < linkpids.size() - 1; i++) {

			RdLink preLink = linkMap.get(linkpids.get(i));

			RdLink nextLink = linkMap.get(linkpids.get(i + 1));

			if ((nextLink.getDirect() == 1 || nextLink.getDirect() == 2)
					&& (nextLink.getsNodePid() == preLink.getsNodePid() || nextLink
							.getsNodePid() == preLink.geteNodePid())) {

				if (nextLink.getsNodePid() == intersectFlag) {

					intersectFlag = nextLink.geteNodePid();

				} else {

					return false;
				}

			} else if ((nextLink.getDirect() == 1 || nextLink.getDirect() == 3)
					&& (nextLink.geteNodePid() == preLink.getsNodePid() || nextLink
							.geteNodePid() == preLink.geteNodePid())) {

				if (nextLink.geteNodePid() == intersectFlag) {

					intersectFlag = nextLink.getsNodePid();

				} else {

					return false;
				}

			} else {

				return false;
			}
		}

		return true;
	}

	/**
	 * 将一组link按顺序挂接
	 * 
	 * @param rdlinks
	 * @return
	 */
	public List<RdLink> sortLink(List<RdLink> rdlinks) {

		List<RdLink> sortLinks = new ArrayList<RdLink>();

		if (rdlinks == null || rdlinks.size() == 0) {

			return sortLinks;
		}

		if (rdlinks.size() < 3) {

			return rdlinks;
		}

		List<RdLink> cacheLinks = new ArrayList<RdLink>();

		cacheLinks.addAll(rdlinks);

		int targetNodePid = cacheLinks.get(0).getsNodePid();

		getConnectLink(targetNodePid, cacheLinks, sortLinks, 1);

		getConnectLink(targetNodePid, cacheLinks, sortLinks, 0);

		return sortLinks;
	}

	/**
	 * 获取挂接link
	 * 
	 * @param targetNodePid
	 *            连接点
	 * @param cacheLinks
	 *            link池
	 * @param sortLinks
	 *            有序link
	 * @param type
	 *            挂接类型 1：顺向、2：逆向
	 */
	private void getConnectLink(int targetNodePid, List<RdLink> cacheLinks,
			List<RdLink> sortLinks, int type) {

		RdLink connectLink = null;

		for (RdLink link : cacheLinks) {

			if (targetNodePid != link.getsNodePid()
					&& targetNodePid != link.geteNodePid()) {
				continue;
			}
			if (sortLinks.contains(link)) {
				continue;
			}

			targetNodePid = (targetNodePid == link.getsNodePid()) ? link
					.geteNodePid() : link.getsNodePid();

			if (type == 1) {

				sortLinks.add(link);

			} else {

				sortLinks.add(0, link);
			}

			connectLink = link;

			break;
		}

		if (connectLink != null) {

			cacheLinks.remove(connectLink);

			getConnectLink(targetNodePid, cacheLinks, sortLinks, type);
		}
	}

	/**
	 * 计算link的经过点
	 * 
	 * @param links
	 *            目标link
	 * @return 经过点Pid
	 */
	public static List<Integer> calNodePids(List<RdLink> links) {
		List<Integer> nodePids = new ArrayList<>();
		if (null == links || links.isEmpty())
			return nodePids;
		Map<Integer, Integer> map = new HashMap<>();
		for (RdLink link : links) {
			Integer sNum = map.get(link.getsNodePid());
			Integer eNUm = map.get(link.geteNodePid());
			if (null == sNum) {
				map.put(link.getsNodePid(), 1);
			} else {
				map.put(link.getsNodePid(), sNum + 1);
			}
			if (null == eNUm) {
				map.put(link.geteNodePid(), 1);
			} else {
				map.put(link.geteNodePid(), eNUm + 1);
			}
		}
		for (Integer nodePid : map.keySet()) {
			Integer num = map.get(nodePid);
			if (num > 1) {
				nodePids.add(nodePid);
			}
		}
		return nodePids;
	}

	/**
	 * 计算进入点联通的线(排除进入线和已经选择该link作为退出线的)
	 * 
	 * @param conn
	 * @param nodePid
	 *            进入点
	 * @param inLinkPid
	 *            进入线
	 * @param linkPidList
	 *            需要排除在外的退出线pid
	 * @return
	 */
	public List<Integer> getInNodeLinkPids(int nodePid, int inLinkPid,
			List<Integer> linkPidList) {
		RdLinkSelector selector = new RdLinkSelector(conn);

		List<Integer> linkPids = new ArrayList<Integer>();

		try {
			List<RdLink> rdLinks = selector.loadByNodePidOnlyRdLink(nodePid,
					true);

			for (RdLink link : rdLinks) {
				if (link.getDirect() == 1) {
					linkPids.add(link.getPid());
				} else if (link.getDirect() == 2
						&& link.getsNodePid() == nodePid) {
					linkPids.add(link.getPid());
				} else if (link.getDirect() == 3
						&& link.geteNodePid() == nodePid) {
					linkPids.add(link.getPid());
				}
			}

			// linkPids = selector.loadLinkPidByNodePid(nodePid, true);

			if (CollectionUtils.isNotEmpty(linkPids)) {
				// 剔除进入线，防止进入线和退出线是一条线
				if (linkPids.contains(inLinkPid)) {
					linkPids.remove(linkPids.indexOf(inLinkPid));
				}

				// 删除已经作为指定方向的退出线
				linkPids.removeAll(linkPidList);
			}
		} catch (Exception e) {
		}
		return linkPids;
	}

	/**
	 * 计算箭头的限制信息
	 * 
	 * @param arrow
	 * @param infoMap
	 */
	public static int calIntInfo(String arrow) {
		if (arrow.contains("[")) {
			// 理论值带[]
			return Integer.parseInt(arrow.substring(1, 2));
		} else {
			// 实际值不带
			return Integer.parseInt(arrow);
		}
	}

	/**
	 * 获取最小夹角的退出线
	 * 
	 * @param outLinkPids
	 *            退出线
	 * @param infoList
	 *            交限信息
	 */
	public static int getMinAngleOutLinkPidOnArrowDir(
			List<Integer> outLinkPids, int arrow,
			Map<Integer, LineSegment> outLinkSegmentMap,
			LineSegment inLinkSegment) {
		// 最小夹角对应的退出线
		int minAngleOutLinkPid = 0;

		// 最小夹角
		double temAngle = 361;

		List<Integer> resultOutLinkPids = new ArrayList<>();

		resultOutLinkPids.addAll(outLinkPids);

		for (Integer outPid : resultOutLinkPids) {
			LineSegment outLinkSegment = outLinkSegmentMap.get(outPid);

			if (outLinkSegment != null) {
				// 获取线的夹角
				double angle = AngleCalculator.getAngle(inLinkSegment,
						outLinkSegment);
				// 计算交限信息
				int restricInfo = calRestricInfo(angle);

				if (arrow == restricInfo) {
					// link计算的夹角比上个link的夹角小的替换最小夹角和对应的linkPid
					if (angle < temAngle) {

						temAngle = angle;

						minAngleOutLinkPid = outPid;
					}
				}
			}

		}

		return minAngleOutLinkPid;
	}

	/**
	 * 计算限制信息 直： (angle > 157.5 && angle <= 202.5)； 左：(angle > 247.5 && angle <=
	 * 292.5)； 调：(angle > 337.5 && angle <= 360.0)；(angle >= 0 && angle <=
	 * 22.5)； 右：(angle > 67.5 && angle < 112.5)
	 * 
	 * @param angle
	 * @return
	 */
	public static int calRestricInfo(double angle) {
		if (angle > 45 && angle <= 135) {
			return 3;
		} else if (angle > 135 && angle <= 225) {
			return 4;
		} else if (angle > 225 && angle <= 315) {
			return 2;
		} else {
			return 1;
		}

	}

	/***
	 * 
	 * @param inNodePid
	 *            进入点
	 * @param inLinkPid
	 *            进入线
	 * @param arrows
	 *            方向箭头
	 * @return
	 * @throws Exception
	 */
	public Map<String, List<Integer>> getOutLinkForArrow(int inNodePid,
			int inLinkPid, List<String> arrows) throws Exception {
		// 判断进入点是否参与路口关系 原则一个进入点只能属于一个路口关系
		RdCrossNodeSelector crossNodeSelector = new RdCrossNodeSelector(conn);
		RdLinkSelector linkSelector = new RdLinkSelector(conn);

		List<RdLink> outLinks = new ArrayList<RdLink>();

		int rdCrossPid = crossNodeSelector.loadCrossNodeByNodePid(inNodePid);

		Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
		RdLink inLink = (RdLink) linkSelector.loadById(inLinkPid, true);

		LineSegment inLinkSegment = RdLinkSearchUtils.getLineSegment(inLink,
				inNodePid);
		if (rdCrossPid != 0) {

			double inAngle = this.getAngelOfNorth(inNodePid, inLink,
					inLinkSegment);

			Map<RdLink, Integer> mapOutLinks = linkSelector.getLinksByCrossId(
					rdCrossPid, inLinkPid, true);
			this.caleOutLinkForArrow(arrows, mapOutLinks, inNodePid, inAngle,
					inLinkSegment, map);
		} else {
			outLinks = linkSelector.loadTrackLink(inLinkPid, inNodePid, true);
			this.caleOutLinkForArrow(arrows, outLinks, inNodePid,
					inLinkSegment, map);

		}
		return map;
	}

	/***
	 * zhaokk 计算进入点挂接link的退出线
	 * 
	 * @param arrows
	 * @param outLinks
	 * @param inNodePid
	 * @param inLinkSegment
	 * @param map
	 */
	private void caleOutLinkForArrow(List<String> arrows,
			List<RdLink> outLinks, int inNodePid, LineSegment inLinkSegment,
			Map<String, List<Integer>> map) {
		for (String arrow : arrows) {
			int arr = calIntInfo(arrow);
			Map<Integer, Double> angleMap = new TreeMap<Integer, Double>();

			for (RdLink link : outLinks) {
				this.caleArrowForOutLink(link, inLinkSegment, inNodePid,
						angleMap, arr);

			}

			List<Integer> list = new ArrayList<Integer>(angleMap.keySet());
			map.put(arrow, list);

		}
	}

	/***
	 * zhaokk 计算路口范围内点挂接link的退出线
	 * 
	 * @param arrows
	 * @param mapOutLinks
	 * @param inAngle
	 * @param inLinkSegment
	 * @param map
	 */
	private void caleOutLinkForArrow(List<String> arrows,
			Map<RdLink, Integer> mapOutLinks, int inNodePid, double inAngle,
			LineSegment inLinkSegment, Map<String, List<Integer>> map) {
		for (String arrow : arrows) {
			int arr = calIntInfo(arrow);
			Map<Integer, Double> angleMap = new TreeMap<Integer, Double>();
			for (RdLink link : mapOutLinks.keySet()) {
				// 计算箭头方向对应的角度范围的退出线
				int nodePid = mapOutLinks.get(link);
				if (nodePid != inNodePid) {

					LineSegment outLinkSegment = RdLinkSearchUtils
							.getLineSegment(link, nodePid);

					double angle = this.getAngelOfNorth(nodePid, link,
							outLinkSegment);
					double realAngel = 0;
					if (inAngle - angle >= 0) {
						realAngel = inAngle - angle;
					} else {
						realAngel = 360 + inAngle - angle;
					}

					if (this.isMatchArr(realAngel, arr)) {

						angleMap.put(link.getPid(), Double.valueOf(this
								.getRealAngle(realAngel, arr)));
					}

				} else {
					this.caleArrowForOutLink(link, inLinkSegment, nodePid,
							angleMap, arr);
				}

			}
			ValueComparator bvc = new ValueComparator(angleMap);
			TreeMap<Integer, Double> sorted_map = new TreeMap<Integer, Double>(
					bvc);
			sorted_map.putAll(angleMap);

			List<Integer> list = new ArrayList<Integer>(sorted_map.keySet());
			map.put(arrow, list);

		}
	}

	/***
	 * 计算link和正北方向的夹角
	 * 
	 * @param nodePid
	 * @param link
	 * @param linkSegment
	 * @return
	 */
	private double getAngelOfNorth(int nodePid, RdLink link,
			LineSegment linkSegment) {
		LngLatPoint latPointa = null;
		LngLatPoint latPointb = null;
		if (nodePid != link.getsNodePid()) {
			latPointa = new LngLatPoint(linkSegment.p1.x / 100000,
					linkSegment.p1.y / 100000);
			latPointb = new LngLatPoint(linkSegment.p0.x / 100000,
					linkSegment.p0.y / 100000);
		} else {
			latPointa = new LngLatPoint(linkSegment.p0.x / 100000,
					linkSegment.p0.y / 100000);
			latPointb = new LngLatPoint(linkSegment.p1.x / 100000,
					linkSegment.p1.y / 100000);
		}
		return AngleCalculator.getAngle(latPointa, latPointb);

	}

	/***
	 * 计算进入线和退出线 按照箭头的夹角 zhaokk
	 * 
	 * @param link
	 * @param inLinkSegment
	 * @param nodePid
	 * @param angleMap
	 * @param arr
	 */
	private void caleArrowForOutLink(RdLink link, LineSegment inLinkSegment,
			int nodePid, Map<Integer, Double> angleMap, int arr) {
		LineSegment outLinkSegment = RdLinkSearchUtils.getLineSegment(link,
				nodePid);
		// 计算进入线和和退出线的夹角、
		double angle = AngleCalculator.getConnectLinksAngle(inLinkSegment,
				outLinkSegment, 2);
		if (this.isMatchArr(angle, arr)) {
			double realAngle = this.getRealAngle(angle, arr);
			angleMap.put(link.getPid(), Double.valueOf(realAngle));
		}

	}

	/***
	 * 计算按照箭头方向角度最接近的绝对值 zhaokk
	 * 
	 * @param angle
	 * @param arr
	 * @return
	 */
	private double getRealAngle(double angle, int arr) {
		if (arr == 1) {
			return Math.abs(angle - 180);
		}
		if (arr == 2) {
			return Math.abs(angle - 270);
		}
		if (arr == 3) {
			return Math.abs(angle - 360);
		}
		if (arr == 4) {
			return Math.abs(angle - 90);
		}
		return 0;
	}

	/***
	 * zhaokk 直： (angle > 157.5 && angle <= 202.5)； 左：(angle > 247.5 && angle <=
	 * 292.5)； 调：(angle > 337.5 && angle <= 360.0)；(angle >= 0 && angle <=
	 * 22.5)； 右：(angle > 67.5 && angle < 112.5)
	 * 
	 * @param angle
	 * @param arrow
	 * @return
	 */
	private boolean isMatchArr(double angle, int arrow) {
		if (arrow == 1) {
			if (angle > 157.5 && angle <= 202.5) {
				return true;
			}
		}
		if (arrow == 2) {
			if (angle > 247.5 && angle <= 292.5) {
				return true;
			}
		}
		if (arrow == 3) {
			if (angle > 67.5 && angle < 112.5) {
				return true;
			}
		}
		if (arrow == 4) {
			if ((angle > 337.5 && angle <= 360.0)
					|| (angle >= 0 && angle <= 22.5)) {
				return true;
			}
		}

		return false;
	}

	class ValueComparator implements Comparator<Integer> {

		Map<Integer, Double> base;

		public ValueComparator(Map<Integer, Double> base) {
			this.base = base;
		}

		// Note: this comparator imposes orderings that are inconsistent with
		// equals.
		public int compare(Integer a, Integer b) {
			if (base.get(a) <= base.get(b)) {
				return -1;
			} else {
				return 1;
			}
		}
	}
}
