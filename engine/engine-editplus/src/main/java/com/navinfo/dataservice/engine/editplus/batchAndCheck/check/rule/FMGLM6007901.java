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
 * @ClassName FMGLM6007901
 * @author Han Shaoming
 * @date 2017年2月18日 下午1:17:36
 * @Description TODO
 * 检查条件：非删除POI对象
 * 检查原则：
 * kindCode为120101或为120102或120103或120104，且POI官方原始中文名称中含有“机场”或“機場”的POI记录，
 * 则这些记录的父POI的kindCode只能是机场（230126）且父POI官方原始中文名称一定以“机场”或“機場”“航站楼”或“航站樓”或“候机楼”或“候机樓”结尾，
 * 否则报log：机场宾馆未与机场制作父子关系
 */
public class FMGLM6007901 extends BasicCheckRule {

	private Map<Long, Long> parentMap=new HashMap<Long, Long>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode == null || (!"120101".equals(kindCode)&&!"120102".equals(kindCode)
					&&!"120103".equals(kindCode)&&!"120104".equals(kindCode))){return;}
			IxPoiName ixPoiName = poiObj.getOfficeOriginCHName();
			if(ixPoiName == null){return;}
			String name = ixPoiName.getName();
			if(name == null){return;}
			if(name.endsWith("机场")||name.endsWith("機場")){
				//是否有父
				if(!parentMap.containsKey(poi.getPid())){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
					return;
				}
				Long parentId=parentMap.get(poi.getPid());
				BasicObj parentObj = myReferDataMap.get(ObjectName.IX_POI).get(parentId);
				IxPoiObj parentPoiObj = (IxPoiObj) parentObj;
				IxPoi parentPoi = (IxPoi) parentPoiObj.getMainrow();
				String kindCodeP = parentPoi.getKindCode();
				if(kindCodeP == null ){return;}
				if(!"230126".equals(kindCodeP)){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
					return;
				}
				IxPoiName ixPoiNameP = parentPoiObj.getOfficeOriginCHName();
				if(ixPoiNameP == null){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
					return;
				}
				String nameP = ixPoiNameP.getName();
				if(nameP == null){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
					return;
				}
				if(!nameP.endsWith("机场")&&!nameP.endsWith("機場")&&!nameP.endsWith("航站楼")
						&&!nameP.endsWith("航站樓")&&!nameP.endsWith("候机楼")&&!nameP.endsWith("候機樓")){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
					return;
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
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
