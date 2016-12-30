package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 *检查条件： 
 *对象POI新增或者修改(改名称、改分类)
 *检查原则：
 *(1)如果分类和IX_POI_HOTEL.RATING的值与配置表SC_POINT_SPEC_KINDCODE_NEW中type=14中POIKIND和RATING字段内容匹配，则报log：网络搜集英文作业！
 *(2)如果分类为160202且chain不为空的数据，则报log:网络搜集英文作业！
 *(3)如果该POI为父，其子的官方标准化中文名称包含父的官方标准化中文名称
 *，则子POI报log:网络搜集英文作业！
 * @author gaopengrong
 */
public class FMM0102 extends BasicCheckRule {
	
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
			String newKindCode=poi.getKindCode();
			String chain=poi.getChain();
			if(newKindCode.equals("160202")&&chain!=null){
				String log="网络搜集英文作业！";
				setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
				return;
			}
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Map<String, String> type14= metadataApi.scPointSpecKindCodeType14();
			
			
			List<IxPoiHotel> hotels= poiObj.getIxPoiHotels();
			for(IxPoiHotel hotel:hotels){
				int rating= hotel.getRating();
				if (type14.containsKey(newKindCode)){
					if(String.valueOf(rating).equals(type14.get(newKindCode))){
						String log="网络搜集英文作业！";
						setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
						return;
					}
				}

			}
		}
	}
	private boolean isCheck(IxPoiObj poiObj){
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		if(br.getHisOpType().equals(OperationType.INSERT)){return true;}
		if(poi.hisOldValueContains(IxPoi.KIND_CODE)){
			String oldKindCode=(String) poi.getHisOldValue(IxPoi.KIND_CODE);
			String newKindCode=poi.getKindCode();
			if(!newKindCode.equals(oldKindCode)){
				return true;
			}
		}
		//存在IX_POI_NAME新增或者修改履历
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
