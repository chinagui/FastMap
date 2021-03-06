package com.navinfo.dataservice.engine.edit.search.rd.utils;

import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.speedlimit.RdSpeedlimitSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.variablespeed.RdVariableSpeedSelector;
import com.vividsolutions.jts.geom.LineSegment;
import net.sf.json.JSONArray;

import java.sql.Connection;
import java.util.*;

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

	private final int speedLimitNextLinkCount = 30;

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

		List<RdVariableSpeed> variableSpeeds = variableSpeedSelector
				.loadRdVariableSpeedByParam(null, nodePid, link.getPid(),
						true);

		if (variableSpeeds.size() > 0) {
			return false;
		}

		return true;
	}

	/**
	 * 查询link串，批量修改限速使用。
	 *
	 * @param linkPid   关联link
	 * @param direct    限速方向
	 * @param dependent 小于0，普通限速； 大于0，条件限速，dependent（限速条件值）
	 */
	public List<Integer> getConnectLinks(int linkPid, int direct, int dependent, int speedValue)
			throws Exception {

		List<Integer> nextLinkPids = new ArrayList<>();

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

		getConnectLink(targetLink, nodePid, nextLinkPids, dependent, speedValue);

		return nextLinkPids;
	}

	/**
	 * 批量修改限速查询link串 递归调用
	 *
	 * @param targetLink 当前link
	 * @param nodePid    连接点nodePid
	 * @param linkPids   link串Pid
	 * @param dependent  小于0，普通限速； 大于0，条件限速，dependent（限速条件值）
	 * @param speedValue 限速值
	 */
	private void getConnectLink(RdLink targetLink, int nodePid, List<Integer> linkPids, int dependent, int speedValue)
			throws Exception {

		if (!linkPids.contains(targetLink.getPid())) {

			linkPids.add(targetLink.getPid());
		} else {
			return;
		}
		if (linkPids.size() >= speedLimitNextLinkCount) {

			return;
		}

		RdLinkSelector selector = new RdLinkSelector(conn);

		List<RdLink> links = selector.loadByNodePidOnlyRdLink(nodePid, true);

		RdLink nextLink = null;

		double minAngle = Double.MAX_VALUE;

		LineSegment targetlineSegment = getLineSegment(targetLink,
				nodePid);

		for (RdLink link : links) {

			if (link.getPid() == targetLink.getPid()) {
				continue;
			}
			if (link.getDirect() == 2 && link.getsNodePid() != nodePid) {
				continue;
			}
			if (link.getDirect() == 3 && link.geteNodePid() != nodePid) {
				continue;
			}

			LineSegment lineSegment = getLineSegment(link, nodePid);

			double angle = Math.abs(180 - AngleCalculator.getConnectLinksAngle(targetlineSegment, lineSegment, 0));

			if (angle < minAngle) {

				minAngle = angle;

				nextLink = link;
			}
		}

		if (nextLink == null || stopBySpeedLimit(nextLink, speedValue, nodePid, dependent)) {
			return;
		}

		int targetNodePid = nextLink.getsNodePid() == nodePid ? nextLink.geteNodePid() : nextLink.getsNodePid();

		getConnectLink(nextLink, targetNodePid, linkPids, dependent, speedValue);
	}

	/**
	 * 判断指定link上关联的限速是否满足停止追踪条件
	 *
	 * @param nextLink       下一条link
	 * @param speedValue     限速值
	 * @param nodePid        连接点nodePid
	 * @param speedDependent 小于0，普通限速； 大于0，条件限速，dependent（限速条件值）
	 */
	private boolean stopBySpeedLimit(RdLink nextLink, int speedValue, int nodePid, int speedDependent) throws Exception {

		int direct = nextLink.getDirect();

		if (direct != 2 && direct != 3) {
			direct = nextLink.getsNodePid() == nodePid ? 2 : 3;
		}

		RdSpeedlimitSelector selector = new RdSpeedlimitSelector(conn);

		List<RdSpeedlimit> limits = selector.loadSpeedlimitByLinkPid(nextLink.getPid(), true);

		for (RdSpeedlimit limit : limits) {

			if (limit.getDirect() != direct) {
				continue;
			}
			if (limit.getSpeedFlag() == 1 && limit.getSpeedValue() != speedValue) {
				continue;
			}
			if (speedDependent < 0 && limit.getSpeedType() != 0) {
				continue;

			} else if (speedDependent >= 0
					&& (limit.getSpeedType() != 3 || limit.getSpeedDependent() != speedDependent)) {
				continue;
			}
			return true;
		}
		return false;
	}

	public JSONArray getRdLinkSpeedlimit(List<Integer> linkPids,
			int speedDependent) throws Exception {

		AbstractSelector speedlimitSelector = new AbstractSelector(
				RdLinkSpeedlimit.class, conn);

		List<IRow> rows = speedlimitSelector
				.loadRowsByParentIds(linkPids, true);

		JSONArray array = new JSONArray();

		for (IRow row : rows) {

			RdLinkSpeedlimit speedlimit = (RdLinkSpeedlimit) row;

			if (speedDependent < 0) {
				
				if (speedlimit.getSpeedType() == 0) {

					array.add(speedlimit.Serialize(ObjLevel.FULL));
				}
				
				continue;
			}

			if (speedlimit.getSpeedType() == 3
					&& speedlimit.getSpeedDependent() == speedDependent) {

				array.add(speedlimit.Serialize(ObjLevel.FULL));
			}
		}

		return array;
	}


	/***
	 * 查询上下线分离关联的link 1.关联link数量不能超过maxNum 2.关联查找link必须联通link方向一致
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
								nextLinklineSegment,0));

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
	 * @param
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
	public static LineSegment getLineSegment(RdLink link, int nodePidDir) {
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
	
	public List<RdLink> getCloseTrackLinks(int cuurentLinkPid, int cisFlag)
			throws Exception {
		List<RdLink> tracks = new ArrayList<RdLink>();
		RdLinkSelector selector = new RdLinkSelector(conn);
		Set<Integer> nodes = new HashSet<Integer>();
		// 添加当前选中的link
		RdLink fristLink = (RdLink) selector.loadById(cuurentLinkPid, true);
		nodes.add(fristLink.getsNodePid());
		nodes.add(fristLink.geteNodePid());
		tracks.add(fristLink);
		// 查找当前link联通的links
		int cruuentNodePid = fristLink.geteNodePid();
		List<RdLink> nextLinks = selector.loadTrackLinkNoDirect(cuurentLinkPid,
				cruuentNodePid, true);
		while (nextLinks.size() > 0) {
			// 加载当前link
			RdLink currentLink = (RdLink) selector.loadById(cuurentLinkPid,
					true);
			// 计算当前link直线的几何属性
			LineSegment currentLinklineSegment = getLineSegment(currentLink,
					cruuentNodePid);
			// 如果当前link的起点等于当前联通方向
			// 取当前link的最后两个形状点组成直线

			Map<Double, RdLink> map = new HashMap<Double, RdLink>();
			for (RdLink rdlink : nextLinks) {
				// 获取联通links直线的几何
				LineSegment nextLinklineSegment = getLineSegment(rdlink,
						cruuentNodePid);
				// 计算当前线直线和联通直线夹角 按照顺逆标志
				double minAngle = Math.abs(AngleCalculator
						.getConnectLinksAngle(currentLinklineSegment,
								nextLinklineSegment, cisFlag));

				if (map.size() > 0) {
					if (map.keySet().iterator().next() > minAngle) {
						map.clear();
						map.put(minAngle, rdlink);
					}

				} else {
					map.put(minAngle, rdlink);
				}
			}
			// 获取联通线中夹角最小的link
			// 赋值给当前cuurentLinkPid 确定方向
			RdLink link = map.values().iterator().next();
			cuurentLinkPid = link.getPid();
			cruuentNodePid = (cruuentNodePid == link.getsNodePid()) ? link
					.geteNodePid() : link.getsNodePid();
			if (nodes.contains(cruuentNodePid)) {
				tracks.add(link);
				break;
			}
			nodes.add(cruuentNodePid);
			tracks.add(link);
			// 赋值查找下一组联通links
			nextLinks = selector.loadTrackLinkNoDirect(cuurentLinkPid,
					cruuentNodePid, true);
		}
		return tracks;
	}
}
