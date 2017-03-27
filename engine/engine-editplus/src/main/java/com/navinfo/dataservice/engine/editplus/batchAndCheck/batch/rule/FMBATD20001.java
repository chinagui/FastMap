package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiGasstation;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 查询条件，满足以下条件之一，即执行批处理：
 * (1)新增加油站(分类为230215)且存在加油站信息
 * (2)修改加油站POI对象（分类为230215）且修改加油站信息 
 * 批处理：
 * (1)如果加油站汽油类型(OIL_TYPE)存在值，则加油站燃油类型FUEL_TYPE赋值1；
 * (2)如果加油站乙醇汽油类型(EG_TYPE)存在值，则加油站燃油类型FUEL_TYPE赋值6；
 * (3)如果加油站甲醇汽油类型(MG_TYPE)存在值，则加油站燃油类型FUEL_TYPE赋值2；
 * (4)如果以上（1）（2）（3）多个条件满足时，则加油站燃油类型FUEL_TYPE分别赋值，多个以“|”分隔(举例：1|2|6)
 * 
 * @author wangdongbin
 *
 */
public class FMBATD20001 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE) || !poi.getKindCode().equals("230215")) {
			return;
		}
		List<IxPoiGasstation> gasstationsList= poiObj.getIxPoiGasstations();
		for (IxPoiGasstation gasstation:gasstationsList) {
//			if (!gasstation.getHisOpType().equals(OperationType.INSERT) && !gasstation.getHisOpType().equals(OperationType.UPDATE)) {
//				continue;
//			}
			String oilType = gasstation.getOilType();
			String egType = gasstation.getEgType();
			String mgType = gasstation.getMgType();
			String fuelType = gasstation.getFuelType();
			
			if (StringUtils.isNotEmpty(oilType)) {
				if (StringUtils.isEmpty(fuelType)) {
					fuelType = "1";
				} else {
					if (fuelType.indexOf("1")<0) {
						fuelType += "|1";
					}
				}
			} else {
				if (StringUtils.isNotEmpty(fuelType)) {
					if (fuelType.indexOf("|1")>0) {
						fuelType = fuelType.replace("|1", "");
					} else if (fuelType.indexOf("1")>0) {
						fuelType = fuelType.replace("1", "");
					}
				}
			}
			if (StringUtils.isNotEmpty(egType)) {
				if (StringUtils.isEmpty(fuelType)) {
					fuelType = "6";
				} else {
					if (fuelType.indexOf("6")<0) {
						fuelType += "|6";
					}
				}
			} else {
				if (StringUtils.isNotEmpty(fuelType)) {
					if (fuelType.indexOf("|6")>0) {
						fuelType = fuelType.replace("|6", "");
					} else if (fuelType.indexOf("6")>0) {
						fuelType = fuelType.replace("6", "");
					}
				}
			}
			if (StringUtils.isNotEmpty(mgType)) {
				if (StringUtils.isEmpty(fuelType)) {
					fuelType = "2";
				} else {
					if (fuelType.indexOf("2")<0) {
						fuelType += "|2";
					}
				}
			} else {
				if (StringUtils.isNotEmpty(fuelType)) {
					if (fuelType.indexOf("|2")>0) {
						fuelType = fuelType.replace("|2", "");
					} else if (fuelType.indexOf("2")>0) {
						fuelType = fuelType.replace("2", "");
					}
				}
			}
			
			gasstation.setFuelType(fuelType);
		}
	}

}
