package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;

public class GLM001TEST extends BasicBatchRule {

	public GLM001TEST() {
		objNameList.add("IX_POI");
		objNameList.add("IX_POI_NAME");
	}

	@Override
	public void runBatch(String objName, BasicObj obj) {
		if(objName.equals("IX_POI")){
			IxPoi poiObj=(IxPoi) obj.getMainrow();
			Map<String, Object> oldValueMap=poiObj.getOldValues();
			if(!oldValueMap.containsKey("KIND_CODE")){return;}
			poiObj.setKindCode("test124");
			List<BasicRow> subRows=obj.getRowsByName("IX_POI_NAME");
			for(BasicRow br:subRows){
				if(br.getObjType().equals("UPDATE")){
				}
			}
			IxPoiName name=new IxPoiName(0);
			obj.insertSubrow(name);
		}else if(objName.equals("IX_POI_NAME")){}
	}

}
