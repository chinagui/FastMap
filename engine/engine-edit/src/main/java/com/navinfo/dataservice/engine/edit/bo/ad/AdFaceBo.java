package com.navinfo.dataservice.engine.edit.bo.ad;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.engine.edit.bo.BoFactory;
import com.navinfo.dataservice.engine.edit.bo.BreakResult;
import com.navinfo.dataservice.engine.edit.bo.FaceBo;
import com.navinfo.dataservice.engine.edit.bo.LinkBo;
import com.navinfo.dataservice.engine.edit.bo.PoFactory;
import com.navinfo.dataservice.engine.edit.utils.AdLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @ClassName: BoAdLink
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: BoAdLink.java
 */
public class AdFaceBo extends FaceBo {
	protected AdFace po;
	protected List<AdFaceTopo> topos;

	public Result breakoff(LinkBo oldLink, LinkBo newLeftLink,
			LinkBo newRightLink) throws Exception {
		BreakResult result = new BreakResult();
		List<AdLink> links = new ArrayList<AdLink>();
		for (IRow iRow : po.getFaceTopos()) {
			AdFaceTopo obj = (AdFaceTopo) iRow;
			if (obj.getLinkPid() != oldLink.getPo().pid()) {
				links.add((AdLink) PoFactory.getInstance().get(conn,
						AdLink.class, obj, false));
			}
			result.insertObject(obj, ObjStatus.DELETE, po.getPid());
		}
		links.add((AdLink) newLeftLink.getPo());
		links.add((AdLink) newRightLink.getPo());

		if (links.size() < 1) {
			throw new Exception("重新维护面的形状:发现面没有组成link");
		}
		AdLink currLink = links.get(0);
		// 获取当前LINK和NODE
		int startNodePid = currLink.getsNodePid();
		int currNodePid = startNodePid;
		Map<AdLink, Integer> map = new HashMap<AdLink, Integer>();
		map.put(currLink, 1);
		int index = 1;
		List<Geometry> list = new ArrayList<Geometry>();
		list.add(currLink.getGeometry());
		Map<Integer, AdLink> currLinkAndPidMap = new HashMap<Integer, AdLink>();
		currLinkAndPidMap.put(currNodePid, currLink);
		// 获取下一条联通的LINK
		while (AdLinkOperateUtils.getNextLink(links, currLinkAndPidMap)) {
			if (currLinkAndPidMap.keySet().iterator().next() == startNodePid) {
				break;
			}
			index++;
			map.put(currLinkAndPidMap.get(currLinkAndPidMap.keySet().iterator()
					.next()), index);
			list.add(currLinkAndPidMap.get(
					currLinkAndPidMap.keySet().iterator().next()).getGeometry());
		}

		for (AdLink link : map.keySet()) {
			AdFaceTopo faceTopo = new AdFaceTopo();
			faceTopo.setLinkPid(link.getPid());
			faceTopo.setFacePid(po.getPid());
			faceTopo.setSeqNum(map.get(link));
			result.insertObject(faceTopo, ObjStatus.INSERT, po.getPid());
		}
		Geometry g = GeoTranslator.getCalLineToPython(list);
		Coordinate[] c1 = new Coordinate[g.getCoordinates().length];
		// 判断线组成面是否可逆
		if (!GeometryUtils.IsCCW(g.getCoordinates())) {
			for (int i = g.getCoordinates().length - 1; i >= 0; i--) {
				c1[c1.length - i - 1] = g.getCoordinates()[i];
			}
			this.reverseFaceTopo(result);

		} else {
			c1 = g.getCoordinates();
		}

		this.updateGeometry(GeoTranslator.getPolygonToPoints(c1), this.po,
				result);

		return result;
	}

	public List<AdFaceBo> query(Connection conn, int linkPid, boolean isLock)
			throws Exception {
		AdFaceSelector s = new AdFaceSelector(conn);
		List<AdFace> faces = s.loadAdFaceByLinkId(linkPid, isLock);
		List<AdFaceBo> list = new ArrayList<AdFaceBo>();
		for (AdFace face : faces) {
			list.add((AdFaceBo) BoFactory.getInstance().create(face));
		}
		return list;
	}

	@Override
	public void setPo(IObj po) {
		this.po = (AdFace) po;
		this.geometry = this.po.getGeometry();
	}

	@Override
	public IObj getPo() {
		return po;
	}

	private void reverseFaceTopo(Result result) {
		int newIndex = 0;
		for (int i = result.getAddObjects().size() - 1; i >= 0; i--) {
			if (result.getAddObjects().get(i) instanceof AdFaceTopo) {
				newIndex++;
				((AdFaceTopo) result.getAddObjects().get(i))
						.setSeqNum(newIndex);

			}
		}
	}

	private void updateGeometry(Geometry g, AdFace face, Result result)
			throws Exception {

		JSONObject updateContent = new JSONObject();
		g = GeoTranslator.transform(g, 0.00001, 5);
		updateContent.put("geometry", GeoTranslator.jts2Geojson(g));
		updateContent.put("area", GeometryUtils.getCalculateArea(g));
		updateContent.put("perimeter", GeometryUtils.getLinkLength(g));
		face.fillChangeFields(updateContent);
		result.insertObject(face, ObjStatus.UPDATE, face.getPid());
	}
}
