package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-GLM60406	名称组错误	DHM	
 * 检查对象：
 * 该POI发生变更(新增或修改主子表、删除子表)；
 * 检查原则：
 * 1、同一名称组中，至多有两条英文名称；
 * 2、官方标准化中文名必须与官方原始英文名在同一组，如果有官方标准化英文名，则这三个名称需要在同一组；
 * 3、同一名称组中，名称分类（NAME_CLASS）必须相同；
 * log：名称组错误
 * @author zhangxiaoyi
 */
public class FMGLM60406 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			Map<Long, Map<String,Integer>> nameGroupMap=new HashMap<Long, Map<String,Integer>>();
			Long OfficeGroupId=(long) 0;
			for(IxPoiName nameTmp:names){
				//官方标准化中文名必须与官方原始英文名在同一组，如果有官方标准化英文名，则这三个名称需要在同一组；
				if(nameTmp.isOfficeName()&&((nameTmp.isStandardName()&&nameTmp.isCH())||
						((nameTmp.isStandardName()||nameTmp.isOriginName())&&nameTmp.isEng()))){
					if(OfficeGroupId==0){OfficeGroupId=nameTmp.getNameGroupid();}
					else if(OfficeGroupId!=nameTmp.getNameGroupid()){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
						return;
					}
				}
				// 1、同一名称组中，至多有两条英文名称；
				//3、同一名称组中，名称分类（NAME_CLASS）必须相同；
				Long groupTmp=nameTmp.getNameGroupid();
				if(!nameGroupMap.containsKey(groupTmp)){
					Map<String, Integer> groupValueMap=new HashMap<String, Integer>();
					groupValueMap.put("nameClass", nameTmp.getNameClass());
					if(nameTmp.isEng()){groupValueMap.put("eng", 1);}
					else{groupValueMap.put("eng", 0);}
					nameGroupMap.put(groupTmp, groupValueMap);
					continue;
				}
				Map<String, Integer> groupValueMap=nameGroupMap.get(groupTmp);
				if(groupValueMap.get("nameClass")!=nameTmp.getNameClass()){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
					return;
				}
				if(nameTmp.isEng()){
					int engNum=groupValueMap.get("eng");
					if(engNum<2){groupValueMap.put("eng", engNum+1);}
					else {
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
						return;
					}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
