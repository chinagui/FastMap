package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParent;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @ClassName GLM60084
 * @author Han Shaoming
 * @date 2017年2月20日 上午9:59:00
 * @Description TODO
 * 检查条件：非删除POI对象
 * 检查原则：
 * 显示坐标相同的点位，有两个以上点位的KIND_CODE包含星级宾馆(120101）时，星级宾馆之间不能相互建立父子关系，
 * 否则报LOG：星级宾馆之间不允许制作父子关系
 */
public class GLM60084 extends BasicCheckRule {

	private Map<Long, Long> parentMap=new HashMap<Long, Long>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode == null || !"120101".equals(kindCode)){return;}
			//子poi
			Geometry geometry = poi.getGeometry();
			Coordinate coordinate = geometry.getCoordinate();
			//是否有父
			if(parentMap.containsKey(poi.getPid())){
				Long parentId=parentMap.get(poi.getPid());
				BasicObj parentObj = myReferDataMap.get(ObjectName.IX_POI).get(parentId);
				IxPoiObj parentPoiObj = (IxPoiObj) parentObj;
				IxPoi parentPoi = (IxPoi) parentPoiObj.getMainrow();
				String kindCodeP = parentPoi.getKindCode();
				if(kindCodeP == null ){return;}
				//父poi
				Geometry geometryP = parentPoi.getGeometry();
				//显示坐标相同的点位
				Coordinate coordinateP = geometryP.getCoordinate();
				double distance = GeometryUtils.getDistance(coordinate, coordinateP);
				if("120101".equals(kindCodeP) && distance<3){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
					return;
				}
			}
			//poi为父,查子
			List<IxPoiParent> ixPoiParents = poiObj.getIxPoiParents();
			if(ixPoiParents==null||ixPoiParents.isEmpty()){return;}
			List<IxPoiChildren> ixPoiChildrens = poiObj.getIxPoiChildrens();
			if(ixPoiChildrens==null||ixPoiChildrens.isEmpty()){return;}
			for (IxPoiChildren ixPoiChildren : ixPoiChildrens) {
				BasicObj childrenObj = myReferDataMap.get(ObjectName.IX_POI).get(ixPoiChildren.getChildPoiPid());
				IxPoiObj childrenPoiObj = (IxPoiObj) childrenObj;
				IxPoi childrenPoi = (IxPoi) childrenPoiObj.getMainrow();
				String kindCodeC = childrenPoi.getKindCode();
				if(kindCodeC == null ){return;}
				Geometry geometryC = childrenPoi.getGeometry();
				//显示坐标相同的点位
				Coordinate coordinateC = geometryC.getCoordinate();
				double distance = GeometryUtils.getDistance(coordinate, coordinateC);
				if("120101".equals(kindCodeC) && distance<3){
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
		List<Long> childrenList = IxPoiSelector.getChildrenPidsByParentPid(getCheckRuleCommand().getConn(), pidList);
		Set<String> referSubrow=new HashSet<String>();
		//referSubrow.add("IX_POI_NAME");
		Collection<Long> parents = parentMap.values();
		List<Long> values = new ArrayList<>(parents);
		values.addAll(childrenList);
		Map<Long, BasicObj> referObjs = getCheckRuleCommand().loadReferObjs(values, ObjectName.IX_POI, referSubrow, false);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
	}

}
