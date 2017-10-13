package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.json.JSONObject;

/** 
 * @ClassName: CollectorUploadPas
 * 只使用deletePois和updatePas
 * @author zl
 * @date 2017年10月09日
 * @Description: CollectorUploadPas.java
 */
public class CollectorUploadPas extends UploadPas{
	
	public Map<String,Set<String>> gridFids = new HashMap<String,Set<String>>();//key:gridId,values:fid set临时，只是为了跨大区的数据写任务规划统计使用
	
	public void addJsonPa(String fid,JSONObject jo){
		int lifecycle = jo.getInt("t_lifecycle");
		if(lifecycle==1){
			deletePas.put(fid, jo);
		}else if(lifecycle==2||lifecycle==3){
			updatePas.put(fid, jo);
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
