package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: CollectorUploadPois
 * 只使用deletePois和updatePois
 * @author xiaoxiaowen4127
 * @date 2017年4月25日
 * @Description: CollectorUploadPois.java
 */
public class CollectorUploadPois extends UploadPois{
	
	public Map<String,Set<String>> gridFids = new HashMap<String,Set<String>>();//key:gridId,values:fid set临时，只是为了跨大区的数据写任务规划统计使用
	
	public void addJsonPoi(String fid,JSONObject jo){
		int lifecycle = jo.getInt("t_lifecycle");
		if(lifecycle==1){
			deletePois.put(fid, jo);
		}else if(lifecycle==2||lifecycle==3){
			updatePois.put(fid, jo);
		}
	}
	
	public void filterErrorFid(List<ErrorLog> errs){
		if(errs==null||errs.size()==0)return;
		for(ErrorLog err:errs){
			for(Set<String> fids:gridFids.values()){
				fids.remove(err.getFid());
			}
		}
	}

	public void filterErrorFid(ErrorLog err){
		for(Set<String> fids:gridFids.values()){
			fids.remove(err.getFid());
		}
	}
}
