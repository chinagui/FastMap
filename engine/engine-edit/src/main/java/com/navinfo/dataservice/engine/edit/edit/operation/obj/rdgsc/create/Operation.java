package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdgsc.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.commons.util.GeometryUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 新增立交操作类
 * 
 * @author 张小龙
 *
 */
public class Operation implements IOperation {

	private Command command;

	private Connection conn;
	
	private Check check;

	public Operation(Command command, Check check,Connection conn) {
		this.command = command;
		
		this.check = check;
		
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		RdGsc rdGsc = new RdGsc();

		// link的pid和层级的映射关系
		Map<Integer, Integer> linkMap = command.getLinkMap();
		
		if(linkMap.size() <2)
		{
			throw new Exception("立交线少于两条");
		}

		// link对象的map集合
		Map<Integer, RdLink> linkObjMap = new HashMap<>();

		RdLinkSelector linkSelector = new RdLinkSelector(conn);

		List<Geometry> linksGeometryList = new ArrayList<Geometry>();

		for (Integer linkPid : linkMap.keySet()) {
			RdLink link = (RdLink) linkSelector.loadById(linkPid, true);

			Geometry geometry = link.getGeometry();

			geometry.setUserData(linkPid);

			linkObjMap.put(link.getPid(), link);

			linksGeometryList.add(geometry);
		}

		// 获取交点（不考虑矩形框可能有多个交点）
		Geometry interGeometry = GeometryUtils.getIntersectsGeo(linksGeometryList);

		if (interGeometry != null && !interGeometry.isEmpty()) {
			/**
			 * 判断交点是否在矩形框内 1.获取矩形框 2.获取交点 3.判断交点是否和矩形框相交，并且只有一个交点
			 */
			Geometry spatial = GeoTranslator
					.transform(GeoTranslator.geojson2Jts(command.getGeoObject()), 100000, 0);

			Geometry gscGeo = interGeometry.intersection(spatial);
			
			boolean flag = check.checkIsHasGsc(gscGeo,command.getLinkMap(),this.conn);
			
			if(flag)
			{
				throw new Exception("同一点位不能重复创建立交");
			}
			if (gscGeo != null && gscGeo.getNumGeometries() == 1) {
				rdGsc.setPid(PidService.getInstance().applyRdGscPid());

				result.setPrimaryPid(rdGsc.getPid());

				rdGsc.setGeometry(gscGeo);

				// 处理标识默认为不处理
				rdGsc.setProcessFlag(1);

				List<IRow> rdGscLinks = new ArrayList<IRow>();

				for (Map.Entry<Integer, Integer> entry : linkMap.entrySet()) {

					int linkPid = entry.getKey();

					int zlevel = entry.getValue();

					RdGscLink rdGscLink = new RdGscLink();

					rdGscLink.setPid(rdGsc.getPid());

					rdGscLink.setTableName("RD_LINK");

					rdGscLink.setZlevel(zlevel);

					rdGscLink.setLinkPid(linkPid);

					// 更新link的形状点
					RdLink linkObj = linkObjMap.get(linkPid);

					Coordinate[] linkCoor = updateLinkGeo(linkObj, gscGeo, result);

					int startEndFlag = GeometryUtils.getStartOrEndType(linkObj.getGeometry(),
							gscGeo);

					rdGscLink.setStartEnd(startEndFlag);

					// 计算SHP_SEQ_NUM
					if (startEndFlag == 1) {
						rdGscLink.setShpSeqNum(0);
					} else if (startEndFlag == 2) {
						rdGscLink.setShpSeqNum(linkCoor.length - 1);
					} else {
						rdGscLink.setShpSeqNum(calcShpSeqNum(gscGeo, linkCoor));
						
						handleOtherGscLink(linkPid,result,linkCoor);
					}

					rdGscLinks.add(rdGscLink);

				}
				rdGsc.setLinks(rdGscLinks);

				result.insertObject(rdGsc, ObjStatus.INSERT, rdGsc.pid());
			} else {
				throw new Exception("矩形框内有且只能有一个交点");
			}
		}
		else
		{
			throw new Exception("组成Link没有交点");
		}

		return null;
	}
	
	/**
	 * 处理link上其他立交的位置序号
	 * @param linkPid
	 * @param result
	 * @param linkCoor
	 * @throws Exception
	 */
	private void handleOtherGscLink(int linkPid, Result result, Coordinate[] linkCoor) throws Exception {
		RdGscSelector selector = new RdGscSelector(conn);
		
		List<RdGsc> rdGscList = selector.onlyLoadRdGscLinkByLinkPid(linkPid, false);
		
		for(RdGsc gsc : rdGscList)
		{
			List<IRow> gscLinkList = gsc.getLinks();
			
			for(IRow gscLink : gscLinkList)
			{
				RdGscLink link = (RdGscLink) gscLink;
				
				if(link.getLinkPid() == linkPid)
				{
					int shpSeqNum = calcShpSeqNum(gsc.getGeometry(), linkCoor);
					
					JSONObject updateContent = new JSONObject();

					updateContent.put("shpSeqNum", shpSeqNum);

					boolean changed = link.fillChangeFields(updateContent);
					if(changed)
					{
						result.insertObject(link, ObjStatus.UPDATE, gsc.getPid());
					}
				}
			}
			
		}
		
	}

	private Coordinate[] updateLinkGeo(RdLink linkObj, Geometry gscGeo, Result result) throws Exception {
		
		//link的几何
		JSONObject geojson = GeoTranslator.jts2Geojson(linkObj.getGeometry());
		
		//立交点的坐标
		double lon = gscGeo.getCoordinate().x;

		double lat = gscGeo.getCoordinate().y;

		JSONArray jaLink = geojson.getJSONArray("coordinates");

		JSONArray ja1 = new JSONArray();

		boolean hasFound = false;
		for (int i = 0; i < jaLink.size() - 1; i++) {

			JSONArray jaPS = jaLink.getJSONArray(i);
			
			if(i == 0)
			{
				ja1.add(jaPS);
			}
			JSONArray jaPE = jaLink.getJSONArray(i + 1);
			if (!hasFound) {
				// 交点和形状点重合
				if (lon == jaPE.getDouble(0) && lat == jaPE.getDouble(1)) {
					hasFound = true;
					if (i == jaLink.size() - 2) {
						ja1.add(jaPE);
					}
				}
				// 交点在线段上
				else if (GeoTranslator.isIntersection(
						new double[] { jaPS.getDouble(0), jaPS.getDouble(1) },
						new double[] { jaPE.getDouble(0), jaPE.getDouble(1) },
						new double[] { lon, lat })) {
					ja1.add(jaPS);
					
					ja1.add(new double[] { lon, lat });
					hasFound = true;
					if (i == jaLink.size() - 2) {
						ja1.add(jaPE);
					}
				} else {
					if(i>0){
						ja1.add(jaPS);
					}
				}

			} else {
				ja1.add(jaPS);
				if (i == jaLink.size() - 2) {
					ja1.add(jaPE);
				}
			}
		}

		JSONObject geojson1 = new JSONObject();

		geojson1.put("type", "LineString");

		geojson1.put("coordinates", ja1);

		JSONObject updateContent = new JSONObject();
		
		//新的link的几何
		JSONObject geoJson = GeoTranslator.jts2Geojson(GeoTranslator.geojson2Jts(geojson1), 0.00001, 5);
		
		updateContent.put("geometry", geoJson);

		boolean changed = linkObj.fillChangeFields(updateContent);

		if(changed)
		{
			result.insertObject(linkObj, ObjStatus.UPDATE, linkObj.pid());
		}
		
		return GeoTranslator.geojson2Jts(geoJson,100000,0).getCoordinates();
	}

	/**
	 * 计算点在link上的形状点序号
	 * @param gscGeo 点
	 * @param linkCoors lin的形状点数组
	 * @return int类型序号
	 */
	private int calcShpSeqNum(Geometry gscGeo, Coordinate[] linkCoors) {

		int result = 1;

		Coordinate gscCoor = gscGeo.getCoordinate();

		for (int i = 0; i < linkCoors.length; i++) {
			Coordinate linkCoor = linkCoors[i];

			if (gscCoor.x == linkCoor.x && gscCoor.y == linkCoor.y) {
				result = i;
				break;
			}
		}

		return result;
	}

}
