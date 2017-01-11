package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;


import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;


/**
 * 检查条件：
	非删除（根据履历判断删除）
	检查原则：
	1.收费标准（IX_POI_PARKING.TOLL_STD)为5,停车场收费备注(IX_POI_PARKING.REMARK)有值，则报log:收费标准为免费，停车场备注不为空。
 * @author gaopengrong
 */
public class FMYW20227 extends BasicCheckRule {
	
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
				String tollStd=parking.getTollStd();
				
				if ("5".equals(tollStd)){
					String remark=parking.getRemark();
					if (StringUtils.isNotEmpty(remark)){
						log = "收费标准为免费，停车场备注不为空。";
						setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
					}
				}

			}

		}
	}
}
