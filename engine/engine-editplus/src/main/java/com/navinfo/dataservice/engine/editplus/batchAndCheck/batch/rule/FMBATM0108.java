package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import net.sf.json.JSONObject;

/**
 * 【批处理执行时间】POI数据日落月完成POI精编前批之前
 * 【批处理执行查询条件】新增POI或修改引导坐标或引导link为0的POI对象或对应引导link不存在rd_link表中
 * 【批处理字段】IX_POI表中x_guide、y_guide、link_pid、name_groupid、side、pmesh_id 【批处理原则】
 * 根据引导坐标3m范围找Link，如果存在，则进行批处理，如果找到多条，则取最近的一条进行批处理； 如果不存在，则需要把引导link赋值为0。
 * 字段赋值原则如下： 引导坐标：求出最近点保留5位后与原来一致则不修改；如果不一致，则取最近点保留5位精度。
 * SIDE：基于显示坐标计算显示坐标与link的距离，如果在1.5米之内，则为“3
 * link上”；如果在1.5米之外，则根据link的画线方向计算，如果在划线方向的左侧，则side赋值为“1
 * Link左侧”，如果在划线方向右侧，则赋值为“2 Link右侧”； PMESH_ID：取关联link的mesh_id NAME_GROUPID:赋值为0
 * 
 * @author gaopengrong
 *
 */
public class FMBATM0108 extends BasicBatchRule {
	private Map<Long, JSONObject> poiInfo = new HashMap<Long, JSONObject>();
	private List<Long> NotInRdLink = new ArrayList<Long>();

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub
		Set<Long> pidList = new HashSet<Long>();
		for (BasicObj obj : batchDataList) {
			pidList.add(obj.objPid());
		}
		poiInfo = IxPoiSelector.getCalculateValuesByPid(getBatchRuleCommand().getConn(), pidList);
		NotInRdLink = IxPoiSelector.getPoiForLinkPidNotInRdLink(getBatchRuleCommand().getConn(), pidList);
	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			if (!isBatch(poiObj)) {
				return;
			}
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			JSONObject linkInfo = poiInfo.get(poi.getPid());
			if (linkInfo == null || linkInfo.isEmpty()) {
				if (poi.getLinkPid() != 0) {
					poi.semeshId = linkInfo.getInt("MESH_ID");
				Coordinate nearestPoint = GeometryUtils.GetNearestPointOnLine(poi.getGeometry().getCoordinate(), geom);
				Geometry tmpPoint = new GeometryFactory().createPoint(nearestPoint);
				int side = GeometryUtils.calulatPointSideOflink(poi.getGeometry(), geom, tmpPoint, true);
				if (poi.getLinkPid() != linkPid) {
					poi.setLinkPid(linkPid);
				}
				if (poi.getXGuide() != nearestPoint.x || poi.getYGuide() != nearestPoint.y) {
					poi.setXGuide(nearestPoint.x);
					poi.setYGuide(nearestPoint.y);
				}
				if (poi.getSide() != side) {
					poi.setSide(side);
				}
				if (poi.getPmeshId() != meshId) {
					poi.setPmeshId(meshId);
				}
				if (poi.getNameGroupid() != 0) {
					poi.setNameGroupid(0);
				}
			}

		}
	}

	/**
	 * 新增POI或修改引导坐标或引导link为0的POI对象
	 * 
	 * @param poiObj
	 * @return
	 */

	private boolean isBatch(IxPoiObj poiObj) {
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		// POI新增或引导link为0
		if (poi.getHisOpType().equals(OperationType.INSERT)) {
			return true;
		}
		if (Integer.valueOf(0).equals(poi.getLinkPid())) {
			return true;
		}
		if (NotInRdLink.contains(poi.getPid())) {
			return true;
		}
		// 修改引导坐标
		if (poi.hisOldValueContains(IxPoi.X_GUIDE)) {
			String oldXguide = String.valueOf(poi.getHisOldValue(IxPoi.X_GUIDE));
			String newXguide = String.valueOf(poi.getXGuide());
			if (!oldXguide.equals(newXguide)) {

tLinkPid(0);
				}
			} else {
				long linkPid = linkInfo.getLong("LINK_PID");
				Geometry geom = GeoTranslator.wkt2Geometry(linkInfo.getString("RD_GEOMETRY"));
				int 
				return true;
			}
		}
		if (poi.hisOldValueContains(IxPoi.Y_GUIDE)) {
			String oldYguide = String.valueOf(poi.getHisOldValue(IxPoi.Y_GUIDE));
			String newYguide = String.valueOf(poi.getYGuide());
			if (!oldYguide.equals(newYguide)) {
				return true;
			}
		}
		return false;
	}

}
