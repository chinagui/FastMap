package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.StringUtils;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiBusinesstime;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiDetail;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiPhoto;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

import net.sf.json.JSONObject;

/**
 *若IX_POI_DETAIL表中所有字段均为默认值（参考GDB规格书），并且POI_PID关联IX_POI_PHOTO.POI_PID，没有TAG=7的记录，
 *并且POI_PID关联IX_POI_BUSINESSTIME.POI_PID,没有关联记录，并且当IX_POI.KIND_CODE为120101时，
 *POI_PID关联IX_POI_HOTEL.POI_PID，没有关联记录时，IX_POI_DETAIL表中的记录删除。
 *IX_POI_BUSINESSTIME表中若所有字段均为默认值（参考GDB规格书），则将原有记录删除。
 */

public class FMBATTEMP13 extends BasicBatchRule {
	
	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {return;}
		
		//判断photo表
		boolean noPhoto = true;
		List<IxPoiPhoto> poiPhotos = poiObj.getIxPoiPhotos();
		for (IxPoiPhoto poiPhoto : poiPhotos) {
			int tag = poiPhoto.getTag();
			if(tag==7){noPhoto=false;}
		}
		//判断Business表
		boolean noBusiness = true;
		List<IxPoiBusinesstime> poiBusinesstimes = poiObj.getIxPoiBusinesstimes();
		if(CollectionUtils.isNotEmpty(poiBusinesstimes)){noBusiness=false;}
		//判断Hotel表
		boolean noHotels = true;
		if (poi.getKindCode().equals("120101")) {
			List<IxPoiHotel> poiHotels = poiObj.getIxPoiHotels();
			if(CollectionUtils.isNotEmpty(poiHotels)){noHotels=false;}
		}
		
		List<IxPoiDetail> poiDetails = poiObj.getIxPoiDetails();
		if (CollectionUtils.isNotEmpty(poiDetails)) {
			List<IxPoiDetail> poiDetailsCopy=new ArrayList<IxPoiDetail>();
            poiDetailsCopy.addAll(poiDetails);
            
            for (IxPoiDetail poiDetail : poiDetailsCopy) {
    			String webSite = poiDetail.getWebSite();
    			if(!StringUtils.isEmpty(webSite)){continue;}
    			
    			String fax = poiDetail.getFax();
    			if(!StringUtils.isEmpty(fax)){continue;}
    			
    			String starHotel = poiDetail.getStarHotel();
    			if(!StringUtils.isEmpty(starHotel)){continue;}
    			
    			String briefDesc = poiDetail.getBriefDesc();
    			if(!StringUtils.isEmpty(briefDesc)){continue;}
    			
    			int adverFlag = poiDetail.getAdverFlag();
    			if(adverFlag!=0){continue;}
    			
    			String photoName = poiDetail.getPhotoName();
    			if(!StringUtils.isEmpty(photoName)){continue;}
    			
    			String reserved = poiDetail.getReserved();
    			if(!StringUtils.isEmpty(reserved)){continue;}
    			
    			String memo = poiDetail.getMemo();
    			if(!StringUtils.isEmpty(memo)){continue;}
    			
    			int hwEntryexit = poiDetail.getHwEntryexit();
    			if(hwEntryexit!=0){continue;}
    			
    			int paycard = poiDetail.getPaycard();
    			if(paycard!=0){continue;}
    			
    			String cardtype = poiDetail.getCardtype();
    			if(!StringUtils.isEmpty(cardtype)){continue;}
    			
    			int hospitalClass = poiDetail.getHospitalClass();
    			if(hospitalClass!=0){continue;}
    			
    			int michelinStar = poiDetail.getMichelinStar();
    			if(michelinStar!=9){continue;}
    			
    			int establishment = poiDetail.getEstablishment();
    			if(establishment!=99){continue;}
    			
    			long services = poiDetail.getServices();
    			if(services!=1){continue;}
    			
    			if(noPhoto&&noBusiness&&noHotels){
    				poiObj.deleteSubrow(poiDetail);
    			}
    		}
        }
		
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
