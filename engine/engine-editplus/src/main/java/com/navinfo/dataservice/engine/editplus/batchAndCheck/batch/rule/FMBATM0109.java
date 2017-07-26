package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiNameFlag;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/** 
* @ClassName: FMBATM0109 
* @author: z
* @date: 2017年7月25日
* @Desc: FMBATM0109.java
* 批处理对象：存在官方标准英文名称的POI对象
批处理原则：将IX_POI_NAME_FLAG中官方原始英文的NAME_ID的值改为该POI对象的官方标准英文名称对应的NAME_ID；
*/
public class FMBATM0109 extends BasicBatchRule{
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoiName standardEngName = poiObj.getOfficeStandardEngName();
		if(standardEngName != null){
			IxPoiName originEngName = poiObj.getOfficeOriginEngName();
			List<IxPoiNameFlag> ixPoiNameFlags = poiObj.getIxPoiNameFlags();
			for (IxPoiNameFlag ixPoiNameFlag: ixPoiNameFlags){
				long nameId = ixPoiNameFlag.getNameId();
				long standardNameId = standardEngName.getNameId();
				if(nameId != standardNameId){
					if(originEngName != null){
						long originNameId = originEngName.getNameId();
						if(nameId == originNameId){
							ixPoiNameFlag.setNameId(standardNameId);
						}
					}
				}
			}
		}
	}
}
