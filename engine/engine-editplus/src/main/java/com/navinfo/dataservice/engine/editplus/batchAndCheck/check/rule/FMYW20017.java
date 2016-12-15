package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
/**
 * FM-YW-20-017	非重要分类英文名作业	D	
 * 检查条件：
 *     以下条件(1)、(2)、(3)之一，且(4)同时满足时，需要进行检查：
 *     (1)存在IX_POI_NAME的新增；
 *     (2)存在IX_POI_NAME的修改；
 *     (3) 存在KIND_CODE或CHAIN修改且修改前后在word_kind表中对应的词库不一样；
 *     (4) KIND_CODE不在重要分类表中
 *     检查原则：
 *     满足条件的POI全部报出。
 *     提示：非重要分类英文名作业
 * @author zhangxiaoyi
 */
public class FMYW20017 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj=(IxPoiObj) obj;
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		String kindCode=poi.getKindCode();
		MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		boolean isImportant=metadataApi.judgeScPointKind(kindCode, poi.getChain());
		if(!isImportant){
			setCheckResult(poi.getGeometry(),poiObj,null);
		}		
	}

	/**
	 *  以下条件(1)、(2)、(3)之一，且(4)同时满足时，需要进行检查：
	 *     (1)存在IX_POI_NAME的新增；
	 *     (2)存在IX_POI_NAME的修改；
	 *     (3) 存在KIND_CODE或CHAIN修改且修改前后在word_kind表中对应的词库不一样；
	 * @param poiObj
	 * @return
	 * @throws Exception 
	 */
	private boolean isCheck(IxPoiObj poiObj) throws Exception{
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		if(poi.hisOldValueContains(IxPoi.KIND_CODE) ||poi.hisOldValueContains(IxPoi.CHAIN)){
			String newKindCode=poi.getKindCode();
			String oldKindCode=newKindCode;
			if(poi.hisOldValueContains(IxPoi.KIND_CODE)){
				oldKindCode=(String) poi.getHisOldValue(IxPoi.KIND_CODE);
			}
			String newChain=poi.getChain();
			String oldChain=newChain;
			if(poi.hisOldValueContains(IxPoi.CHAIN)){
				oldChain=(String) poi.getHisOldValue(IxPoi.CHAIN);
			}
			//存在KIND_CODE或CHAIN修改且修改前后在word_kind表中对应的词库不一样；
			//TODO
		}
		//(1)存在IX_POI_NAME的新增；(2)存在IX_POI_NAME的修改；
		List<IxPoiName> names = poiObj.getIxPoiNames();
		for (IxPoiName br:names){
			if(br.getHisOpType().equals(OperationType.INSERT)){return true;}
			if(br.getHisOpType().equals(OperationType.UPDATE) && br.hisOldValueContains(IxPoiName.NAME)){
				String oldName=(String) br.getHisOldValue(IxPoiName.NAME);
				String newName=br.getName();
				if(!newName.equals(oldName)){
					return true;
				}}
		}
		return false;
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
