package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import org.springframework.util.StringUtils;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiGasstation;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 查询条件：存在IX_POI_GASSTATION新增或者修改，且IX_POI_GASSTATION.U_RECORD!=2（删除） 
 * 批处理：
 * 如果IX_POI_GASSTATION.OIL_TYPE含有97号汽油，将97修改成95
 * 如果IX_POI_GASSTATION.OIL_TYPE含有93号汽油，将93修改成92
 * 如果IX_POI_GASSTATION.OIL_TYPE含有90号汽油，将90修改成89 生成批处理履历；
 * 
 * @author wangdongbin
 *
 */
public class FMBAT20114 extends BasicBatchRule {

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
		List<IxPoiGasstation> gasstationList = poiObj.getIxPoiGasstations();
		for (IxPoiGasstation ixPoiGasstation:gasstationList) {
			if (ixPoiGasstation.getHisOpType().equals(OperationType.DELETE)) {
				continue;
			}
			
			String oilType = ixPoiGasstation.getOilType();
			if (StringUtils.isEmpty(oilType)){
				continue;
			}
			boolean changeFlag = false;
			if (oilType.indexOf("90")>-1) {
				if (oilType.indexOf("89")>-1) {
					if (oilType.indexOf("|90")>-1) {
						oilType = oilType.replace("|90", "");
					} else if (oilType.indexOf("90|")>-1) {
						oilType = oilType.replace("90|", "");
					} else {
						oilType = oilType.replace("90", "");
					}
				} else {
					oilType = oilType.replace("90", "89");
				}
				changeFlag = true;
			}
			if (oilType.indexOf("93")>-1) {
				if (oilType.indexOf("92")>-1) {
					if (oilType.indexOf("|93")>-1) {
						oilType = oilType.replace("|93", "");
					} else if (oilType.indexOf("93|")>-1){
						oilType = oilType.replace("93|", "");
					} else {
						oilType = oilType.replace("93", "");
					}
				} else {
					oilType = oilType.replace("93", "92");
				}
				changeFlag = true;
			}
			if (oilType.indexOf("97")>-1) {
				if (oilType.indexOf("95")>-1) {
					if (oilType.indexOf("|97")>-1) {
						oilType = oilType.replace("|97", "");
					} else if (oilType.indexOf("97|")>-1){
						oilType = oilType.replace("97|", "");
					} else {
						oilType = oilType.replace("97", "");
					}
				} else {
					oilType = oilType.replace("97", "95");
				}
				changeFlag = true;
			}
			
			if (changeFlag) {
				ixPoiGasstation.setOilType(oilType);
			}
		}
	}
}
