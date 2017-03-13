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
 * @ClassName FMYW20285
 * @author Han Shaoming
 * @date 2017年2月28日 下午1:58:24
 * @Description TODO
 * 检查条件：  非删除POI对象
 * 检查原则：
 * (1)官方原始中文名称包含“出口”或“入口”，分类不为“机场出发达到”(230127)或“火车站出发到达”(230105)，
 * 则报log：名称包含“出口”(“入口”)，分类不是“机场出发到达”（火车站出发到达），请确认！
 * (2)分类为“机场”(230126)或“机场出发到达”(230127),官方原始中文名称不包含“机场”或“機場”，
 * 则报log：名称不包含“机场”(機場)关键字，请确认！
 * (3)分类为“火车站广场”(230103)，官方原始中文名称中不包含父POI的名称，
 * 则报log：子POI官方原始中文名称中不包含父POI官方原始中文名称！
 */
public class FMYW20285 extends BasicCheckRule {
	
	private Map<Long, Long> parentMap=new HashMap<Long, Long>();

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode == null){return;}
			IxPoiName ixPoiName = poiObj.getOfficeOriginCHName();
			String name = null;
			if(ixPoiName != null){
				name = ixPoiName.getName();
				if(name != null){
					if((name.contains("出口")||name.contains("入口"))&&!"230127".equals(kindCode)&&!"230105".equals(kindCode)){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "名称包含“出口”(“入口”)，分类不是“机场出发到达”（火车站出发到达），请确认");
					}
				}
			}
			if("230126".equals(kindCode)||"230127".equals(kindCode)){
				boolean check = false;
				if(ixPoiName == null||name==null){check = true;}
				if(name != null&&!name.contains("机场")&&!name.contains("機場")){check = true;}
				if(check){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "名称不包含“机场”(機場)关键字，请确认");
				}
			}
			if("230103".equals(kindCode)){
				//是否有父
				if(parentMap.containsKey(poi.getPid())){
					boolean check = false;
					if(ixPoiName == null||name==null){check = true;}
					if(name != null){
						Long parentId=parentMap.get(poi.getPid());
						BasicObj parentObj = myReferDataMap.get(ObjectName.IX_POI).get(parentId);
						IxPoiObj parentPoiObj = (IxPoiObj) parentObj;
						IxPoiName ixPoiNameP = parentPoiObj.getOfficeOriginCHName();
						if(ixPoiNameP != null){
							String nameP = ixPoiNameP.getName();
							if(nameP != null){
								if(!name.contains(nameP)){
									check = true;
								}
							}
						}
					}
					if(check){
						Long parentId=parentMap.get(poi.getPid());
						String targets = "[IX_POI,"+poi.getPid()+"];[IX_POI,"+parentId+"]";
						setCheckResult(poi.getGeometry(), targets,poi.getMeshId(), "子POI官方原始中文名称中不包含父POI官方原始中文名称");
					}
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
