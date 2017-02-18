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
 * @ClassName FMGLM6006301
 * @author Han Shaoming
 * @date 2017年2月17日 下午2:21:43
 * @Description TODO
 * 检查条件：非删除POI对象
 * 检查原则：
 * POI名称（name）以“机场”或“機場”结尾，且kindCode为230126时，不能为子，否则报log：此设施是父的分类，不能为子！
 */
public class FMGLM6006301 extends BasicCheckRule {

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
			if(name.endsWith("机场")||name.endsWith("機場")){
				//是否有父
				if(parentMap.containsKey(poi.getPid())){
					//为子
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
	}

}
