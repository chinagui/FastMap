package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-YW-20-018	地址英文作业	DHM	
 * 检查条件：
 *     同时满足条件(1)，(2)和(4)，或(1)、(3)和(4)时，需要进行检查：
 *     (1)IX_POI.U_RECORD!=2(删除)；
 *     (2) 存在IX_POI_ADDRESS新增或者修改且FULLNAME不为空；
 *     (3) 不存在IX_POI_ADDRESS新增或者修改且FULLNAME不为空，也没有英文地址（LANG_CODE=""ENG""）
 *     (4) KIND_CODE在重要分类表中
 *     检查原则：
 *     满足条件的POI全部报出。
 *     提示：地址英文作业
 * @author zhangxiaoyi
 */
public class FMYW20018 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			if(!isCheck(poiObj)){return;}
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode=poi.getKindCode();
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			boolean isImportant=metadataApi.judgeScPointKind(kindCode, poi.getChain());
			if(isImportant){
				setCheckResult(poi.getGeometry(),poiObj,null);
			}}
	}

	/**
	 * 满足一个条件就返回true
	 *   (2) 存在IX_POI_ADDRESS新增或者修改且FULLNAME不为空；
	 *   (3) 不存在IX_POI_ADDRESS新增或者修改且FULLNAME不为空，也没有英文地址（LANG_CODE=""ENG""）
	 * @param poiObj
	 * @return true满足检查条件，false不满足检查条件
	 * @throws Exception 
	 */
	private boolean isCheck(IxPoiObj poiObj) throws Exception{
		List<IxPoiAddress> addrs = poiObj.getIxPoiAddresses();
		boolean hasEng=false;
		boolean hasAddr=false;
		for (IxPoiAddress br:addrs){
			if(br.getHisOpType().equals(OperationType.INSERT)){return true;}
			if(br.getHisOpType().equals(OperationType.UPDATE) && br.hisOldValueContains(IxPoiAddress.FULLNAME)){
				String oldName=(String) br.getHisOldValue(IxPoiAddress.FULLNAME);
				String newName=br.getFullname();
				if(newName!=null && !newName.equals(oldName) && !newName.isEmpty()){
					return true;
				}}
			String newName=br.getFullname();
			if(br.isCH()&& newName!=null && !newName.isEmpty()){
				hasAddr=true;
			}
			if(br.isEng()){hasEng=true;}
		}
		if(hasAddr && !hasEng){return true;}
		return false;
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
