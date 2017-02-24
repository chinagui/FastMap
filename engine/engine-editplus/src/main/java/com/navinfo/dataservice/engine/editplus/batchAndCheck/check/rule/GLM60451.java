package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import oracle.net.aso.l;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * GLM60451
 * 检查条件：
 * IX_POI表中"STATE（状态）"非"1（删除）"
 * 检查对象：种别为：230210的POI
 * 检查原则：
 * POI的停车场类型parking_type字段在值域{0,1,2,3,4}范围内，否则报log：停车场类型不在值域范围内！
 * @author zhangxiaoyi
 */
public class GLM60451 extends BasicCheckRule {
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kind=poi.getKindCode();
			if(!kind.equals("230210")){return;}
			List<IxPoiParking> parkings = poiObj.getIxPoiParkings();
			if(parkings==null||parkings.size()==0){return;}
			for(IxPoiParking p:parkings){
				String typeStr=p.getParkingType();
				if(typeStr==null||(typeStr.isEmpty()&&!typeStr.equals("0"))){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
					return;
				}
				if(typeStr.equals("0")||typeStr.equals("1")||typeStr.equals("2")
						||typeStr.equals("3")||typeStr.equals("4")){
					continue;
				}else{
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
					return;
				}		
			}
		}
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
