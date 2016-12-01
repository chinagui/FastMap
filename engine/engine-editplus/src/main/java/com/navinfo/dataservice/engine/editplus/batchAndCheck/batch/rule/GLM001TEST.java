package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

public class GLM001TEST extends BasicBatchRule {

	public GLM001TEST() {
	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			if(!poi.hisOldValueContains(IxPoi.KIND_CODE)){return;}
			String oldKindCode=(String) poi.getHisOldValue(IxPoi.KIND_CODE);
			if(!oldKindCode.isEmpty()){poi.setKindCode("test124");}
			List<IxPoiName> subRows=poiObj.getIxPoiNames();
			for(IxPoiName br:subRows){
				if(br.getHisOpType().equals(OperationType.UPDATE)){
					poiObj.deleteSubrow(br);
				}
			}
			IxPoiObj ixpoiObj = (IxPoiObj)obj;
			IxPoiName name = ixpoiObj.createIxPoiName();
			name.setLangCode("CHI");
		}else if(obj.objName().equals(ObjectName.AD_LINK)){}
	}

}
