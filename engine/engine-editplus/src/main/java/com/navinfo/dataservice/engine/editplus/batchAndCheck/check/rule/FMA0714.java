package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParent;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
/**
 * FM-A07-14	机场及机场子简称长度检查	DHM
 * 检查条件：
 * (1) 该POI发生变更(新增或修改主子表、删除子表)
 * (2) 分类为“230126”、“230127”、“230128”、“230210”；
 * (3) 数据必须存在父子关系，且对应的第一级父分类为“230126”；
 * 检查原则：
 * NAME_CLASS为“5（简称）”且NAME大于15个字符长度时，需要报出。
 * 提示：机场简称长度检查：机场简称长度不能大于15个字符
 * @author zhangxiaoyi
 *
 */
public class FMA0714 extends BasicCheckRule {
	private Map<Long, Long> parentMap=new HashMap<Long, Long>();

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode=poi.getKindCode();
			if(!kindCode.equals("230126")&&!kindCode.equals("230127")&&!kindCode.equals("230128")
					&&!kindCode.equals("230210")){
				return;
			}
			Long pid=poi.getPid();
			//获取一级父pid
			Long parentPid=getFirstParentPid(pid);
			if(parentPid==null){return;}
			//数据必须存在父子关系，且对应的第一级父分类为“230126”
			//获取一级父poi
			Map<Long, BasicObj> poiMap = myReferDataMap.get(ObjectName.IX_POI);
			if(!poiMap.containsKey(parentPid)){return;}
			IxPoiObj parentObj = (IxPoiObj) poiMap.get(parentPid);
			IxPoi parentPoi=(IxPoi) parentObj.getMainrow();
			if(!parentPoi.getKindCode().equals("230126")){return;}
			//NAME_CLASS为“5（简称）”且NAME大于15个字符长度时，需要报出。
			for(IxPoiName nameTmp:poiObj.getIxPoiNames()){
				if(nameTmp.isShortName()&&nameTmp.isCH()&&nameTmp.getName()!=null&&nameTmp.getName().length()>15){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
					return;
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
