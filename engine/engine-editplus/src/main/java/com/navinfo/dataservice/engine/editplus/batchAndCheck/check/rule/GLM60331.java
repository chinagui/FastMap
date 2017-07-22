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
 * 	GLM60331		机场子简称长度检查	DHM	
	检查对象：
	非删除POI对象，且分类为230126、230127（机场到达、出发）、230128（机场到达、出发门）），或分类为
	为230210的停车场且要求其停车场父POI的分类是230126（如果直接父分类不是230126，逐级判断上一层父）)
	检查原则：官方中文简称(name_class=5,name_type=1,lang_code=CHI或CHT)长度超过15个全角字符，报LOG：机场子简称中名称长度超过15个字符,需做简化！
 * @author sunjiawei
 *
 */
public class GLM60331 extends BasicCheckRule {
	private Map<Long, Long> parentMap=new HashMap<Long, Long>();

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode= poi.getKindCode();
			boolean parkFlag = false;
			if(kindCode.equals("230210")){
				Map<Long, BasicObj> poiMap = myReferDataMap.get(ObjectName.IX_POI);
				Long pid=poi.getPid();
				//获取一级父pid
				Long parentPid=getFirstParentPid(pid);
				if(!poiMap.containsKey(parentPid)){return;}
				IxPoiObj parentObj = (IxPoiObj) poiMap.get(parentPid);
				IxPoi parentPoi=(IxPoi) parentObj.getMainrow();
				if(parentPoi.getKindCode().equals("230126")){
					parkFlag = true;
				}
			}
			if(kindCode.equals("230126")||kindCode.equals("230127")||kindCode.equals("230128")
					||parkFlag){
			
				IxPoiName poiName= poiObj.getStandardShortName();
				if(poiName==null){return;}
				String poiNameStr=poiName.getName();
				if(poiNameStr.isEmpty()){return;}
				if(poiNameStr.length()>15){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(),null);
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
