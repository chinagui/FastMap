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
 * 检查条件：
 * 非删除（根据履历判断删除）
 * 检查原则：(收费方式字段：IX_POI_PARKING.TOLL_WAY；支付方式字段：IX_POI_PARKING.PAYMENT)
					1.收费方式与支付方式不能同时有值
					2.收费方式只有大陆数据才能有值
					3.支付方式只能港澳数据才能有值
					log1：收费方式与支付方式同时有值
					log2：港澳数据，收费方式不能有值
					log3：大陆数据，支付方式不能有值
 * @author gaopengrong
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
			List<IxPoiParking> parkings = poiObj.getIxPoiParkings();
			String log;
			for(IxPoiParking parking : parkings){
				//对于大陆数据，支付方式不能有值。
				String payment=parking.getPayment();
				if (StringUtils.isNotEmpty(payment)){
					log = "大陆数据，支付方式不能有值";
					setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
				}
			}		
		}
	}
}
