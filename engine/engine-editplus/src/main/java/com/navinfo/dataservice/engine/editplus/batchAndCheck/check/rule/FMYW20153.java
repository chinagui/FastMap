package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FMYW20153
 * @author Han Shaoming
 * @date 2017年2月13日 下午8:49:00
 * @Description TODO
 * 检查条件：  非删除POI对象且非鲜度验证数据
 * 检查原则：
 * 除下列两种逻辑组合关系外所有的组合方式都报出LOG：
 * 1、充电桩（230227）插头类型为{0,1,3,5,8,10}时，充电机（桩）交/直流电为{0}，充电模式为{0}；
 * 2、充电桩（230227）插头类型为{2,4,6,7,8,10}时，充电机（桩）交/直流电为{1}，充电模式为{1}。
 */
public class FMYW20153 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode != null && "230227".equals(kindCode)){
				boolean check = false;
				List<IxPoiChargingplot> ixPoiChargingPlots = poiObj.getIxPoiChargingplots();
				if(ixPoiChargingPlots == null || ixPoiChargingPlots.isEmpty()){return;}
				for (IxPoiChargingplot ixPoiChargingPlot : ixPoiChargingPlots) {
					//插头类型
					String plugType = ixPoiChargingPlot.getPlugType();
					//交直流电充电
					int acdc = ixPoiChargingPlot.getAcdc();
					//充电模式
					int mode = ixPoiChargingPlot.getMode();
					String type1 = "0,1,3,5,8,10";
					List<Integer> list1 = StringUtils.getIntegerListByStr(type1);
					String type2 = "2,4,6,7,8,10";
					List<Integer> list2 = StringUtils.getIntegerListByStr(type2);
					String type3 = "8,10";
					List<Integer> list3 = StringUtils.getIntegerListByStr(type3);
					String[] types = plugType.split("\\|");
					for (String string : types) {
						int str = Integer.valueOf(string);
						//充电桩（230227）插头类型为{0,1,3,5,8,10}
						if(!list1.contains(str)){
							//插头类型为{2,4,6,7,8,10}
							if(!list2.contains(str)){
								check = true;
								break;
							}else{
								//充电机（桩）交/直流电为{1}，充电模式为{1}
								if(acdc != 1 || mode != 1){
									check = true;
									break;
								}
							}
						}else{
							//判断是否为8和10
							if(!list3.contains(str)){
								//充电机（桩）交/直流电为{0}，充电模式为{0}
								if(acdc != 0 || mode != 0){
									check = true;
									break;
								}
							}else if(list3.contains(str)){
								if(!(acdc == 0 && mode == 0)&&!(acdc == 1 && mode == 1)){
									check = true;
									break;
								}
							}
						}
					}
				}
				if(check){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
