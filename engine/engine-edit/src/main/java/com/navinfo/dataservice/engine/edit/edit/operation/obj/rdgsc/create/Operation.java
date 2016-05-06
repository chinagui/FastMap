package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdgsc.create;

import java.sql.Connection;
import java.util.ArrayList;
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
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * 新增立交操作类
 * @author 张小龙
 *
 */
public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Command command, Connection conn) {
		this.command = command;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		RdGsc rdGsc = new RdGsc();

		Map<Integer, Integer> linkMap = command.getLinkMap();

		RdLinkSelector linkSelector = new RdLinkSelector(conn);
		
		List<Geometry> linksGeometryList = new ArrayList<Geometry>();

		for (Integer linkPid : linkMap.keySet()) {
			RdLink link = (RdLink) linkSelector.loadById(linkPid, true);

			Geometry geometry = link.getGeometry();
			
			geometry.setUserData(linkPid);

			linksGeometryList.add(geometry);
		}
		
		//获取交点（不考虑矩形框可能有多个交点）
		Geometry interGeometry = GeometryUtils.getIntersectsGeo(linksGeometryList);
		
		if(interGeometry != null)
		{
			/**
			 * 判断交点是否在矩形框内
			 * 1.获取矩形框
			 * 2.获取交点
			 * 3.判断交点是否和矩形框相交，并且只有一个交点
			 */
			Geometry spatial = GeoTranslator.transform(GeoTranslator.geojson2Jts(command.getGeoObject()), 100000, 0);
			
			Geometry gscGeo = interGeometry.intersection(spatial);
			
			if(gscGeo != null && gscGeo.getNumGeometries() == 1)
			{
				rdGsc.setPid(PidService.getInstance().applyRdGscPid());
				
				result.setPrimaryPid(rdGsc.getPid());
				
				rdGsc.setGeometry(gscGeo);
				
				//首选需要计算出唯一的交点才能计算是否是起始点或者形状点
				Map<Object, Integer> startEndMap = GeometryUtils.getStartOrEndType(linksGeometryList, gscGeo);
				
				//处理标识默认为不处理
				rdGsc.setProcessFlag(1);
				
				List<IRow> rdGscLinks = new ArrayList<IRow>();
				
				for (Map.Entry<Integer, Integer> entry : linkMap.entrySet()) {
					
					int linkPid = entry.getKey();
					
					int zlevel = entry.getValue();
					
					RdGscLink rdGscLink = new RdGscLink();
					
					rdGscLink.setPid(rdGsc.getPid());
					
					rdGscLink.setZlevel(zlevel);
					
					rdGscLink.setLinkPid(linkPid);
					
					rdGscLink.setStartEnd(startEndMap.get(linkPid));
					
					rdGscLinks.add(rdGscLink);
					
				}
				rdGsc.setLinks(rdGscLinks);
				
				result.insertObject(rdGsc, ObjStatus.INSERT, rdGsc.pid());
			}
			else
			{
				return "矩形框内有且只有一个交点";
			}
		}

		return null;
	}

}
