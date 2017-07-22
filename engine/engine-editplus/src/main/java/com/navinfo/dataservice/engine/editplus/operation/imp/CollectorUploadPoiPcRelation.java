package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @ClassName: CollectorUploadPoiPcRelation
 * @author xiaoxiaowen4127
 * @date 2017年5月8日
 * @Description: CollectorUploadPoiPcRelation.java
 */
public class CollectorUploadPoiPcRelation{
	
	private Map<Long,Set<String>> updateChildren = new HashMap<Long,Set<String>>();//key:pid,value:relateChildren fid set

	private Set<String> childrenFids = new HashSet<String>();
	
	public Map<Long, Set<String>> getUpdateChildren() {
		return updateChildren;
	}
	
	public void addUpdateChildren(long pid,JSONArray ja){
		if(!updateChildren.containsKey(pid)){
			updateChildren.put(pid, new HashSet<String>());
		}
		if(ja!=null){
			for(Object obj:ja){
				JSONObject jObj = (JSONObject)obj;
				String childFid = jObj.getString("childFid");
				updateChildren.get(pid).add(childFid);
				childrenFids.add(childFid);
			}
		}
	}
	
	public Set<String> getChildrenFids(){
		return childrenFids;
	}
}
