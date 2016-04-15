package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakadpoint;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.google.gson.JsonArray;
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
import com.vividsolutions.jts.geom.Point;

public class OpTopo implements IOperation {

	private Command command;

	private Check check;
	
	private Connection conn;

	public OpTopo(Command command, Check check, Connection conn) {
		this.command = command;

		this.check = check;
		
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		String msg="";
		this.breakPoint(result);
		return msg;
	}

	/**
	 * 打断点
	 * 
	 * @param link
	 * @throws Exception 
	 */
	private void breakPoint(Result result) throws Exception {
		Point point = command.getPoint();
		double lon = point.getX() * 100000;
		double lat = point.getY() * 100000;
	    JSONArray ja1 = new JSONArray();
		JSONArray ja2 = new JSONArray();
		boolean hasFound = false;
		result.setPrimaryPid(command.getLinkPid());
	    AdLink adLink =(AdLink)new AdLinkSelector(conn).loadById(command.getLinkPid(),true);
	    result.insertObject(adLink, ObjStatus.DELETE, adLink.pid());
	    JSONObject geojson = GeoTranslator.jts2Geojson(adLink
					.getGeometry());
	    JSONArray jaLink = geojson.getJSONArray("coordinates");
	    for(int i =0 ; i < jaLink.size()-1; i++){
	    		JSONArray jaPS = jaLink.getJSONArray(i);
	    		if(i == 0){
	    			ja1.add(jaPS);
	    		}
	    		JSONArray jaPE = jaLink.getJSONArray(i + 1);
	    		if(!hasFound){
		    		if(lon == jaPE.getDouble(0) && lat == jaPE.getDouble(1)){ 
						ja1.add(new double[] { lon, lat });
						hasFound = true;
					}
		    		//打断点在线段上
					else if (GeoTranslator.isIntersection(
							new double[] { jaPS.getDouble(0), jaPS.getDouble(1) },
							new double[] { jaPE.getDouble(0), jaPE.getDouble(1) },
							new double[] { lon, lat })) {
						ja1.add(new double[] { lon, lat });
						ja2.add(new double[] { lon, lat });
						hasFound = true;
					}else{
						if(i>0){
							ja1.add(jaPS);
						}
						
						ja1.add(jaPE);
					}
	    		}else{
	    			ja2.add(jaPS);
	    		}
	    		if (i == jaLink.size() - 2) {
					ja2.add(jaPE);
				}
	    		if (!hasFound) {
	    			throw new Exception("打断的点不在打断LINK上");
	    		}
	    }
	    	
	    this.createLinksForADNode(adLink,ja1,ja2,result);
	    
	}

    
	private void  createLinksForADNode(AdLink adLink,JSONArray sArray,JSONArray eArray,Result result) throws Exception {
		AdNode node = OperateUtils.createAdNode(command.getPoint().getX(), command.getPoint().getY());
		result.insertObject(node, ObjStatus.INSERT, node.pid());
		JSONObject sGeojson = new JSONObject();
		sGeojson.put("type", "LineString");
		sGeojson.put("coordinates", sArray);
		JSONObject eGeojson = new JSONObject();
		eGeojson.put("type", "LineString");
		eGeojson.put("coordinates", eArray);
		AdLink  slink = new AdLink();
		slink.setPid(PidService.getInstance().applyAdLinkPid());
		slink.copy(adLink);
		slink.setGeometry(GeoTranslator.geojson2Jts(sGeojson));
		slink.setLength(GeometryUtils.getLinkLength(GeoTranslator.transform(slink.getGeometry(), 0.00001, 5)));
		command.setsAdLink(slink);
		AdLink  elink = new AdLink();
		elink.setPid(PidService.getInstance().applyAdLinkPid());
		elink.copy(adLink);
		elink.setGeometry(GeoTranslator.geojson2Jts(sGeojson));
		elink.setLength(GeometryUtils.getLinkLength(GeoTranslator.transform(elink.getGeometry(), 0.00001, 5)));
		slink.setStartNodePid(node.getPid());
		elink.setEndNodePid(node.getPid());
		command.seteAdLink(elink);
		result.insertObject(slink, ObjStatus.INSERT, slink.pid());
		result.insertObject(elink, ObjStatus.INSERT, elink.pid());
	}

}
