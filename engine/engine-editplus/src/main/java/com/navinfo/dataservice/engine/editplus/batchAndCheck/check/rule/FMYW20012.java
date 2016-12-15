package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiPhoto;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * 检查条件：
 * 以下条件(1)、(2)和(3)同时满足时，需要进行检查：
 * (1) 存在IX_POI的新增或修改(改分类、改名称)；
 * (2) IX_POI_HPOTO中存在TAG=5的记录；
 * (3) KIND_CODE在重要分类表中
 * 检查原则：
 * 满足条件的POI全部报出。
 * 提示：根据照片录入英文名作业
 * @author gaopengrong
 */
public class FMYW20012 extends BasicCheckRule {
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			if(!isCheck(poiObj)){return;}
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId());		
		}
	}

	/**
	 * (1) 存在IX_POI的新增或修改(改分类、改名称(官方原始中文名称))；
	 * (2) IX_POI_HPOTO中存在TAG=5的记录；
	 * (3) KIND_CODE在重要分类表中
	 * @param poiObj
	 * @return
	 * @throws Exception 
	 */
	private boolean isCheck(IxPoiObj poiObj) throws Exception{
		IxPoi poi= (IxPoi) poiObj.getMainrow();
		String kindCode= poi.getKindCode();
		String chain= poi.getChain();
		
		IxPoiName name= poiObj.getOfficeOriginCHIName();
		boolean isNamesChange = false;
		List<IxPoiPhoto> photos= poiObj.getIxPoiPhotos();
		boolean isPhoto5 = false;
		
		MetadataApi apiService=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		boolean isImportantPoi = apiService.judgeScPointKind(kindCode, chain);
		if(name.getHisOpType().equals(OperationType.INSERT)){isNamesChange = true;}
		if(name.getHisOpType().equals(OperationType.UPDATE)&&name.hisOldValueContains(IxPoiName.NAME)){
			String oldName=(String) name.getHisOldValue(IxPoiName.NAME);
			String newName=name.getName();
			if(!oldName.equals(newName)){
				isNamesChange = true;
			}
		}
		for (IxPoiPhoto photo: photos) {
			if(photo.getTag()==5){
				isPhoto5=true;
			}
		}
		if (!isPhoto5){return false;}
		if (!isImportantPoi){return false;}
		if (poi.getOpType().equals(OperationType.INSERT)||poi.hisOldValueContains(IxPoi.KIND_CODE)||isNamesChange){return true;}
		return false;
	}	
}
