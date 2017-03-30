package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 批处理： 
 * 针对充电站充电桩深度分类(kindCode=（230218，230227）)信息增加半角批处理
 * 批处理字段为：
 * (1)充电站X_POI_CHARGINGSTATION的开发时间OPEN_HOUR和停车收费备注parking_IBFO；
 * (2)充电桩IX_POI_CHARGINGPLOT的充电功率POWER、充电电压VOLTAGE、充电电流CURRENT、充电价格PRICES、
 * 电动车泊位号码PARKING_NUM、设备生产商MANUFACTURER、充电桩编号PLOT_NUM、出厂编号FACTORY_NUM、
 * 产品型号PRODUCT_NUM；
 * 
 * @author wangdongbin
 *
 */
public class FMBAT20206 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}
		String kindcode = poi.getKindCode();
		if ((!kindcode.equals("230218")&&!kindcode.equals("230227"))) {
			return;
		}
		if (kindcode.equals("230218")) {
			List<IxPoiChargingstation> chargingstationList = poiObj.getIxPoiChargingstations();
			if (chargingstationList == null || chargingstationList.size() == 0) {
				return;
			}
			IxPoiChargingstation chargingstation = chargingstationList.get(0);
			if (chargingstation.getOpenHour() != null) {
				chargingstation.setOpenHour(ExcelReader.f2h(chargingstation.getOpenHour()));
			}
			if (chargingstation.getParkingInfo() != null) {
				chargingstation.setParkingInfo(ExcelReader.f2h(chargingstation.getParkingInfo()));
			}
		} else if (kindcode.equals("230227")) {
			List<IxPoiChargingplot> chargingplotList = poiObj.getIxPoiChargingplots();
			for (IxPoiChargingplot poiChargingPlot:chargingplotList) {
				if (poiChargingPlot.getPower() != null) {
					poiChargingPlot.setPower(ExcelReader.f2h(poiChargingPlot.getPower()));
				}
				if (poiChargingPlot.getVoltage() != null) {
					poiChargingPlot.setVoltage(ExcelReader.f2h(poiChargingPlot.getVoltage()));
				}
				if (poiChargingPlot.getCurrent() != null) {
					poiChargingPlot.setCurrent(ExcelReader.f2h(poiChargingPlot.getCurrent()));
				}
				if (poiChargingPlot.getPrices() != null) {
					poiChargingPlot.setPrices(ExcelReader.f2h(poiChargingPlot.getPrices()));
				}
				if (poiChargingPlot.getParkingNum() != null) {
					poiChargingPlot.setParkingNum(ExcelReader.f2h(poiChargingPlot.getParkingNum()));
				}
				if (poiChargingPlot.getManufacturer() != null) {
					poiChargingPlot.setManufacturer(ExcelReader.f2h(poiChargingPlot.getManufacturer()));
				}
				if (poiChargingPlot.getPlotNum() != null) {
					poiChargingPlot.setPlotNum(ExcelReader.f2h(poiChargingPlot.getPlotNum()));
				}
				if (poiChargingPlot.getFactoryNum() != null) {
					poiChargingPlot.setFactoryNum(ExcelReader.f2h(poiChargingPlot.getFactoryNum()));
				}
				if (poiChargingPlot.getProductNum() != null) {
					poiChargingPlot.setProductNum(ExcelReader.f2h(poiChargingPlot.getProductNum()));
				}
			}
		}
		
	}

}
