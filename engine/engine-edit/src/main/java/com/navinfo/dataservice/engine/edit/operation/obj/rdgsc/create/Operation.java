package com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.engine.edit.utils.RdGscOperateUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

/**
 * 新增立交操作类 两条link相交必须做立交或者打断
 * 
 * @author 张小龙
 *
 */
public class Operation implements IOperation {

	protected Logger log = Logger.getLogger(this.getClass());

	private Command command;

	private Connection conn;

	private Check check;

	// 立交组成link对象的map集合
	private Map<Integer, IRow> linkObjMap;

	// 获取交点（可能包含多个交点）
	private Geometry interGeometry = null;

	private boolean isSelfGsc = false;

	private Result result;

	private boolean selfGscHasUpdate;

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

		List<Geometry> linksGeometryList = new ArrayList<Geometry>();

		linkObjMap = RdGscOperateUtils.handleLink(command.getLinkMap(), linksGeometryList, conn);

		return linksGeometryList;
	}

	@Override
	public String run(Result result) throws Exception {

		this.result = result;

		log.info("开始创建立交");

		// link的pid和层级的映射关系:key:zlevel value:link对象(pid和线类型)
		Map<Integer, RdGscLink> linkMap = command.getLinkMap();

		// 立交组成线分两种：1.一条link组成线 2.多条link组成线
		if (linkMap.size() < 1) {
			log.error("传递参数有问题：没有立交组成线");
			throw new Exception("没有立交组成线");
		}

		// 判断是否自相交
		isSelfGsc = RdGscOperateUtils.checkIsSelfGsc(linkMap);

		log.info("组装新建立交需要的数据");
		// 查询数据：1.link_pid和对象map 2.立交组成线几何的集合
		List<Geometry> linksGeometryList = preParedData();

		if (isSelfGsc) {
			// 处理自相交立交交点
			interGeometry = GeometryUtils.getInterPointFromSelf(linksGeometryList.get(0));
		} else {
			// 不同类型要素的多条link相交建立立交计算交点
			interGeometry = GeometryUtils.getIntersectsGeo(linksGeometryList);
		}

		createGsc();

		return null;
	}

	/**
	 * 创建立交
	 * 
	 * @throws Exception
	 */
	private void createGsc() throws Exception {

		Geometry gscGeo = checkHasInter(interGeometry);

		RdGsc rdGsc = RdGscOperateUtils.addRdGsc(gscGeo);

		log.info("新建立交组成线");

		for (Entry<Integer, RdGscLink> entry : command.getLinkMap().entrySet()) {

			int level = entry.getKey();

			RdGscLink gscLink = entry.getValue();

			gscLink.setPid(rdGsc.getPid());

			// row是link对象非Rdgsclink对象
			IRow row = linkObjMap.get(level);

			// 更新立交组成线几何
			updateLinkGeo(gscLink, row, gscGeo);

			if (!gscLink.changedFields().isEmpty()) {
				int seqNum = (int) gscLink.changedFields().get("shpSeqNum");

				gscLink.setShpSeqNum(seqNum);

				result.insertObject(gscLink, ObjStatus.INSERT, gscLink.getPid());
			}
		}
		result.setPrimaryPid(rdGsc.getPid());

		result.insertObject(rdGsc, ObjStatus.INSERT, rdGsc.pid());
	}

	/**
	 * 立交检查
	 * 
	 * @param interGeo
	 * @return 立交交点
	 * @throws Exception
	 */
	private Geometry checkHasInter(Geometry interGeo) throws Exception {
		Geometry gscGeo = null;

		if (interGeo != null && !interGeo.isEmpty()) {

			// 矩形框
			Geometry spatial = GeoTranslator.transform(GeoTranslator.geojson2Jts(command.getGeoObject()), 100000, 0);

			// 立交组成线和矩形框交点
			gscGeo = interGeo.intersection(spatial);
			
			Geometry gscPoint = command.getGscPoint();
					
			if(gscPoint != null && gscGeo.getNumGeometries() >=1)
			{
				Geometry minDistinceGeo  = gscGeo.getGeometryN(0);
				
				double minDistince = gscPoint.distance(minDistinceGeo);
				
				for(int i = 1;i<gscGeo.getNumGeometries();i++)
				{
					Geometry pointTmp = gscGeo.getGeometryN(i);
					
					double distince = gscPoint.distance(pointTmp);
					
					if(distince < minDistince)
					{
						minDistince = distince;
						
						minDistinceGeo = pointTmp;
					}
				}
				
				gscGeo = minDistinceGeo;
			}

			// 立交检查：1.点位是否重复 2.是否和矩形框有交点
			check.checkGsc(gscGeo, command.getLinkMap());

			// 立交检查：1.检查立交组成线是否正确
			check.checkGscLink(gscGeo, linkObjMap);

			// 创建立交
		} else {
			throw new Exception("组成Link没有交点");
		}
		return gscGeo;
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
	private void handleOtherGscLink(RdGscLink gscLink, Result result, Coordinate[] linkCoor) throws Exception {

		RdGscSelector selector = new RdGscSelector(conn);
		
		List<RdGsc> rdGscList = selector.onlyLoadTargetRdGscLink(gscLink.getLinkPid(), gscLink.getTableName(), true);

		for (RdGsc gsc : rdGscList) {

			List<IRow> gscLinkList = gsc.getLinks();

			boolean flag = RdGscOperateUtils.checkIsSelfInter(gscLinkList);
			
			// 处理对于立交的影响
			RdGscOperateUtils.handleInterEffect(flag, gsc, linkCoor, result);
		}

	}

	/**
	 * 更新组成线link的几何
	 * 
	 * @param gscLink
	 * @param row
	 * @param gscGeo
	 * @throws Exception
	 */
	private void updateLinkGeo(RdGscLink gscLink, IRow row, Geometry gscGeo) throws Exception {

		Coordinate[] linkCoor = null;

		if (row instanceof RdLink) {
			RdLink linkObj = (RdLink) row;

			JSONObject jsonObj = calcLinkGeo(gscLink, linkObj.getGeometry(), gscGeo);

			JSONObject updateContent = new JSONObject();

			updateContent.put("geometry", jsonObj);

			boolean changed = linkObj.fillChangeFields(updateContent);

			if (changed) {
				result.insertObject(linkObj, ObjStatus.UPDATE, linkObj.pid());
			}

			linkCoor = GeoTranslator.geojson2Jts(jsonObj, 100000, 0).getCoordinates();
		}
		if (row instanceof RwLink) {
			RwLink linkObj = (RwLink) row;

			JSONObject jsonObj = calcLinkGeo(gscLink, linkObj.getGeometry(), gscGeo);

			JSONObject updateContent = new JSONObject();

			updateContent.put("geometry", jsonObj);

			boolean changed = linkObj.fillChangeFields(updateContent);

			if (changed) {
				result.insertObject(linkObj, ObjStatus.UPDATE, linkObj.pid());
			}

			linkCoor = GeoTranslator.geojson2Jts(jsonObj, 100000, 0).getCoordinates();

		}
		
		if (row instanceof LcLink) {
			LcLink linkObj = (LcLink) row;

			JSONObject jsonObj = calcLinkGeo(gscLink, linkObj.getGeometry(), gscGeo);

			JSONObject updateContent = new JSONObject();

			updateContent.put("geometry", jsonObj);

			boolean changed = linkObj.fillChangeFields(updateContent);

			if (changed) {
				result.insertObject(linkObj, ObjStatus.UPDATE, linkObj.pid());
			}

			linkCoor = GeoTranslator.geojson2Jts(jsonObj, 100000, 0).getCoordinates();

		}

		// 计算立交点序号和起终点标识
		RdGscOperateUtils.calShpSeqNum(gscLink, gscGeo, linkCoor);

		// 更新线上其他立交的形状点号
		handleOtherGscLink(gscLink, result, linkCoor);
	}

	/**
	 * 根据立交点计算并更新组成线几何
	 * 
	 * @param gscLink
	 * @param geometry
	 * @param gscGeo
	 * @return 组成线几何
	 * @throws Exception
	 */
	private JSONObject calcLinkGeo(RdGscLink gscLink, Geometry geometry, Geometry gscGeo) throws Exception {
		JSONObject jsonObj = null;

		if (isSelfGsc) {
			jsonObj = RdGscOperateUtils.updateLinkGeoBySelf(gscLink, geometry, gscGeo);
		} else {
			jsonObj = RdGscOperateUtils.updateLinkGeo(gscLink, geometry, gscGeo);
		}

		return jsonObj;
	}
}
