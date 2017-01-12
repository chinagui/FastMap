package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * 深度信息停车场前批：批处理原则："IX_POI.OPEN_24H"字段为“1”的，对停车场的“IX_POI_PARKING.OPEN_TIME”批处理为“00:00-24:00“
 * @author Gao Pengrong
 */
public class FMBAT20198 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	//月编无删除数据
	public void runBatch(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			try {
				if (poi.getOpen24h()!=1){
					return;
				}
				List<IxPoiParking> parkings = poiObj.getIxPoiParkings();
				//模型定义一个poi只有一个parking子表
				for(IxPoiParking parking : parkings)
				{
					if (!"00:00-24:00".equals(parking.getOpenTiime())){
						parking.setOpenTiime("00:00-24:00");
					}	
				}
			} catch (Exception e) {
				throw e;
			}

		}

	}

}
