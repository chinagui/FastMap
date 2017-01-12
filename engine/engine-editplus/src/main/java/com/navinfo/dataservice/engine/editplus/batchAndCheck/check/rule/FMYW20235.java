package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;


import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiPhoto;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * 检查条件：停车场poi无照片（IX_POI_PHOTO中无关联记录），但是有营业时间（IX_POI_PARKING.OPEN_TIME）并且有修改营业时间的履历的数据，
  	则报出log：无照片，但是有营业时间和营业时间履历。
 * @author gaopengrong
 */
public class FMYW20235 extends BasicCheckRule {
	
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
			List<IxPoiPhoto> photos = poiObj.getIxPoiPhotos();
			if(photos.size()==0){return;}
			for(IxPoiParking parking : parkings){
				String openTiime = parking.getOpenTiime();
				if(openTiime.isEmpty()){return;}
				if(parking.getHisOpType().equals(OperationType.UPDATE) &&parking.hisOldValueContains(IxPoiParking.OPEN_TIIME)){
					String oldOpenTiime= (String) parking.getHisOldValue(IxPoiParking.OPEN_TIIME);
					if (openTiime.equals(oldOpenTiime)){
						setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId());
					}
				}
				
			}
		}
	}
}
