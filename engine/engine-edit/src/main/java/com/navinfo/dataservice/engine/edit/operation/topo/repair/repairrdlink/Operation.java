package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrdlink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.RdLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.batch.AdminIDBatchUtils;
import com.navinfo.dataservice.engine.edit.utils.batch.UrbanBatchUtils;
import com.navinfo.dataservice.engine.edit.utils.batch.ZoneIDBatchUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;

import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	private Map<Integer, List<RdLink>> map;

	public Map<Integer, List<RdLink>> getMap() {
		return map;
	}

	public void setMap(Map<Integer, List<RdLink>> map) {
		this.map = map;
	}

	public Operation(Connection conn, Command command) {

		this.conn = conn;

		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {

		Map<Integer, List<RdLink>> map = new HashMap<Integer, List<RdLink>>();
		List<RdLink> links = new ArrayList<RdLink>();
		Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(GeoTranslator.geojson2Jts(command.getLinkGeom()));
		if (meshes.size() == 1) {
			JSONObject content = new JSONObject();
			result.setPrimaryPid(this.command.getUpdateLink().getPid());
			content.put("geometry", command.getLinkGeom());
			Geometry geo = GeoTranslator.geojson2Jts(command.getLinkGeom());
			double length = 0;
			if (null != geo)
				length = GeometryUtils.getLinkLength(geo);
			content.put("length", length);
			boolean isChanged = this.command.getUpdateLink().fillChangeFields(content);
			if (isChanged) {
				result.insertObject(this.command.getUpdateLink(), ObjStatus.UPDATE, this.command.getUpdateLink().getPid());
			}
			// 拷贝原link，set属性
			RdLink link = new RdLink();
			link.setPid(this.command.getUpdateLink().pid());
			link.copy(this.command.getUpdateLink());

			link.setGeometry(GeoTranslator.geojson2Jts(this.command.getLinkGeom(), 100000, 0));
			links.add(link);
			// 设置Link的urban属性
			UrbanBatchUtils.updateUrban(this.command.getUpdateLink(), link.getGeometry(), conn, result);
			// 设置link的AdminId
			AdminIDBatchUtils.updateAdminID(this.command.getUpdateLink(), link.getGeometry(), conn);
			// 设置link的ZoneId
			ZoneIDBatchUtils.updateZoneID(this.command.getUpdateLink(), link.getGeometry(), conn, result);
		} else {
			Iterator<String> it = meshes.iterator();
			Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
			Geometry g = GeoTranslator.transform(this.command.getUpdateLink().getGeometry(), 0.00001, 5);
			maps.put(g.getCoordinates()[0], this.command.getUpdateLink().getsNodePid());
			maps.put(g.getCoordinates()[g.getCoordinates().length - 1], this.command.getUpdateLink().geteNodePid());
			while (it.hasNext()) {
				String meshIdStr = it.next();
				Geometry geomInter = MeshUtils.linkInterMeshPolygon(
						GeoTranslator.geojson2Jts(command.getLinkGeom()), GeoTranslator.transform(MeshUtils.mesh2Jts(meshIdStr),1,5));
				if(geomInter instanceof GeometryCollection)
				{
					int geoNum = geomInter.getNumGeometries();
					for (int i = 0; i < geoNum; i++) {
						Geometry subGeo = geomInter.getGeometryN(i);
						if (subGeo instanceof LineString) {
							subGeo = GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(subGeo), 1, 5);

							List<RdLink> rdLinkds = RdLinkOperateUtils.getCreateRdLinksWithMesh(geomInter, maps, result,this.command.getUpdateLink());
							links.addAll(rdLinkds);
						}
					}
				}
				else
				{
					geomInter = GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(geomInter), 1, 5);

					List<RdLink> rdLinkds = RdLinkOperateUtils.getCreateRdLinksWithMesh(geomInter, maps, result,this.command.getUpdateLink());
					
					links.addAll(rdLinkds);
				}

				for (RdLink link : links) {
					// 设置Link的urban属性
					UrbanBatchUtils.updateUrban(link, null, conn, result);
					// 设置link的区划号码
					AdminIDBatchUtils.updateAdminID(this.command.getUpdateLink(), link.getGeometry(), conn);
					// 设置link的ZoneId
					ZoneIDBatchUtils.updateZoneID(this.command.getUpdateLink(), link.getGeometry(), conn, result);
				}
			}
			deleteRdLink(result);
		}

		updataRelationObj(this.command.getUpdateLink(), links, result);

		map.put(this.command.getLinkPid(), links);
		this.map = map;
		return null;
	}

	/**
	 * @param result
	 * @throws Exception
	 */
	private void deleteRdLink(Result result) throws Exception {
		result.insertObject(this.command.getUpdateLink(), ObjStatus.DELETE, this.command.getUpdateLink().getPid());
	}

	/**
	 * 维护关联要素
	 *
	 * @throws Exception
	 */
	private void updataRelationObj(RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {

		CalLinkOperateUtils calLinkOperateUtils = new CalLinkOperateUtils();

		List<RdLink> sortLinks = calLinkOperateUtils.sortLink(newLinks);

		if (newLinks.size() == 1) {
			if (!this.command.getOperationType().equals("innerRun")) {
				// 维护同一线
				com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation samelinkOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation(
						this.conn);

				samelinkOperation.repairLink(newLinks.get(0), this.command.getRequester(), result);
			}
		}

		/*
		 * 任何情况均需要处理的元素
		 */
		// 电子眼
		com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.move.Operation eleceyeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.move.Operation(
				this.conn);
		eleceyeOperation.moveEleceye(oldLink, newLinks, result);

		// poi被动维护（引导link，方位）
		com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Operation poiUpdateOption = new com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Operation(
				this.conn);
		poiUpdateOption.updateLinkSideForPoi(oldLink, newLinks, result);

		// 维护点限速坐标
		com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.move.Operation speedlimitOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.move.Operation(
				this.conn);
		speedlimitOperation.moveSpeedlimit(oldLink, newLinks, result);

		// 立交
		com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation gscOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation();

		Map<Integer, Geometry> newLinkMap = new HashMap<Integer, Geometry>();

		for (RdLink link : newLinks) {
			newLinkMap.put(link.getPid(), link.getGeometry());
		}

		gscOperation.repairLink(this.command.getGscList(), newLinkMap, oldLink, result);

		// 维护限高限重
		com.navinfo.dataservice.engine.edit.operation.obj.hgwg.move.Operation hgwgOperation = new com.navinfo.dataservice.engine.edit.operation.obj.hgwg.move.Operation(conn);
		hgwgOperation.moveHgwgLimit(oldLink, newLinks, result);

		/*
		 * 条件以下为仅打断情况下需要处理的元素 (size < 2说明没有进行打断操作)
		 */
		if (newLinks.size() < 2) {
			return;
		}
		// 警示信息
		com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.update.Operation warninginOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.update.Operation(
				this.conn);
		warninginOperation.breakRdLink(oldLink.getPid(), newLinks, result);

		// 维护信号灯
		com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update.Operation trafficSignalOperation = new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update.Operation(
				this.conn);
		trafficSignalOperation.breakRdLink(oldLink.getPid(), newLinks, result);

		// 分岔路提示
		com.navinfo.dataservice.engine.edit.operation.obj.rdse.update.Operation rdSeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdse.update.Operation(
				this.conn);
		rdSeOperation.breakRdSe(result, oldLink.pid(), newLinks);

		// 减速带
		com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.update.Operation rdSpeedbumpOpeartion = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.update.Operation(
				this.conn);
		rdSpeedbumpOpeartion.breakSpeedbump(result, oldLink.getPid(), newLinks);
		// 坡度
		com.navinfo.dataservice.engine.edit.operation.obj.rdslope.update.Operation rdSlopeOpeartion = new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.update.Operation(
				this.conn);
		rdSlopeOpeartion.breakRdLink(oldLink.getPid(), newLinks, result);
		// 顺行
		com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Operation(
				conn);
		operation.breakRdLink(oldLink, sortLinks, result);
		// 维护CRF交叉点
		com.navinfo.dataservice.engine.edit.operation.obj.rdinter.update.Operation rdinterOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.update.Operation(
				this.conn);
		// 维护CRF道路
		com.navinfo.dataservice.engine.edit.operation.obj.rdroad.update.Operation rdRoadOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.update.Operation(
				this.conn);
		rdRoadOperation.breakRdLink(oldLink.getPid(), newLinks, result);

		rdinterOperation.breakRdLink(oldLink, newLinks, result);
		// 维护CRF对象
		com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation rdObjectOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation(
				this.conn);
		rdObjectOperation.breakRdObjectLink(oldLink, newLinks, result);
		// 收费站
		com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update.Operation rdTollgateOpeartion = new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update.Operation(
				this.conn);
		rdTollgateOpeartion.breakRdTollgate(result, oldLink.getPid(), newLinks);

		// 语音引导
		com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.update.Operation voiceguideOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.update.Operation(
				conn);
		voiceguideOperation.breakRdLink(oldLink, sortLinks, result);

		// 维护可变限速关系
		com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.update.Operation variableSpeedOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.update.Operation(
				this.conn);
		variableSpeedOperation.breakLine(oldLink, newLinks, result);

	}

}
