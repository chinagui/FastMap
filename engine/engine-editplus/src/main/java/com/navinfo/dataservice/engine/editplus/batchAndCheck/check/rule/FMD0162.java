package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * 	FM-D01-62		官方标准英文名长度检查		D
	 检查条件：
	    非删除POI对象；
	检查原则：
	1. 针对重要分类，如果官方原始英文名称长度大于35且小于150，且无官方标准英文名称，则报log：重点分类官方原始英文名长度大于35无标准化英文名；
	2.针对重要分类，如果官方原始英文名称长度小于等于35，且存在官方标准英文名称，则报log：重点分类官方原始英文名长度小于等于35，不应存在标准化英文名；
	3.针对重要分类，如果官方标准英文名称长度大于35，则报log：重点分类标准化英文名不应大于35；
 * @author sunjiawei
 */
public class FMD0162 extends BasicCheckRule {
	private MetadataApi api=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			//key:groupId,value:IxPoiName
			Map<Long, List<IxPoiName>> groupNameMap=new HashMap<Long, List<IxPoiName>>();
			//key:groupId,value:(key:standardEng/originEng value:nameStr)
			Map<Long, Map<String, String>> groupEngNameMap=new HashMap<Long, Map<String,String>>();
			for(IxPoiName nameTmp:names){
				if(!nameTmp.isEng()){continue;}
				String name=nameTmp.getName();
				if(name==null||name.isEmpty()){name="";}
				Long groupId=nameTmp.getNameGroupid();
				if(!groupNameMap.containsKey(groupId)){
					groupNameMap.put(groupId, new ArrayList<IxPoiName>());
				}
				groupNameMap.get(groupId).add(nameTmp);
				if(nameTmp.isOriginName()){
					if(!groupEngNameMap.containsKey(groupId)){
						groupEngNameMap.put(groupId, new HashMap<String, String>());
					}
					groupEngNameMap.get(groupId).put("originEng", name);
				}else if(nameTmp.isStandardName()){
					if(!groupEngNameMap.containsKey(groupId)){
						groupEngNameMap.put(groupId, new HashMap<String, String>());
					}
					groupEngNameMap.get(groupId).put("standardEng", name);
				}
			}
			if(groupEngNameMap.isEmpty()){return;}
			for(Long groupId:groupEngNameMap.keySet()){
				Map<String, String> engNames = groupEngNameMap.get(groupId);
				String originEng=engNames.get("originEng");
				boolean isImportant=api.judgeScPointKind(poi.getKindCode(), poi.getChain());
				if(isImportant){
					if(!engNames.containsKey("standardEng")){
						for(IxPoiName nameTmp:groupNameMap.get(groupId)){
							if(nameTmp.getName()==null||nameTmp.getName().length()<=35||nameTmp.getName().length()>150){continue;}
							if(nameTmp.isOriginName()&&nameTmp.isOfficeName()&&nameTmp.getName().length()
									>=30&&nameTmp.getName().length()<150){
								setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "重点分类官方原始英文名长度大于35无标准化英文名");
								return;
							}
						}
					}else{
						if(originEng.length()<=35){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "重点分类官方原始英文名长度小于等于35，不应存在标准化英文名");
							return;
						}else if(engNames.get("standardEng")!=null&&engNames.get("standardEng").length()>35){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "重点分类标准化英文名不应大于35");
							return;
						}
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
