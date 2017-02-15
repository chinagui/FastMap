package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FMYW20227
 * @author Han Shaoming
 * @date 2017年2月13日 下午10:18:28
 * @Description TODO
 * 检查条件：    lifecycle！=1
 * 检查原则：
 * 1.收费标准（parkings.tollStd)为5,停车场收费备注(parkings.remark)的值必须包含0，则报log
 */
public class FMYW20227 extends BasicCheckRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiParking> ixPoiParkings = poiObj.getIxPoiParkings();
			//错误数据
			if(ixPoiParkings==null || ixPoiParkings.isEmpty()){return;}
			for (IxPoiParking ixPoiParking : ixPoiParkings) {
				//收费标准
				String tollStd = ixPoiParking.getTollStd();
				if(tollStd == null){return;}
				if("5".equals(tollStd)){
					String remark = ixPoiParking.getRemark();
					if(remark == null){return;}
					if(!remark.contains("0")){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
					}
				}
			}
		}
	}
}
