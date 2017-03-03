package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * @ClassName FM14Sum1109
 * @author Han Shaoming
 * @date 2017年3月1日 下午2:42:45
 * @Description TODO
 * 检查条件：非删除POI的对象；
 * 检查原则：
 * 查询出必须有父设施，却没有父的POI。
 * kindCode为医院内部设施、急诊科/室、火车站出发到达、机场出发/到达、机场出发/到达门、风景名胜售票点、教学楼、
 * 院/系、电动汽车充电桩(170104\170105\230105\230127\230128\210304\160106\160107\230227)，
 * log：分类POI必须有父！
 * 或indoor=1时，POI必须要有父，否则报log：内部POI必须有父！
 */
public class FM14Sum1109 extends BasicCheckRule {

	private Map<Long, Long> parentMap=new HashMap<Long, Long>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode == null){return;}
			int indoor = poi.getIndoor();
			boolean check = false;
			//是否有父
			if(!parentMap.containsKey(poi.getPid())){check = true;}
			if("170104".equals(kindCode)||"170105".equals(kindCode)||"230105".equals(kindCode)
					||"230127".equals(kindCode)||"230128".equals(kindCode)||"210304".equals(kindCode)
					||"160106".equals(kindCode)||"160107".equals(kindCode)||"230227".equals(kindCode)){
				//是否有父
				if(check){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "分类POI必须有父");
				}
			}
			if(indoor == 1){
				//是否有父
				if(check){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "内部POI必须有父");
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
