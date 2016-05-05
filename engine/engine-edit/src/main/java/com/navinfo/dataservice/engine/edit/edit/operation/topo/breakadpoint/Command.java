package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakadpoint;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * @author  zhaokk 
 * 创建行政区划点基础参数类 
 */
public class Command extends AbstractCommand {
	private String requester;
	private AdLink  sAdLink;
	private AdLink  eAdLink;
	private List<AdFace> faces;
	public List<AdFace> getFaces() {
		return faces;
	}

	public void setFaces(List<AdFace> faces) {
		this.faces = faces;
	}

	public AdLink getsAdLink() {
		return sAdLink;
	}

	public void setsAdLink(AdLink sAdLink) {
		this.sAdLink = sAdLink;
	}

	public AdLink geteAdLink() {
		return eAdLink;
	}

	public void seteAdLink(AdLink eAdLink) {
		this.eAdLink = eAdLink;
	}

	public List<AdFaceTopo> getAdFaceTopos() {
		return adFaceTopos;
	}

	public void setAdFaceTopos(List<AdFaceTopo> adFaceTopos) {
		this.adFaceTopos = adFaceTopos;
	}

	private GeometryFactory geometryFactory = new GeometryFactory();
	private Point point;
	private int linkPid;
	private List<AdFaceTopo> adFaceTopos;

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.ADNODE;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) throws Exception{
		this.requester = requester;
		this.setProjectId(json.getInt("projectId"));
		this.linkPid = json.getInt("objId");
		JSONObject data = json.getJSONObject("data");
		double lng = Math.round(data.getDouble("longitude")*100000)/100000.0;
		double lat = Math.round(data.getDouble("latitude")*100000)/100000.0;
		Coordinate coord = new Coordinate(lng, lat);
		this.eAdLink = new AdLink();
		this.sAdLink = new AdLink();
		this.point = geometryFactory.createPoint(coord);
	}

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	public void createGlmList() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
