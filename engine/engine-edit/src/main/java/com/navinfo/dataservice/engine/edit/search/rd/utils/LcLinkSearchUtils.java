package com.navinfo.dataservice.engine.edit.search.rd.utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;


import com.vividsolutions.jts.geom.LineSegment;

/**
 * 
 * @author zhaokk LCLINK 复杂查询公共类
 * 
 */
public class LcLinkSearchUtils {
	private Connection conn;

	public LcLinkSearchUtils(Connection conn) throws Exception {
		this.conn = conn;
	}

	public List<LcLink> getCloseTrackLinks(int cuurentLinkPid, int cisFlag)
			throws Exception {
		List<LcLink> tracks = new ArrayList<LcLink>();
		LcLinkSelector selector = new LcLinkSelector(conn);
		Set<Integer> nodes = new HashSet<Integer>();
		// 添加当前选中的link
		LcLink fristLink = (LcLink) selector.loadById(cuurentLinkPid, true);
		nodes.add(fristLink.getsNodePid());
		nodes.add(fristLink.geteNodePid());
		tracks.add(fristLink);
		// 查找当前link联通的links
		int cruuentNodePid = fristLink.geteNodePid();
		List<LcLink> nextLinks = selector.loadTrackLinkNoDirect(cuurentLinkPid,
				cruuentNodePid, true);
		while (nextLinks.size() > 0) {
			// 加载当前link
			LcLink currentLink = (LcLink) selector.loadById(cuurentLinkPid,
					true);
			// 计算当前link直线的几何属性
			LineSegment currentLinklineSegment = getLineSegment(currentLink,
					cruuentNodePid);
			// 如果当前link的起点等于当前联通方向
			// 取当前link的最后两个形状点组成直线

			Map<Double, LcLink> map = new HashMap<Double, LcLink>();
			for (LcLink lcLink : nextLinks) {
				// 获取联通links直线的几何
				LineSegment nextLinklineSegment = getLineSegment(lcLink,
						cruuentNodePid);
				// 计算当前线直线和联通直线夹角 按照顺逆标志
				double minAngle = Math.abs(AngleCalculator
						.getConnectLinksAngle(currentLinklineSegment,
								nextLinklineSegment, cisFlag));

				if (map.size() > 0) {
					if (map.keySet().iterator().next() > minAngle) {
						map.clear();
						map.put(minAngle, lcLink);
					}

				} else {
					map.put(minAngle, lcLink);
				}
			}
			// 获取联通线中夹角最小的link
			// 赋值给当前cuurentLinkPid 确定方向
			LcLink link = map.values().iterator().next();
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

	/**
	 * 获取link指定端点处的直线几何
	 * 
	 * @param link
	 *            线
	 * @param nodePidDir
	 *            指定端点
	 * @return 以指定端点为起点的直线几何
	 */
	private LineSegment getLineSegment(LcLink link, int nodePidDir) {
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
