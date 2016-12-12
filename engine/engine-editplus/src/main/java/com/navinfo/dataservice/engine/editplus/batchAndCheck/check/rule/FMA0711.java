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
 * FM-A07-11	火车站停车场简称作业	DHM	
 * 检查条件：
 * 该POI发生变更(新增或修改主子表、删除子表)
 * 检查原则：
 * (1) 分类为230210的POI与230103（火车站）存在父子关系，230210分类的标准化官方中文名称（langCode=CHI或CHT）中包含230103分类的标准化官方中文名称
 * (2) 230210分类的POI的名称分类中不存在NAME_CLASS为“5（简称）”的记录，或NAME_CLASS为“5（简称）”的NAME名称开头不包含最高级父【230103（火车站）】的简称；
 * 同时满足以上条件时，程序报LOG
 * 提示：父设施名称有变更，请添加子设施停车场名称"
 * @author zhangxiaoyi
 *
 */
public class FMA0711 extends BasicCheckRule {
	private Map<Long, Long> parentMap=new HashMap<Long, Long>();

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode=poi.getKindCode();
			if(!kindCode.equals("230210")){return;}
			Long pid=poi.getPid();
			if(!parentMap.containsKey(pid)){return;}
			Long parentPid=parentMap.get(pid);
			Map<Long, BasicObj> poiMap = myReferDataMap.get(ObjectName.IX_POI);
			if(!poiMap.containsKey(parentPid)){return;}
			IxPoiObj parentObj = (IxPoiObj) poiMap.get(parentPid);
			IxPoi parentPoi=(IxPoi) parentObj.getMainrow();
			if(!parentPoi.getKindCode().equals("230103")){return;}
			IxPoiName poiOfficeName=poiObj.getOfficeStandardCHName();
			IxPoiName parentOfficeName=parentObj.getOfficeStandardCHName();
			if(!poiOfficeName.getName().contains(parentOfficeName.getName())){return;}
			boolean hasShortName=false;
			IxPoiName poiShortName=null;
			for(IxPoiName nameTmp:poiObj.getIxPoiNames()){
				if(nameTmp.isShortName()){
					hasShortName=true;
					poiShortName=nameTmp;
					break;
				}
			}
			if(!hasShortName){setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId());}
			boolean hasParentShortName=false;
			IxPoiName parentShortName=null;
			for(IxPoiName nameTmp:parentObj.getIxPoiNames()){
				if(nameTmp.isShortName()){
					hasParentShortName=true;
					parentShortName=nameTmp;
					break;
				}
			}	
			if(!hasParentShortName){return;}
			if(!poiShortName.getName().contains(parentShortName.getName())){
				setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId());}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		parentMap = IxPoiSelector.getParentPidsByChildrenPids(getCheckRuleCommand().getConn(), pidList);
		Set<String> referSubrow=new HashSet<String>();
		referSubrow.add("IX_POI_NAME");
		Map<Long, BasicObj> referObjs = getCheckRuleCommand().loadReferObjs(parentMap.values(), ObjectName.IX_POI, referSubrow, false);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
	}

}
