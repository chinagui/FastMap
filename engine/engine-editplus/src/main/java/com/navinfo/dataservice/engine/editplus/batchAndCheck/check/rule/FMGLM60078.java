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
 * @ClassName FMGLM60078
 * @author Han Shaoming
 * @date 2017年2月18日 下午8:00:19
 * @Description TODO
 * 检查对象：非删除POI对象
 * 检查原则：
 * kindCode为公园（180304）且官方原始中文名称中以“门”或“門”或“口”结尾的POI记录
 * 1.这些记录POI必须子，否则报log1：公园门未制作父子关系。
 * 2.这些记录的父POI必须为公园（180304），否则报log2：公园门只能作为公园的子。
 * 3.这些子POI的官方原始中文名称一定是以父POI的官方原始中文名称开头，否则log3：公园门名称未以父POI名称开头。
 */
public class FMGLM60078 extends BasicCheckRule {

	private Map<Long, Long> parentMap=new HashMap<Long, Long>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode == null ||!"180304".equals(kindCode)){return;}
			IxPoiName ixPoiName = poiObj.getOfficeOriginCHName();
			if(ixPoiName == null){return;}
			String name = ixPoiName.getName();
			if(name == null){return;}
			if(name.endsWith("门")||name.endsWith("門")||name.endsWith("口")){
				//是否有父
				if(!parentMap.containsKey(poi.getPid())){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "公园门未制作父子关系");
					return;
				}
				Long parentId=parentMap.get(poi.getPid());
				BasicObj parentObj = myReferDataMap.get(ObjectName.IX_POI).get(parentId);
				IxPoiObj parentPoiObj = (IxPoiObj) parentObj;
				IxPoi parentPoi = (IxPoi) parentPoiObj.getMainrow();
				String kindCodeP = parentPoi.getKindCode();
				if(kindCodeP == null ){return;}
				if(!"180304".equals(kindCodeP)){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "公园门只能作为公园的子");
					return;
				}
				IxPoiName ixPoiNameP = parentPoiObj.getOfficeOriginCHName();
				if(ixPoiNameP == null){return;}
				String nameP = ixPoiNameP.getName();
				if(nameP == null){return;}
				if(!name.startsWith(nameP)){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "公园门名称未以父POI名称开头");
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
