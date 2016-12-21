package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
/**
 * FM-GLM60304	机场子POI简称内容检查	DHM	
 * 检查条件：
 * 该POI发生变更(新增或修改主子表、删除子表)
 * 检查原则：
 * 父POI分类是230126（机场），子POI分类为230126、230127、230129、230128、230210
 * 子POI的标准化简称中文（LANG_CODE=CHI或CHT）名称包含其最高级父POI的标准化官方中文名称，
 * 则报出：机场子POI简称包含最高级父POI标准化中文名称，请修改
 * @author gaopengrong
 *
 */
public class FMGLM60304 extends BasicCheckRule {
	private Map<Long, Long> parentMap=new HashMap<Long, Long>();

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode= poi.getKindCode();
			if(kindCode.equals("230126")||kindCode.equals("230127")||kindCode.equals("230129")
					||kindCode.equals("230128")||kindCode.equals("230210")){
				Long pid=poi.getPid();
				//获取一级父pid
				Long parentPid=getFirstParentPid(pid);
				
				Map<Long, BasicObj> poiMap = myReferDataMap.get(ObjectName.IX_POI);
				if(!poiMap.containsKey(parentPid)){return;}
				IxPoiObj parentObj = (IxPoiObj) poiMap.get(parentPid);
				IxPoi parentPoi=(IxPoi) parentObj.getMainrow();
				if(!parentPoi.getKindCode().equals("230126")){
					IxPoiName poiName= poiObj.getOfficeStandardCHName();
					String poiNameStr=poiName.getName();
					IxPoiName parentPoiName= parentObj.getOfficeStandardCHName();
					String parentPoiNameStr=parentPoiName.getName();
					if(poiNameStr.isEmpty()||parentPoiNameStr.isEmpty()){return;}
					if(poiNameStr.contains(parentPoiNameStr)){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(),null);
					}
				}
			}
		}
	}
	
	private Long getFirstParentPid(Long pid){
		if(!parentMap.containsKey(pid)){return null;}
		Long parentPid=parentMap.get(pid);
		while(parentMap.containsKey(parentPid)){
			parentPid=parentMap.get(parentPid);
		}
		return parentPid;
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		parentMap = IxPoiSelector.getAllParentPidsByChildrenPids(getCheckRuleCommand().getConn(), pidList);
		Set<String> referSubrow=new HashSet<String>();
		referSubrow.add("IX_POI_NAME");
		Map<Long, BasicObj> referObjs = getCheckRuleCommand().loadReferObjs(parentMap.values(), ObjectName.IX_POI, referSubrow, false);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
	}
	

}
