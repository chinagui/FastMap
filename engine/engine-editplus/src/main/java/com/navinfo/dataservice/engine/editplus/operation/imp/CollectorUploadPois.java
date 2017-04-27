package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.util.HashMap;
import java.util.Map;

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

	public void addJsonPoi(String fid,JSONObject jo){
		int lifecycle = jo.getInt("t_lifecycle");
		if(lifecycle==1){
			deletePois.put(fid, jo);
		}else if(lifecycle==2||lifecycle==3){
			updatePois.put(fid, jo);
		}
	}
}
