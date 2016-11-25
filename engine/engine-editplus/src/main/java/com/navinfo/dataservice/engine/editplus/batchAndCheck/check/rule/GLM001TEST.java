package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.obj.IxPoiObj;

public class GLM001TEST extends BasicCheckRule {

	public GLM001TEST() {
		objNameList.add("IX_POI");
		objNameList.add("IX_POI_NAME");
	}

	@Override
	public void runCheck(String objName, BasicObj obj) throws Exception{
		if(objName.equals("IX_POI")){
			IxPoi poiObj=(IxPoi) obj.getMainrow();
			Map<String, Object> oldValueMap=poiObj.getOldValues();
			if(!oldValueMap.containsKey("KIND_CODE")){return;}
			List<BasicRow> subRows=obj.getRowsByName("IX_POI_NAME");
			for(BasicRow br:subRows){
				if(br.getObjType().equals("UPDATE")){
					this.setCheckResult(poiObj.getGeometry(), "[IX_POI,"+poiObj.getPid()+"]", poiObj.getMeshId());
				}
			}
		}else if(objName.equals("IX_POI_NAME")){}
	}

}
