package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FMYW20210
 * @author Han Shaoming
 * @date 2017年2月13日 下午3:40:17
 * @Description TODO
 * 检查条件：    lifecycle！=1
 * 检查原则：1、充电站（分类为230218）地址为空时，报log：
 */
public class FMYW20210 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode != null && "230218".equals(kindCode)){
				//存在IxPoiAddress
				IxPoiAddress ixPoiAddress=poiObj.getCHAddress();
				if(ixPoiAddress==null){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
				}else{
					String fullname = ixPoiAddress.getFullname();
					//充电站（分类为230218）地址为空时，报log
					if(fullname == null){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
					}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
