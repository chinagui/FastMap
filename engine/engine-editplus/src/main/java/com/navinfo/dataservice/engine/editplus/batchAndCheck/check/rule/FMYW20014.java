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
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-YW-20-014	重要分类英文名作业	D
 * 检查条件：
 *     以下条件(1)、(2)、(3)、之一，且(5)同时满足时，需要进行检查：
 *     (1)存在IX_POI_NAME的新增；
 *     (2)存在IX_POI_NAME的修改；
 *     (3)存在KIND_CODE或CHAIN修改
 *     (5)KIND_CODE在重要分类表中
 *     检查原则：
 *     满足条件的POI全部报出。
 *     提示：没有照片录入英文名作业
 * @author zhangxiaoyi
 */
public class FMYW20014 extends BasicCheckRule {
	private MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			if(!isCheck(poiObj)){return;}
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode=poi.getKindCode();			
			boolean isImportant=metadataApi.judgeScPointKind(kindCode, poi.getChain());
			if(isImportant){
				setCheckResult(poi.getGeometry(),poiObj,poi.getMeshId(),null);
				return;
			}	}	
	}

	/**
	 *  以下条件(1)、(2)、(3)之一，且(4)同时满足时，需要进行检查：
	 *     (1)存在IX_POI_NAME的新增；
	 *     (2)存在IX_POI_NAME的修改；
	 *     (3) 存在KIND_CODE或CHAIN修改
	 * @param poiObj
	 * @return true满足检查条件，false不满足检查条件
	 * @throws Exception 
	 */
	private boolean isCheck(IxPoiObj poiObj) throws Exception{
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		String newKindCode=poi.getKindCode();
		String newChain=poi.getChain();
		if(poi.hisOldValueContains(IxPoi.KIND_CODE) ||poi.hisOldValueContains(IxPoi.CHAIN)){
			if(poi.hisOldValueContains(IxPoi.KIND_CODE)){
				String oldKindCode="";
				if (poi.getHisOldValue(IxPoi.KIND_CODE)!=null){
					oldKindCode=(String) poi.getHisOldValue(IxPoi.KIND_CODE);
				}
				if(!newKindCode.equals(oldKindCode)){
					return true;
				}
			}
			if(poi.hisOldValueContains(IxPoi.CHAIN)){
				String oldChain="";
				if (poi.getHisOldValue(IxPoi.CHAIN)!=null){
					oldChain=(String) poi.getHisOldValue(IxPoi.CHAIN);
				}
				if(!newChain.equals(oldChain)){
					return true;
				}
			}
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
