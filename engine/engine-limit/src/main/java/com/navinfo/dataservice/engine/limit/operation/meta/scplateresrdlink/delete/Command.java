package com.navinfo.dataservice.engine.limit.operation.meta.scplateresrdlink.delete;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresRdLink;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Command extends AbstractCommand{

	private String requester;
	
	private JSONArray linkpids;
	
	private JSONArray geometryids;
	
	private List<ScPlateresRdLink> rdlinks; 
	
	public Command(JSONObject json, String requester) {
		this.requester = requester;

		if (json.containsKey("linkpids")) {
			this.linkpids = json.getJSONArray("linkpids");
		}

		if (json.containsKey("geometryIds")) {
			this.geometryids = json.getJSONArray("geometryIds");
		}
	}
	
	public JSONArray getLinkpids(){
		return this.linkpids;
	}
	
	public JSONArray getGeometryIds(){
		return this.geometryids;
	}
	
	public void setRdLinks(List<ScPlateresRdLink> value){
		this.rdlinks = value;
	}
	
	public List<ScPlateresRdLink> getRdLinks(){
		return this.rdlinks;
	}
	
	@Override
	public OperType getOperType() {
		// TODO Auto-generated method stub
		return OperType.DELETE;
	}

	@Override
	public DbType getDbType() {
		// TODO Auto-generated method stub
		return DbType.METADB;
	}

	@Override
	public String getRequester() {
		// TODO Auto-generated method stub
		return this.requester;
	}

	@Override
	public LimitObjType getObjType() {
		// TODO Auto-generated method stub
		return LimitObjType.SCPLATERESRDLINK;
	}

}
