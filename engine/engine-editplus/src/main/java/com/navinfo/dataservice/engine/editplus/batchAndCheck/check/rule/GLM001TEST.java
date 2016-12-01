package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

public class GLM001TEST extends BasicCheckRule {

	public GLM001TEST() {
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception{
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			if(!poi.hisOldValueContains(IxPoi.KIND_CODE)){return;}
			String oldKindCode=(String) poi.getHisOldValue(IxPoi.KIND_CODE);
			if(oldKindCode.isEmpty()){return;}
			List<IxPoiName> subRows=poiObj.getIxPoiNames();
			for(BasicRow br:subRows){
				if(br.getHisOpType().equals(OperationType.UPDATE)){
					//增加方法，传入IxPoiObj对象
					this.setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),"kindcode错误");
				}
			}
		}else if(obj.objName().equals(ObjectName.AD_LINK)){}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
