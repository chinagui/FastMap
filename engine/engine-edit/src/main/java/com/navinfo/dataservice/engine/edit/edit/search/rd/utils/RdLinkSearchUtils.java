package com.navinfo.dataservice.engine.edit.edit.search.rd.utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
/**
 * 
 * @author zhaokk
 * RDLINK 复杂查询公共类 
 *
 */
public class RdLinkSearchUtils {
	private Connection conn;

	public RdLinkSearchUtils(Connection conn) throws Exception {
		this.conn = conn;
	}
    /*
     * @查询上下线分离关联的link
     * 1.关联link数量不能超过999
     * 2.关联查找link 必须联通link方向一致
     * 3.关联link必须是夹角最小的一个link
     * @param  cuurentLinkPid 当前link 当前方向node
     * @return 查找所有联通link
     * 
     * */
	public List<RdLink> getNextTrackLinks(int cuurentLinkPid,
			int cruuentNodePidDir) throws Exception {
		RdLinkSelector linkSelector = new RdLinkSelector(conn);
		List<RdLink> tracks = new ArrayList<RdLink>();
		//添加当前选中的link
		tracks.add((RdLink) linkSelector.loadById(cuurentLinkPid, true));
		//查找当前link联通的links
		List<RdLink> nextLinks = linkSelector.loadTrackLink(cuurentLinkPid,
				cruuentNodePidDir, true);
		while (nextLinks.size() > 0 && tracks.size() <= 999) {
			//加载当前link
			RdLink currentLink = (RdLink) linkSelector.loadById(cuurentLinkPid,
					true);
			// 计算当前link直线的几何属性
			LineSegment currentLinklineSegment =getLineSegment(currentLink, cruuentNodePidDir);
		   //如果当前link的起点等于当前联通方向
		   // 取当前link的最后两个形状点组成直线

			Map<Double, RdLink> map = new HashMap<Double, RdLink>();
			for (RdLink ad : nextLinks) {
				//获取联通links直线的几何
				LineSegment nextLinklineSegment = getLineSegment(ad, cruuentNodePidDir);
				//计算当前线直线和联通直线夹角 选出当前线延长线夹角最小
				double minAngle = AngleCalculator.getAngle(
						currentLinklineSegment, nextLinklineSegment);
				if (map.size() > 0) {
					if (map.keySet().iterator().next() < minAngle) {
						map.clear();
						map.put(minAngle, ad);
					}

				} else {
					map.put(minAngle, ad);
				}
			}
			//获取联通线中夹角最小的link
			//赋值给当前cuurentLinkPid 确定方向
			RdLink link = map.values().iterator().next();
			cuurentLinkPid = link.getPid();
			if (link.getDirect() == 2) {
				cruuentNodePidDir = link.geteNodePid();
			}
			if (link.getDirect() == 3) {
				cruuentNodePidDir = link.getsNodePid();
			}
			if (link.getDirect() == 1) {
				cruuentNodePidDir = (cruuentNodePidDir == link.getsNodePid()) ? link
						.geteNodePid() : link.getsNodePid();
			}
			tracks.add(link);
			//赋值查找下一组联通links
			nextLinks = linkSelector.loadTrackLink(cuurentLinkPid,
					cruuentNodePidDir, true);
		}
		return tracks;
	}
	private LineSegment getLineSegment(RdLink link,int nodePidDir){
		LineSegment lineSegment = null;
		if(link.getsNodePid() == nodePidDir){
			lineSegment= new  LineSegment(
					link.getGeometry().getCoordinates()[0],
					link.getGeometry().getCoordinates()[1]);
		}if(link.geteNodePid() == nodePidDir){
			lineSegment = new LineSegment(
					link.getGeometry().getCoordinates()[link.getGeometry().getCoordinates().length-2],
					link.getGeometry().getCoordinates()[link.getGeometry().getCoordinates().length-1]);
		}
		return lineSegment;
	}

}
