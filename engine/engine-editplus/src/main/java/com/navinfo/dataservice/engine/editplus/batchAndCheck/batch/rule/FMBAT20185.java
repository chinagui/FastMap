package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 查询条件：
 * POI分类为230218且非删除且为父，根据父关联存在充电桩(IX_POI_CHARGINGPLOT.U_RECORD)信息的子(非删除)的所有记录总和N； 
 * 批处理：
 * 批处理该父POI的充电站IX_POI_CHARGINGSTATION.CHARGING_NUM=N; 并生成履历；
 * 
 * @author wangdongbin
 *
 */
public class FMBAT20185 extends BasicBatchRule {
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		
	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		if (poiObj.getIxPoiParents().isEmpty()|| !poi.getKindCode().equals("230218")
				||poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}
		List<IxPoiChildren> childrens = poiObj.getIxPoiChildrens();
		List<IxPoiChargingstation>  charginstions = poiObj.getIxPoiChargingstations();
		if (charginstions==null || charginstions.isEmpty()) {
			return;
		}
		
		for (IxPoiChargingstation station:charginstions) {
			int size =  childrens.size();
			if(size!=station.getChargingNum()){
				station.setChargingNum(size);
			}
		}

	}

}
