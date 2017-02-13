package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * @ClassName FMYW20206
 * @author Han Shaoming
 * @date 2017年2月13日 下午4:09:05
 * @Description TODO
 * 检查条件：     Lifecycle！=1（删除）；
 * 检查原则：
 * 停车收费备注（chargingStation-parkingInfo），设备生产商（chargingPole-manufacturer），出厂编号（chargingPole-factoryNum），
 * 充电桩编号（chargingPole-plotNum），产品型号（chargingPole-productNum），电动车泊位号码（chargingPole-parkingNum）
 * 允许存在“TY_CHARACTER_EGALCHAR_EXT”表，“EXTENTION_TYPE”字段中“GBK”、“ENG_F_U”、“ENG_F_L”、“DIGIT_F”、“SYMBOL_F”
 * 类型对应的“CHARACTER”字段的内容， 如果存在其他字符，将此条POI报出；
 * 充电功率（chargingPole-power），充电电压（chargingPole-voltage），充电电流（chargingPole-current）只能包含全半角阿拉伯数字，否则报log。
 * 提示：充电站停车收费备注（充电桩充电功率，充电桩充电电压，充电桩充电电流，充电桩设备生产商，
 * 充电桩出厂编号，充电桩编号，充电桩产品型号， 充电桩电动车泊位号码）含有非法字符“xx”
 * 备注：检查哪个字段报哪个字段的log；检查时，先把各个字段内容转成全角，再查
 */
public class FMYW20206 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			MetadataApi api=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Map<String, List<String>> map = null;
			//充电站类型
			List<IxPoiChargingstation> ixPoiChargingStations = poiObj.getIxPoiChargingstations();
			if(ixPoiChargingStations !=null && !ixPoiChargingStations.isEmpty()){
				map = api.tyCharacterEgalcharExtGetExtentionTypeMap();
				for (IxPoiChargingstation ixPoiChargingStation : ixPoiChargingStations) {
					//停车收费备注
					String parkingInfo = ixPoiChargingStation.getParkingInfo();
					List<String> errorList = this.isCheck(parkingInfo, map);
					if(errorList.size()>0){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "充电站停车收费备注含有非法字符“"
								+errorList.toString().replace("[", "").replace("]", "")+"”");
						return;
					}
				}
			}
			//充电桩
			List<IxPoiChargingplot> ixPoiChargingPlots = poiObj.getIxPoiChargingplots();
			if(ixPoiChargingPlots!=null && !ixPoiChargingPlots.isEmpty()){
				map = api.tyCharacterEgalcharExtGetExtentionTypeMap();
				for (IxPoiChargingplot ixPoiChargingPlot : ixPoiChargingPlots) {
					//设备生产商
					String manufacturer = ixPoiChargingPlot.getManufacturer();
					List<String> manufacturerErrors = this.isCheck(manufacturer, map);
					if(manufacturerErrors.size()>0){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "充电桩设备生产商含有非法字符“"
								+manufacturerErrors.toString().replace("[", "").replace("]", "")+"”");
						return;
					}
					//出厂编号
					String factoryNum = ixPoiChargingPlot.getFactoryNum();
					List<String> factoryNumErrors = this.isCheck(factoryNum, map);
					if(factoryNumErrors.size()>0){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "充电桩出厂编号含有非法字符“"
								+factoryNumErrors.toString().replace("[", "").replace("]", "")+"”");
						return;
					}
					//充电桩编号
					String plotNum = ixPoiChargingPlot.getPlotNum();
					List<String> plotNumErrors = this.isCheck(plotNum, map);
					if(plotNumErrors.size()>0){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "充电桩编号含有非法字符“"
								+plotNumErrors.toString().replace("[", "").replace("]", "")+"”");
						return;
					}
					//产品型号
					String productNum = ixPoiChargingPlot.getProductNum();
					List<String> productNumErrors = this.isCheck(productNum, map);
					if(productNumErrors.size()>0){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "充电桩产品型号含有非法字符“"
								+productNumErrors.toString().replace("[", "").replace("]", "")+"”");
						return;
					}
					//电动车泊位号码
					String parkingNum = ixPoiChargingPlot.getParkingNum();
					List<String> parkingNumErrors = this.isCheck(parkingNum, map);
					if(parkingNumErrors.size()>0){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "充电桩电动车泊位号码含有非法字符“"
								+parkingNumErrors.toString().replace("[", "").replace("]", "")+"”");
						return;
					}
					//充电功率
					String power = ixPoiChargingPlot.getPower();
					if(CheckUtil.isChinese(power)){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "充电桩充电功率只能包含全半角阿拉伯数字");
						return;
					}
					//充电电压
					String voltage = ixPoiChargingPlot.getVoltage();
					if(CheckUtil.isChinese(voltage)){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "充电桩充电电压只能包含全半角阿拉伯数字");
						return;
					}
					//充电电流
					String curent = ixPoiChargingPlot.getCurrent();
					if(CheckUtil.isChinese(curent)){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "充电桩充电电流只能包含全半角阿拉伯数字");
						return;
					}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * 判断是否为非法字符
	 * @author Han Shaoming
	 * @param str
	 * @return
	 * @throws Exception
	 */
	private List<String> isCheck(String nameSub,Map<String, List<String>> map) throws Exception{
		List<String> errorList=new ArrayList<String>();
		if(nameSub!=null){
			//半角转全角
			String nameSubQ = CheckUtil.strB2Q(nameSub);
			for (char nameSubStr : nameSubQ.toCharArray()) {
				String subStr = String.valueOf(nameSubStr);
				if(!map.get("GBK").contains(subStr)&&!map.get("ENG_F_U").contains(subStr)
						&&!map.get("ENG_F_L").contains(subStr)&&!map.get("DIGIT_F").contains(subStr)
						&&!map.get("SYMBOL_F").contains(subStr)){
					errorList.add(subStr);
				}
			}
		}
		return errorList;
	}
}
