package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxSamepoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxSamepoiPart;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.IxSamePoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
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
 * 2、制作了此类型的同一关系(满足检查条件)，但是分类，报LOG：制作多类别同属性同一关系的POI种别必须不相同！
 */
public class GLM60238 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_SAMEPOI)){
			IxSamePoiObj poiObj=(IxSamePoiObj) obj;
			IxSamepoi poi=(IxSamepoi) poiObj.getMainrow();
			if(poi.getRelationType()!=1){return;}
			List<IxSamepoiPart> parts = poiObj.getIxSamepoiParts();
			Set<String> kindSet=new HashSet<String>();
			Geometry geo=null;
			String address=null;
			String name=null;
			boolean first=true;
			String kind=null;
			Long pid=null;
			for(IxSamepoiPart tmp:parts){
				BasicObj partObj = myReferDataMap.get(ObjectName.IX_POI).get(tmp.getPoiPid());
				IxPoiObj partPoiObj = (IxPoiObj) partObj;
				if(partPoiObj == null){
					return;
				}
				IxPoi partPoi = (IxPoi) partPoiObj.getMainrow();
				String partKind=partPoi.getKindCode();
				if(first){
					pid=partPoi.getPid();
					geo=partPoi.getGeometry();					
					kind=partKind;
					IxPoiAddress ixPoiAddressP = partPoiObj.getCHAddress();
					if(ixPoiAddressP!=null){
						address = ixPoiAddressP.getFullname();
					}
					IxPoiName ixPoiNameP = partPoiObj.getOfficeOriginCHName();
					if(ixPoiNameP != null){
						name = ixPoiNameP.getName();
					}
					first=false;
				}
				//第一次循环，仅进行对比字段赋值，不做相同判断。
				if(pid==partPoi.getPid()){continue;}
				//对比属性是否相同
				//显示坐标点位距离
				Coordinate coordinate = geo.getCoordinate();
				Coordinate coordinateP = partPoi.getGeometry().getCoordinate();
				double distance = GeometryUtils.getDistance(coordinate, coordinateP);
				
				String nameP = null;
				String addressP = null;
				IxPoiName ixPoiNameP = partPoiObj.getOfficeOriginCHName();
				if(ixPoiNameP != null){
					nameP = ixPoiNameP.getName();
				}
				IxPoiAddress ixPoiAddressP = partPoiObj.getCHAddress();
				if(ixPoiAddressP!=null){
					addressP = ixPoiAddressP.getFullname();
				}
				
				if((!(("230215".equals(partKind)&&"230216".equals(kind))||("230216".equals(partKind)&&"230215".equals(kind)))&&!StringUtils.equals(name, nameP))||!StringUtils.equals(address, addressP)||distance > 5){
					String targets = "[IX_POI,"+pid+"];[IX_POI,"+partPoiObj.objPid()+"]";
					setCheckResult(partPoi.getGeometry(), targets,partPoi.getMeshId(), "制作多类别同属性同一关系的POI名称、地址、显示坐标应完全相同(5米范围内)");
				}
				
				if(partKind.equals(kind)){
					String targets = "[IX_POI,"+pid+"];[IX_POI,"+partPoiObj.objPid()+"]";
					setCheckResult(partPoi.getGeometry(), targets,partPoi.getMeshId(),  "制作多类别同属性同一关系的POI种别必须不相同");
				}
				kindSet.add(partPoi.getKindCode());
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			IxSamePoiObj poiObj=(IxSamePoiObj) obj;
			IxSamepoi poi=(IxSamepoi) poiObj.getMainrow();
			if(poi.getRelationType()!=1){continue;}
			List<IxSamepoiPart> parts = poiObj.getIxSamepoiParts();
			for(IxSamepoiPart tmp:parts){
				pidList.add(tmp.getPoiPid());
			}
		}
		Set<String> referSubrow=new HashSet<String>();
		referSubrow.add("IX_POI_NAME");
		referSubrow.add("IX_POI_ADDRESS");
		Map<Long, BasicObj> result = getCheckRuleCommand().loadReferObjs(pidList, ObjectName.IX_POI, referSubrow, false);
		myReferDataMap.put(ObjectName.IX_POI, result);
	}

}
