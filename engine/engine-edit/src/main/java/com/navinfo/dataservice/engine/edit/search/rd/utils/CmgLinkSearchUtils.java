package com.navinfo.dataservice.engine.edit.search.rd.utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.AngleCalculator;

import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildlinkSelector;

import com.vividsolutions.jts.geom.LineSegment;

/**
 * 
 * @author zhaokk CmgBuildlink 复杂查询公共类
 * 
 */
public class CmgLinkSearchUtils {
	private Connection conn;

	public CmgLinkSearchUtils(Connection conn) throws Exception {
		this.conn = conn;
	}

	public List<CmgBuildlink> getCloseTrackLinks(int cuurentLinkPid, int cisFlag)
			throws Exception {
		List<CmgBuildlink> tracks = new ArrayList<CmgBuildlink>();
		CmgBuildlinkSelector selector = new CmgBuildlinkSelector(conn);
		Set<Integer> nodes = new HashSet<Integer>();
		// 添加当前选中的link
		CmgBuildlink fristLink = (CmgBuildlink) selector.loadById(
				cuurentLinkPid, true);
		nodes.add(fristLink.getsNodePid());
		nodes.add(fristLink.geteNodePid());
		tracks.add(fristLink);
		// 查找当前link联通的links
		int cruuentNodePid = fristLink.geteNodePid();
		List<CmgBuildlink> nextLinks = selector.loadTrackLinkNoDirect(
				cuurentLinkPid, cruuentNodePid, true);
		while (nextLinks.size() > 0) {
			// 加载当前link
			CmgBuildlink currentLink = (CmgBuildlink) selector.loadById(
					cuurentLinkPid, true);
			// 计算当前link直线的几何属性
			LineSegment currentLinklineSegment = getLineSegment(currentLink,
					cruuentNodePid);
			// 如果当前link的起点等于当前联通方向
			// 取当前link的最后两个形状点组成直线

			Map<Double, CmgBuildlink> map = new HashMap<Double, CmgBuildlink>();
			for (CmgBuildlink CmgBuildlink : nextLinks) {
				// 获取联通links直线的几何
				LineSegment nextLinklineSegment = getLineSegment(CmgBuildlink,
						cruuentNodePid);
				// 计算当前线直线和联通直线夹角 按照顺逆标志
				double minAngle = Math.abs(AngleCalculator
						.getConnectLinksAngle(currentLinklineSegment,
								nextLinklineSegment, cisFlag));

				if (map.size() > 0) {
					if (map.keySet().iterator().next() > minAngle) {
						map.clear();
						map.put(minAngle, CmgBuildlink);
					}

				} else {
					map.put(minAngle, CmgBuildlink);
				}
			}
			// 获取联通线中夹角最小的link
			// 赋值给当前cuurentLinkPid 确定方向
			CmgBuildlink link = map.values().iterator().next();
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
	private LineSegment getLineSegment(CmgBuildlink link, int nodePidDir) {
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
