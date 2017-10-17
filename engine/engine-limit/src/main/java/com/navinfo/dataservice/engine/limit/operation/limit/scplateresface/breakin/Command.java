package com.navinfo.dataservice.engine.limit.operation.limit.scplateresface.breakin;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresFace;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand{

	private String requester;
	
	private ScPlateresFace breakface;
	
	private Point breakpoint;
	
	private String geometryId;
	
	private List<ScPlateresFace> newFaces = new ArrayList<>();
	
	public Command(JSONObject json,String requester){
		this.requester = requester;
		
		this.geometryId = json.getString("objId");
		
		JSONObject data = json.getJSONObject("data");
		
		Coordinate coord = new Coordinate(data.getDouble("longitude"),
				data.getDouble("latitude"));

		this.breakpoint = (Point)GeoTranslator.createPoint(coord);
	}
	
	public void setFace(ScPlateresFace value){
		this.breakface = value;
	}
	
	public ScPlateresFace getFace(){
		return this.breakface;
	}
	
	public List<ScPlateresFace> getNewFaces(){
		return this.newFaces;
	}
	
	public Point getBreakpoint(){
		return this.breakpoint;
	}
	
	public String getGeometryId(){
		return this.geometryId;
	}
	
	@Override
	public OperType getOperType() {
		// TODO Auto-generated method stub
		return OperType.BREAK;
	}

	@Override
	public DbType getDbType() {
		// TODO Auto-generated method stub
		return DbType.LIMITDB;
	}

	@Override
	public String getRequester() {
		// TODO Auto-generated method stub
		return this.requester;
	}

	@Override
	public LimitObjType getObjType() {
		// TODO Auto-generated method stub
		return LimitObjType.SCPLATERESFACE;
	}

}
