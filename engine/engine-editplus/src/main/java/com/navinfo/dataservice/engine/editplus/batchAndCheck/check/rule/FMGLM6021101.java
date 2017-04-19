package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FMGLM6021101
 * @author Han Shaoming
 * @date 2017年3月1日 上午8:36:36
 * @Description TODO
 * 检查条件：非删除POI
 * 检查原则：官方原始中文名称(name_class=1,name_type=2,lang_code=CHI或CHT)不能为空，
 * 否则报log：NAME（名称）字段不能为空
 */
public class FMGLM6021101 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			IxPoiName ixPoiName = poiObj.getOfficeOriginCHName();
			boolean check = false;
			if(ixPoiName == null){check = true;}
			if(ixPoiName != null){
				String name = ixPoiName.getName();
				if(name == null || name.isEmpty()){check = true;}
			}
			if(check){
				setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
