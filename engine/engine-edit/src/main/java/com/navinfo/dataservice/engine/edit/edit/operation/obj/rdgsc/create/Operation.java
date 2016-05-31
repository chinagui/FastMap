package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdgsc.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.pidservice.PidService;
import com.navinfo.dataservice.engine.edit.comm.util.operate.RdGscOperateUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 新增立交操作类 两条link相交必须做立交或者打断
 * 
 * @author 张小龙
 *
 */
public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	private Check check;

	// 立交组成link对象的map集合
	private Map<Integer, RdLink> linkObjMap;

	// 获取交点（可能包含多个交点）
	private Geometry interGeometry = null;

	private boolean isSelfGsc = false;

	public Operation(Command command, Check check, Connection conn) {
		this.command = command;

		this.check = check;

		this.conn = conn;
	}

	/**
	 * 查询link对象和几何信息
	 * 
	 * @return link对象和几何信息
	 * @throws Exception
	 */
	private List<Geometry> preParedData() throws Exception {
		linkObjMap = new HashMap<>();

		RdLinkSelector linkSelector = new RdLinkSelector(conn);

		List<Geometry> linksGeometryList = new ArrayList<Geometry>();

		for (Integer linkPid : command.getLinkMap().values()) {
			if (!linkObjMap.containsKey(linkPid)) {
				RdLink link = (RdLink) linkSelector.loadById(linkPid, true);

				Geometry geometry = link.getGeometry();

				geometry.setUserData(linkPid);

				linkObjMap.put(link.getPid(), link);

				linksGeometryList.add(geometry);
			}
		}

		return linksGeometryList;
	}

	@Override
	public String run(Result result) throws Exception {

		// link的pid和层级的映射关系:key-》zlevel value:link_pid
		Map<Integer, Integer> linkMap = command.getLinkMap();

		// 立交组成线分两种：1.一条link组成线 2.多条link组成线
		if (linkMap.size() < 1) {
			throw new Exception("没有立交组成线");
		}

		// 查询数据：1.link_pid和对象map 2.立交组成线几何的集合
		List<Geometry> linksGeometryList = preParedData();

		// 一条link自相交建立立交
		if (linkObjMap.size() == 1) {

			isSelfGsc = true;

			interGeometry = GeometryUtils.getInterPointFromSelf(linksGeometryList.get(0));
		}
		// 多条link相交建立立交
		if (linkObjMap.size() >= 2) {
			interGeometry = GeometryUtils.getIntersectsGeo(linksGeometryList);
		}
		if (interGeometry != null && !interGeometry.isEmpty()) {

			// 矩形框
			Geometry spatial = GeoTranslator.transform(GeoTranslator.geojson2Jts(command.getGeoObject()), 100000, 0);

			// 立交组成线和矩形框交点
			Geometry gscGeo = interGeometry.intersection(spatial);

			// 立交检查：1.点位是否重复 2.是否和矩形框有交点
			check.checkGsc(gscGeo,command.getLinkMap());

			// 创建立交
			createGsc(result, gscGeo);
		} else {
			throw new Exception("组成Link没有交点");
		}

		return null;
	}

	

	/**
	 * 创建立交
	 * 
	 * @param result
	 *            结果集
	 * @param gscGeo
	 *            立交点几何
	 * @throws Exception
	 */
	private void createGsc(Result result, Geometry gscGeo) throws Exception {

		RdGsc rdGsc = new RdGsc();

		rdGsc.setPid(PidService.getInstance().applyRdGscPid());

		result.setPrimaryPid(rdGsc.getPid());

		rdGsc.setGeometry(gscGeo);

		// 处理标识默认为不处理
		rdGsc.setProcessFlag(1);

		List<IRow> rdGscLinks = new ArrayList<IRow>();
		
		boolean hasUpdated = false;
		
		Coordinate[] linkCoor = null;

		for (Map.Entry<Integer, Integer> entry : command.getLinkMap().entrySet()) {

			int zlevel = entry.getKey();

			int linkPid = entry.getValue();

			RdGscLink rdGscLink = new RdGscLink();

			rdGscLink.setPid(rdGsc.getPid());

			rdGscLink.setTableName("RD_LINK");

			rdGscLink.setZlevel(zlevel);

			rdGscLink.setLinkPid(linkPid);

			RdLink linkObj = linkObjMap.get(linkPid);

			// 更新link的形状点
			if(!hasUpdated)
			{
				linkCoor = updateLinkGeo(linkObj, gscGeo, result);
				if(isSelfGsc)
				{
					hasUpdated = true;
				}
			}
			
			// 获取link起终点标识
			int startEndFlag = GeometryUtils.getStartOrEndType(linkObj.getGeometry(), gscGeo);

			rdGscLink.setStartEnd(startEndFlag);

			// 计算形状点号：SHP_SEQ_NUM
			if (startEndFlag == 1) {
				rdGscLink.setShpSeqNum(0);
			} else if (startEndFlag == 2) {
				rdGscLink.setShpSeqNum(linkCoor.length - 1);
			} else {
				List<Integer> shpSeqNumList = RdGscOperateUtils.calcShpSeqNum(gscGeo, linkCoor);

				// 自相交情况
				if (isSelfGsc) {
					rdGscLink.setShpSeqNum(shpSeqNumList.get(zlevel));
				} else {
					rdGscLink.setShpSeqNum(shpSeqNumList.get(0));
				}

				// 更新线上其他立交的形状点号
				handleOtherGscLink(linkPid, result, linkCoor);
			}

			rdGscLinks.add(rdGscLink);

		}
		rdGsc.setLinks(rdGscLinks);

		result.insertObject(rdGsc, ObjStatus.INSERT, rdGsc.pid());

	}

	/**
	 * 处理link上其他立交的位置序号
	 * 
	 * @param linkPid
	 * @param result
	 * @param linkCoor
	 *            线上新的几何点
	 * @throws Exception
	 */
	private void handleOtherGscLink(int linkPid, Result result, Coordinate[] linkCoor) throws Exception {

		RdGscSelector selector = new RdGscSelector(conn);

		List<RdGsc> rdGscList = selector.onlyLoadRdGscLinkByLinkPid(linkPid, false);

		for (RdGsc gsc : rdGscList) {

			List<IRow> gscLinkList = gsc.getLinks();

			boolean flag = RdGscOperateUtils.checkIsSelfInter(gscLinkList);

			// 处理对于立交的影响
			RdGscOperateUtils.handleInterEffect(flag, gsc, linkCoor, result);
		}

	}

	/**
	 * 更新link几何信息
	 * 
	 * @param linkObj
	 *            link对象
	 * @param gscGeo
	 *            立交交点
	 * @param result
	 * @return 新的link的形状点
	 * @throws Exception
	 */
	private Coordinate[] updateLinkGeo(RdLink linkObj, Geometry gscGeo, Result result) throws Exception {

		// link的几何
		JSONObject geojson = GeoTranslator.jts2Geojson(linkObj.getGeometry());

		JSONArray ja1 = null;

		if (isSelfGsc) {
			ja1 = RdGscOperateUtils.calCoordinateBySelfInter(geojson, gscGeo);
		} else {
			ja1 = RdGscOperateUtils.calCoordinateByNotSelfInter(geojson, gscGeo);
		}

		JSONObject geojson1 = new JSONObject();

		geojson1.put("type", "LineString");

		geojson1.put("coordinates", ja1);

		JSONObject updateContent = new JSONObject();

		// 新的link的几何
		JSONObject geoJson = GeoTranslator.jts2Geojson(GeoTranslator.geojson2Jts(geojson1), 0.00001, 5);

		updateContent.put("geometry", geoJson);

		boolean changed = linkObj.fillChangeFields(updateContent);

		if (changed) {
			result.insertObject(linkObj, ObjStatus.UPDATE, linkObj.pid());
		}

		return GeoTranslator.geojson2Jts(geoJson, 100000, 0).getCoordinates();
	}

}
