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
 * @ClassName FMGLM6006501
 * @author Han Shaoming
 * @date 2017年2月17日 下午3:13:43
 * @Description TODO
 * 检查条件：非删除POI对象
 * 检查原则：
 * kindCode为机场（230126）并且官方原始中文名称以“航站楼”/“航站樓”、“候机楼”/“候機樓”结尾的记录，且为子，
 * 且其父POI的kindCode一定为机场（230126）并且官方原始中文名称以“机场”/“機場”结尾，
 * 否则报log：机场（230126）且名称以“航站楼”、“航站樓”、“候机楼”、“候機樓”结尾的记录的父POI的Kind应为机场（230126）且名称应以“机场”/“機場”结尾；
 */
public class FMGLM6006501 extends BasicCheckRule {

	private Map<Long, Long> parentMap=new HashMap<Long, Long>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode == null || !"230126".equals(kindCode)){return;}
			IxPoiName ixPoiName = poiObj.getOfficeOriginCHName();
			if(ixPoiName == null){return;}
			String name = ixPoiName.getName();
			if(name == null){return;}
			if(name.endsWith("航站楼")||name.endsWith("航站樓")||name.endsWith("候机楼")||name.endsWith("候機樓")){
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
				if(!nameP.endsWith("机场")&&!nameP.endsWith("機場")){
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
