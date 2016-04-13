package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdgsc.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

			linksGeometryList.add(geometry);
		}
		
		//获取交点
		Geometry interGeometry = GeometryUtils.getIntersectsGeo(linksGeometryList);
		
		if(interGeometry != null)
		{
			//判断交点是否在矩形框内
			Geometry spatial = GeometryUtils.getPolygonByWKT(command.getWkt());
			
			if(interGeometry.intersects(spatial))
			{
				rdGsc.setPid(PidService.getInstance().applyRdGscPid());
				
				rdGsc.setGeometry(interGeometry);
				
				rdGsc.setProcessFlag(1);
				
				List<IRow> rdGscLinks = new ArrayList<IRow>();
				
				for (Map.Entry<Integer, Integer> entry : linkMap.entrySet()) {
					
					int linkPid = entry.getKey();
					
					int zlevel = entry.getValue();
					
					RdGscLink rdGscLink = new RdGscLink();
					
					rdGscLink.setPid(rdGsc.getPid());
					
					rdGscLink.setZlevel(zlevel);
					
					rdGscLink.setLinkPid(linkPid);
					
					rdGscLinks.add(rdGscLink);
					//TODO 立交的起终点标识
					
				}
				rdGsc.setLinks(rdGscLinks);
				
				result.insertObject(rdGsc, ObjStatus.INSERT, rdGsc.pid());
			}
			else
			{
			}
		}

		return null;
	}

	public static void main(String[] args) throws ParseException {
		Geometry spatial =GeometryUtils.getPolygonByWKT(
				"POLYGON ((116.56526 39.96671, 116.56522 39.96706, 116.56516 39.96757, 116.56512 39.96807, 116.5651 39.96849, 116.5649 39.97102, 116.56465 39.97433, 116.56458 39.97504, 116.56452 39.9758, 116.56451 39.97595, 116.56447 39.97674, 116.56441 39.9778, 116.56429 39.97937, 116.56419 39.98058, 116.56415 39.98121, 116.56411 39.9815, 116.56406 39.98195, 116.564 39.98218, 116.56385 39.9825, 116.5638 39.9826, 116.56372 39.98277, 116.56368 39.98284, 116.5636 39.98296, 116.56338 39.98329, 116.56266 39.98409, 116.56211 39.98468, 116.56191 39.98488, 116.56117 39.98566, 116.56096 39.98588, 116.56017 39.98669, 116.56012 39.98674, 116.55976 39.98714, 116.55885 39.98813, 116.55454 39.99266, 116.55413 39.99306, 116.55378 39.99344, 116.54911 39.99844, 116.54866 39.9989, 116.54859 39.99897, 116.54763 40.0, 116.54742 40.0, 116.54773 39.9997, 116.54847 39.99889, 116.55075 39.99643, 116.55204 39.99506, 116.55322 39.99383, 116.55426 39.99271, 116.55447 39.9925, 116.55494 39.99198, 116.5555 39.99141, 116.5556 39.9913, 116.55613 39.99073, 116.55671 39.99012, 116.55737 39.98941, 116.55758 39.9892, 116.55825 39.98849, 116.55891 39.9878, 116.55972 39.98693, 116.5605 39.98613, 116.56126 39.98532, 116.56187 39.98467, 116.56198 39.98456, 116.56254 39.98392, 116.56273 39.98372, 116.56323 39.98314, 116.56344 39.98291, 116.56356 39.9827, 116.5637 39.9825, 116.56395 39.98184, 116.56405 39.98012, 116.56414 39.97876, 116.56424 39.97756, 116.56434 39.97619, 116.5644 39.97524, 116.56442 39.97475, 116.5645 39.97377, 116.56462 39.97194, 116.56469 39.971, 116.56478 39.96989, 116.56488 39.96861, 116.56498 39.96724, 116.56501 39.96681, 116.56526 39.96671))");
		
		WKTReader reader = new WKTReader();
		Geometry point = reader.read("POINT (16.56516 39.96751)");
		System.out.println(spatial.intersects(point));
	}
}
