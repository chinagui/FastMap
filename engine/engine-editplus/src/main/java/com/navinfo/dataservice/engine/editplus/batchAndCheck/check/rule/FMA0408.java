package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.api.metadata.model.ScPointNameckObj;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScPointNameckUtil;
/**
 *检查条件：
 *以下条件其中之一满足时，需要进行检查：
 *(1)存在官方原始中文名称IX_POI_NAME新增；
 *(2)存在官方原始中文名称IX_POI_NAME修改或修改分类存在；
 *检查原则：
 *官方标准化简体（NAME_CLASS=1）中文名称含有“SC_POINT_NAMECK”中TYPE=“3”且HM_FLAG<>’HM’的“PRE_KEY”的POI全部报出。
 *提示：POI特殊名称统一：“xxxx”应为“xxxx”（提示：PRE_KEY    RESULT_KEY）
 * @author gaopengrong
 */
public class FMA0408 extends BasicCheckRule {
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			if (!isCheck(poiObj)){return;}
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			
			Map<String, String> typeD3 = metadataApi.scPointNameckTypeD3();

			IxPoiName Name=poiObj.getOfficeStandardCHIName();
			String nameStr= Name.getName();
			
			for(String key:typeD3.keySet()){
				if(nameStr.contains(key)){
					String log="POI特殊名称统一：“"+key+"”,应为“"+typeD3.get(key)+"”";
					setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
				}
			}
		}
	}
	private boolean isCheck(IxPoiObj poiObj){
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		if(poi.hisOldValueContains(IxPoi.KIND_CODE)){
			String oldKindCode=(String) poi.getHisOldValue(IxPoi.KIND_CODE);
			String newKindCode=poi.getKindCode();
			if(!newKindCode.equals(oldKindCode)){
				return true;
			}
		}
		//存在官方原始中文名称IX_POI_NAME新增或修改
		IxPoiName br=poiObj.getOfficeOriginCHIName();
		if(br!=null){
			if(br.getHisOpType().equals(OperationType.INSERT)){return true;}
			if(br.getHisOpType().equals(OperationType.UPDATE) && br.hisOldValueContains(IxPoiName.NAME)){
				String oldName=(String) br.getHisOldValue(IxPoiName.NAME);
				String newName=br.getName();
				if(!newName.equals(oldName)){
					return true;
				}
			}
		}
		return false;
	}
	
}
