package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParent;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
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
	List<Long> childIds = new ArrayList<Long>();
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		childIds = IxPoiSelector.getChildrenPidsByParentPid(getCheckRuleCommand().getConn(), pidList);
		Set<String> referSubrow=new HashSet<String>();
		referSubrow.add("IX_POI_NAME");
		//要修改父信息，所以此处isLock=true
		Map<Long, BasicObj> referObjs = getCheckRuleCommand().loadReferObjs(childIds, ObjectName.IX_POI, referSubrow, true);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
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
				setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),"网络搜集英文作业！");
				if(childIds.size()==0){return;}
				for(long childId:childIds){
					BasicObj childObj=myReferDataMap.get(ObjectName.IX_POI).get(childId);
					IxPoiObj childPoiObj=(IxPoiObj) childObj;
					IxPoi cPoi=(IxPoi) childPoiObj.getMainrow(); 
					IxPoiName cName=childPoiObj.getOfficeStandardCHIName();
					if(cName!=null){
						String cNameStr= cName.getName();
						IxPoiName pName=poiObj.getOfficeStandardCHIName();
						String pNameStr= pName.getName();
						if(cNameStr.contains(pNameStr)){
							setCheckResult(cPoi.getGeometry(), "[IX_POI,"+cPoi.getPid()+"]", cPoi.getMeshId(),"网络搜集英文作业！");
						}
					}
				}
				return;
			}
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Map<String, List<String>>  type14= metadataApi.scPointSpecKindCodeType14();
			
			
			List<IxPoiHotel> hotels= poiObj.getIxPoiHotels();
			for(IxPoiHotel hotel:hotels){
				int rating= hotel.getRating();
				if (type14.containsKey(newKindCode)){
					if(type14.get(newKindCode).contains(String.valueOf(rating))){
						setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),"网络搜集英文作业！");
						if(childIds.size()==0){return;}
						for(long childId:childIds){
							BasicObj childObj=myReferDataMap.get(ObjectName.IX_POI).get(childId);
							IxPoiObj childPoiObj=(IxPoiObj) childObj;
							IxPoi cPoi=(IxPoi) childPoiObj.getMainrow(); 
							IxPoiName cName=childPoiObj.getOfficeStandardCHIName();
							if(cName!=null){
								String cNameStr= cName.getName();
								IxPoiName pName=poiObj.getOfficeStandardCHIName();
								String pNameStr= pName.getName();
								if(cNameStr.contains(pNameStr)){
									setCheckResult(cPoi.getGeometry(), "[IX_POI,"+cPoi.getPid()+"]", cPoi.getMeshId(),"网络搜集英文作业！");
								}
							}
						}
						return;
					}
				}

			}
			
		}
	}
	private boolean isCheck(IxPoiObj poiObj){
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		if(poi.getHisOpType().equals(OperationType.INSERT)){return true;}
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
