package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiGasstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FM14Sum1301
 * @author Han Shaoming
 * @date 2017年2月14日 下午12:44:18
 * @Description TODO
 * 检查条件：Lifecycle为“0（无）\1（删除）\4（验证）”不检查；
 * 检查原则：
 * 1、不允许采集停车场深度信息的设施采集了停车场深度信息；
 * 2、不允许采集加油站深度信息的设施采集了加油站深度信息；
 * 3、不允许采集加气站深度信息的设施采集了加气站深度信息；
 * 备注：230210\230213\230214允许采集停车场深度信息；
 *     230215允许采集加油站深度信息；
 *     230216允许采集加气站深度信息；
 *     加油站与加气站的区别在于油品：加油站（0柴油，1汽油，2甲醇汽油，6乙醇汽油）；
 *     加气站（3其他，4液化石油气，5天然气，7氢燃料，8生物柴油，9液化天然气）
 */
public class FM14Sum1301 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode == null){return;}
			//停车场深度信息
			List<IxPoiParking> ixPoiParkings = poiObj.getIxPoiParkings();
			if(ixPoiParkings != null && !ixPoiParkings.isEmpty()){
				if(!"230210".equals(kindCode)&&!"230213".equals(kindCode)&&!"230214".equals(kindCode)){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "不允许采集停车场深度信息的设施采集了停车场深度信息");
				}
			}
			//加油站深度信息
			List<IxPoiGasstation> ixPoiGasStations = poiObj.getIxPoiGasstations();
			if(ixPoiGasStations != null && !ixPoiGasStations.isEmpty()){
				for (IxPoiGasstation ixPoiGasStation : ixPoiGasStations) {
					//燃料类型
					String fuelType = ixPoiGasStation.getFuelType();
					if(fuelType != null){
						//判断为加油站还是加气站
						String oilType = "0126";
						String gasType = "345789";
						boolean oilFlag = true;
						boolean gasFlag = true; 
						for (char type : fuelType.toCharArray()) {
							String typeStr = String.valueOf(type);
							//加油站
							if(!oilType.contains(typeStr)){oilFlag = false;}
							//加气站
							if(!gasType.contains(typeStr)){oilFlag = false;}
						}
						if(oilFlag&&!"230215".equals(kindCode)){
							setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "不允许采集加油站深度信息的设施采集了加油站深度信息");
							return;
						}
						if(gasFlag&&!"230216".equals(kindCode)){
							setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "不允许采集加气站深度信息的设施采集了加气站深度信息");
							return;
						}
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
