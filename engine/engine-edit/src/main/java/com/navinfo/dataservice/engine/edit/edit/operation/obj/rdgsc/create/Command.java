package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdgsc.create;

import java.util.HashMap;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 道路交叉关系分为平交和立交。
 * 平交：不同道路在同一平面内的交叉称为平交
 * 立交：不同道路在不同高度上的交叉称为立交
 * @author 张小龙
 *
 */
public class Command extends AbstractCommand {

	private String requester;

	private Map<Integer,RdGscLink> linkMap = new HashMap<Integer,RdGscLink>();
	
	private JSONObject geoObject;

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDGSC;
	}

	@Override
	public String getRequester() {
		return requester;
	}
	
	public Map<Integer, RdGscLink> getLinkMap() {
		return linkMap;
	}

	public void setLinkMap(Map<Integer, RdGscLink> linkMap) {
		this.linkMap = linkMap;
	}

	public JSONObject getGeoObject() {
		return geoObject;
	}

	public void setGeoObject(JSONObject geoObject) {
		this.geoObject = geoObject;
	}

	public void createGlmList() throws Exception {
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));
		JSONObject data = json.getJSONObject("data");
		
		this.geoObject = data.getJSONObject("geometry");
		
		if(data.getJSONArray("linkObjs") instanceof JSONArray)
		{
			JSONArray linkAttrArray = data.getJSONArray("linkObjs");
			
			for(int i = 0;i<linkAttrArray.size();i++)
			{
				JSONObject linkObj = linkAttrArray.getJSONObject(i);
				
				int level = linkObj.getInt("zlevel");
				
				int pid = linkObj.getInt("pid");
				
				String type = linkObj.getString("type");
				
				RdGscLink link = new RdGscLink();
				
				link.setTableName(type);
				
				link.setLinkPid(pid);
				
				link.setZlevel(level);
				
				linkMap.put(level,link);
				
			}
			
		}
		
	}
}
