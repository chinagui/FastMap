package com.navinfo.dataservice.engine.edit.edit.operation.obj.adface.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.commons.util.GeometryUtils;
import com.navinfo.dataservice.commons.util.MeshUtils;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLinkMesh;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNodeMesh;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.comm.util.AdminUtils;
import com.navinfo.dataservice.engine.edit.comm.util.OperateUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Operation implements IOperation {

	private Command command;

	private Check check;
	
	private Connection conn;

	public Operation(Command command, Check check, Connection conn) {
		this.command = command;

		this.check = check;
		
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		//既有线构成面
		if (command.getLinkPids() != null){
			//ADLINK
			if (command.getLinkType().equals(ObjType.ADLINK.toString())){
				List<Geometry> geometrys = new ArrayList<Geometry>();
				List<AdLink>   adlinks = new ArrayList<AdLink>();
				Set<Integer> meshList  = new HashSet<>();
				int meshId=0;
				for (int linkPid:command.getLinkPids()){
					
					AdLink adLink =(AdLink)new AdLinkSelector(conn).loadById(linkPid,true);
					meshId = adLink.getMesh();
					meshList.add(meshId);
					adlinks.add(adLink);
					geometrys.add(adLink.getGeometry());
				}
				int total = 0;
				for (Geometry g :geometrys){
					for (int i = 0; i <g.getCoordinates().length; i++) {
						total +=i;
					}
				}
				double [][][] adPs = new double[command.getLinkPids().size()][total][2];
				
				for (int j=0; j< geometrys.size();j++){
					Coordinate[] cs = geometrys.get(j).getCoordinates();
					double[][] ps = new double[cs.length][2];
					for (int i = 0; i <cs.length; i++) {
						ps[i][0] = cs[i].x;

						ps[i][1] = cs[i].y;
					}
					adPs[j] = ps;
				}
				if (meshList.size() ==1){
					
					AdFace adFace =  new AdFace();
					JSONObject geojson = new JSONObject();
					geojson.put("type", "Polygon");
					geojson.put("coordinates", adPs);
					adFace.setMesh(meshId);
					adFace.setMeshId(meshId);
					Geometry polGeometry = GeoTranslator.geojson2Jts(geojson, 1, 5);
					adFace.setGeometry(polGeometry);
					int facePid = PidService.getInstance().applyAdFacePid();
					adFace.setPid(facePid);
					adFace.setArea(GeometryUtils.getCalculateArea(polGeometry));
					adFace.setPerimeter(GeometryUtils.getLinkLength(polGeometry));
					List<IRow> adFaceTopos  = new ArrayList<IRow>();
					for (int i = 0 ;  i < adlinks.size(); i++){
						AdFaceTopo adFaceTopo =  new  AdFaceTopo();
						adFaceTopo.setFacePid(facePid);
						adFaceTopo.setLinkPid(adlinks.get(i).getPid());
						adFaceTopo.setSeqNum(i);
						adFaceTopos.add(adFaceTopo);
					}
					adFace.setFaceTopos(adFaceTopos);
					result.insertObject(adFace, ObjStatus.INSERT, adFace.getPid());
				}
			}
			//RDLINK
			if(command.getLinkType().equals(ObjType.RDLINK.toString())){
				//需求待定
			}
		}
		//创建
		if(command.getGeometry() != null ){
			 this.createFaceByGeometry(result);	 
		    }
		return null;
	}

	private Set<String> getLinkInterMesh(Geometry linkGeom) throws Exception {
		Set<String> set = new HashSet<String>();

		Coordinate[] cs = linkGeom.getCoordinates();

		for (Coordinate c : cs) {
			set.add(MeshUtils.lonlat2Mesh(c.x, c.y));
		}

		return set;
	}
	public void createFaceByGeometry(Result result) throws Exception{
		Geometry geom = GeoTranslator.geojson2Jts(command.getGeometry(), 1, 5);
		Coordinate sPoint = geom.getCoordinates()[0];
		AdNode sNode = OperateUtils.createAdNode(sPoint.x, sPoint.y);
		result.insertObject(sNode, ObjStatus.INSERT, sNode.pid());
		Coordinate ePoint = geom.getCoordinates()[geom.getCoordinates().length - 1];
		AdNode eNode = OperateUtils.createAdNode(ePoint.x, ePoint.y);
		result.insertObject(eNode, ObjStatus.INSERT, eNode.pid());
		Set<String> meshes = new HashSet<String>();
	    meshes = this.getLinkInterMesh(geom);
	    if (meshes.size() == 1) {
				AdLink link = new AdLink();
				int meshId = Integer.parseInt(meshes.iterator().next());
				link.setPid(PidService.getInstance().applyAdLinkPid());
				result.setPrimaryPid(link.getPid());
				link.setMesh(meshId);
				double linkLength = GeometryUtils.getLinkLength(geom);
				link.setLength(linkLength);
				link.setGeometry(GeoTranslator.transform(geom, 100000, 0));
                link.setStartNodePid(sNode.getPid());
                link.setEndNodePid(eNode.getPid());
                AdLinkMesh  adLinkMesh= new AdLinkMesh();
                adLinkMesh.setLinkPid(link.getPid());
                adLinkMesh.setMeshId(meshId);
                List<IRow> adLinkMeshs = new ArrayList<IRow>();
                 adLinkMeshs.add(adLinkMesh);
                 link.setMeshes(adLinkMeshs);
				 AdFace adFace  = new AdFace();
				 adFace.setPid(PidService.getInstance().applyAdFacePid());
				 adFace.setArea(GeometryUtils.getCalculateArea(geom));
				 adFace.setMeshId(meshId);
				 adFace.setGeometry(GeoTranslator.transform(geom, 100000, 0));
				 adFace.setPerimeter(GeometryUtils.getLinkLength(geom));
				 AdFaceTopo  adFaceTopo = new AdFaceTopo();
				 adFaceTopo.setFacePid(adFace.getPid());
				 adFaceTopo.setMesh(meshId);
				 adFaceTopo.setSeqNum(1);
				 adFaceTopo.setLinkPid(link.getPid());
				 List<IRow> adFaceTopos = new ArrayList<IRow>();
				 adFaceTopos.add(adFaceTopo);
			     adFace.setFaceTopos(adFaceTopos);
			     result.insertObject(link, ObjStatus.INSERT, link.getPid());
				 result.insertObject(adFace, ObjStatus.INSERT, adFace.getPid());
			}
	    }
	public void updateAdFaceGeometry(){}

}
