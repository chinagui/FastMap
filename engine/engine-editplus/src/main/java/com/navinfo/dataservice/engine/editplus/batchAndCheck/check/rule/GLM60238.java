package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @ClassName GLM60238
 * @author Han Shaoming
 * @date 2017年2月21日 上午11:00:20
 * @Description TODO
 * 检查条件：制作了多类别同属性的同一关系的POI(非删除POI对象且存在同一关系且IX_SAMEPOI.RELATION_TYPE=1)
 * 检查原则：
 * 1、如果原始官方中文名称、地址（fullname）、显示坐标不完全相同(5米范围外)，报LOG：制作多类别同属性同一关系的POI名称、地址、显示坐标应完全相同！
 * 屏蔽：分类分别为加油站（230215）和加气站（230216），名称不相同，地址、显示坐标相同，不需要报log
 * 2、制作了此类型的同一关系(满足检查条件)，但是分类+CHAIN相同，报LOG：制作多类别同属性同一关系的POI种别必须不相同！
 */
public class GLM60238 extends BasicCheckRule {

	private Map<Long, Long> samePoiMap=new HashMap<Long, Long>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode == null){return;}
			IxPoiName ixPoiName = poiObj.getOfficeOriginCHName();
			if(ixPoiName == null){return;}
			String name = ixPoiName.getName();
			if(name == null){return;}
			IxPoiAddress ixPoiAddress = poiObj.getCHAddress();
			if(ixPoiAddress==null){return;}
			String fullName = ixPoiAddress.getFullname();
			if(fullName==null){return;}
			//是否有同一关系
			if(!samePoiMap.containsKey(poi.getPid())){return;}
			//存在同一关系且IX_SAMEPOI.RELATION_TYPE=1
			List<Long> samePoiGroupIds = CheckUtil.getSamePoiGroupIds(poi.getPid(), 1, this.getCheckRuleCommand().getConn());
			if(samePoiGroupIds == null ||samePoiGroupIds.isEmpty()){return;}
			Long parentId=samePoiMap.get(poi.getPid());
			BasicObj parentObj = myReferDataMap.get(ObjectName.IX_POI).get(parentId);
			IxPoiObj parentPoiObj = (IxPoiObj) parentObj;
			IxPoi parentPoi = (IxPoi) parentPoiObj.getMainrow();
			String kindCodeP = parentPoi.getKindCode();
			if(kindCodeP == null ){return;}
			IxPoiName ixPoiNameP = parentPoiObj.getOfficeOriginCHName();
			if(ixPoiNameP == null){return;}
			String nameP = ixPoiNameP.getName();
			if(nameP == null){return;}
			IxPoiAddress ixPoiAddressP = parentPoiObj.getCHAddress();
			if(ixPoiAddressP==null){return;}
			String fullNameP = ixPoiAddressP.getFullname();
			if(fullNameP==null){return;}
			
			Geometry geometry = poi.getGeometry();
			Geometry geometryP = parentPoi.getGeometry();
			//显示坐标点位距离
			Coordinate coordinate = geometry.getCoordinate();
			Coordinate coordinateP = geometryP.getCoordinate();
			double distance = GeometryUtils.getDistance(coordinate, coordinateP);
			
			//屏蔽：分类分别为加油站（230215）和加气站（230216），名称不相同，地址、显示坐标相同，不需要报log
			if(("230215".equals(kindCode)&&"230216".equals(kindCodeP))
					||("230216".equals(kindCode)&&"230215".equals(kindCodeP))){
				if(!name.equals(nameP)&&fullName.equals(fullNameP)&&distance < 5){
					return;
				}
			}
			if(name.equals(nameP)||fullName.equals(fullNameP)||distance > 5){
				setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "制作多类别同属性同一关系的POI名称、地址、显示坐标应完全相同(5米范围内)");
				return;
			}
			//制作了此类型的同一关系(满足检查条件)，但是分类+CHAIN相同，报LOG
			String chain = poi.getChain();
			String chainP = parentPoi.getChain();
			if((chain == null && chainP == null)||(kindCode.equals(kindCodeP)&&chain.equals(chainP))){
				setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "制作多类别同属性同一关系的POI种别必须不相同");
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
		samePoiMap = IxPoiSelector.getSamePoiPidsByThisPids(getCheckRuleCommand().getConn(), pidList);
		Set<String> referSubrow=new HashSet<String>();
		referSubrow.add("IX_POI_NAME");
		referSubrow.add("IX_POI_ADDRESS");
		Map<Long, BasicObj> referObjs = getCheckRuleCommand().loadReferObjs(samePoiMap.values(), ObjectName.IX_POI, referSubrow, false);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
	}

}
