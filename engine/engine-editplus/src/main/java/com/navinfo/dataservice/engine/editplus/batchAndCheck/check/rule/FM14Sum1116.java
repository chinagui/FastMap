package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @ClassName FM14Sum1116
 * @author Han Shaoming
 * @date 2017年3月2日 上午9:43:53
 * @Description TODO
 * 检查条件：非删除POI对象；
 * 检查原则：
 * 父POI和子POI分类在（200103\200104\120101\120102）中，100米以内，与父官方原始中文名称相同的设施建立了父子关系，
 * 则报log：同名大厦和宾馆建立了父子关系！
 * 备注：log报在子POI上，方便修改
 */
public class FM14Sum1116 extends BasicCheckRule {

	private Map<Long, Long> parentMap=new HashMap<Long, Long>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			
			if(kindCode == null || (!"200103".equals(kindCode)&&!"200104".equals(kindCode)
					&&!"120101".equals(kindCode)&&!"120102".equals(kindCode))){return;}
			IxPoiName ixPoiName = poiObj.getOfficeOriginCHName();
			if(ixPoiName == null){return;}
			String name = ixPoiName.getName();
			if(name == null){return;}
			//是否有父
			if(!parentMap.containsKey(poi.getPid())){return;}
			Long parentId=parentMap.get(poi.getPid());
			BasicObj parentObj = myReferDataMap.get(ObjectName.IX_POI).get(parentId);
			IxPoiObj parentPoiObj = (IxPoiObj) parentObj;
			IxPoi parentPoi = (IxPoi) parentPoiObj.getMainrow();
			String kindCodeP = parentPoi.getKindCode();

			if(kindCodeP == null|| ((!"200103".equals(kindCodeP))&&(!"200104".equals(kindCodeP))
					&&(!"120101".equals(kindCodeP))&&(!"120102".equals(kindCodeP)))){
				return;
			}
			
			IxPoiName ixPoiNameP = parentPoiObj.getOfficeOriginCHName();
			if(ixPoiNameP == null){return;}
			String nameP = ixPoiNameP.getName();
			if(nameP == null){return;}
			Geometry geometry = poi.getGeometry();
			Coordinate coordinate = geometry.getCoordinate();
			Geometry geometryP = parentPoi.getGeometry();
			Coordinate coordinateP = geometryP.getCoordinate();
			double distance = GeometryUtils.getDistance(coordinate, coordinateP);
			if(distance <100&&StringUtils.equals(name, nameP)){
				setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
				return;
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
