package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FMYW20225
 * @author Han Shaoming
 * @date 2017年2月14日 上午10:06:32
 * @Description TODO
 * 检查条件：
 * 非删除（根据履历判断删除）
 * 检查原则：(收费方式字段：IX_POI_PARKING.TOLL_WAY；支付方式字段：IX_POI_PARKING.PAYMENT)
 * 1.收费方式与支付方式不能同时有值
 * 2.收费方式只有大陆数据才能有值
 * 3.支付方式只能港澳数据才能有值
 */
public class FMYW20225 extends BasicCheckRule {
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
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
				//收费方式
				String tollWay = ixPoiParking.getTollWay();
				//支付方式
				String payment=ixPoiParking.getPayment();
				//1.收费方式与支付方式不能同时有值
				if (StringUtils.isNotEmpty(tollWay)&&StringUtils.isNotEmpty(payment)){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "收费方式与支付方式同时有值");
				}
//				long regionId = poi.getRegionId();
				//2.收费方式只有大陆数据才能有值
//				if (StringUtils.isEmpty(tollWay)&&StringUtils.isEmpty(payment)){
//					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "收费方式与支付方式同时有值");
//				}
				//3.支付方式只能港澳数据才能有值
//				if (StringUtils.isEmpty(tollWay)&&StringUtils.isEmpty(payment)){
//					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "收费方式与支付方式同时有值");
//				}
			}		
		}
	}
}
