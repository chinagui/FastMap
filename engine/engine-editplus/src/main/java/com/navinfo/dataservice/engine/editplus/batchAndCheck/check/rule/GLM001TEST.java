package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectType;

public class GLM001TEST extends BasicCheckRule {

	public GLM001TEST() {
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception{
		if(obj.objType().equals(ObjectType.IX_POI)){
			IxPoi poiObj=(IxPoi) obj.getMainrow();
			if(!poiObj.hisOldValueContains("KIND_CODE")){return;}
			List<BasicRow> subRows=obj.getRowsByName("IX_POI_NAME");
			for(BasicRow br:subRows){
				if(br.getObjType().equals("UPDATE")){
					this.setCheckResult(poiObj.getGeometry(), "[IX_POI,"+poiObj.getPid()+"]", poiObj.getMeshId());
				}
			}
		}else if(obj.objType().equals(ObjectType.AD_LINK)){}
	}

}
