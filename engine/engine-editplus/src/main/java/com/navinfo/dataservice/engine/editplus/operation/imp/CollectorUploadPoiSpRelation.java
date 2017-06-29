package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @ClassName: CollectorUploadPoiSpRelation
 * @author xiaoxiaowen4127
 * @date 2017年5月8日
 * @Description: CollectorUploadPoiSpRelation.java
 */
public class CollectorUploadPoiSpRelation{
	
	private Set<String> deleteSp = new HashSet<String>();
	private Map<String,String> updateSp = new HashMap<String,String>();
	
	public Set<String> getDeleteSp() {
		return deleteSp;
	}
	public Map<String, String> getUpdateSp() {
		return updateSp;
	}
	public void deletePoiSp(String fid){
		deleteSp.add(fid);
	}
	public void deletePoiSps(Collection<String> fids){
		if(fids!=null){
			deleteSp.addAll(fids);
		}
	}
	public void addUpdatePoiSp(String fid,String sameFid){
		updateSp.put(fid, sameFid);
	}
	public void addUpdatePoiSps(Map<String,String> sps){
		if(sps!=null){
			updateSp.putAll(sps);
		}
	}
}
