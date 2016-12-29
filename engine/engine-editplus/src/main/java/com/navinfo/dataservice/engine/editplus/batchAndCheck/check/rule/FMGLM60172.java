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
 * FM-GLM60172	英文名长度检查	DHM
 * 检查条件：
 * 该POI发生变更(新增或修改主子表、删除子表)；
 * 检查原则：
 * 1.原始英文名长度大于150,报LOG1：原始英文名长度不应大于150
 * 2.同一名称分组内，原始英文名称长度小于等于150，无标准化英文名：
 * a.重要分类内(提供配置表)，官方原始英文名或别名原始英文名长度大于35，报LOG2a：
 * 重点分类原始官方英文名或别名原始英文名长度大于35无标准化英文名
 * b.非官方原始英文名，非别名原始英文名长度大于35，报LOG2b：非官方或别名原始英文名长度大于35无标准化英文名
 * 3.同一名称分组内，原始英文名称长度小于等于150，有标准化英文名
 * a.原始英文名小于等于35，报LOG3a：原始英文名长度小于35，不应存在标准化英文名
 * b.原始英文名和标准化英文名都大于35，报LOG3b：标准化英文名不应大于35
 * 4.同一名称分组内，有标准化英文名，无原始英文名，报LOG4：同一名称分组内，有标准化英文名，无原始英文名
 * 注：以上条件中“重点分类内”，将【备注】sheet页[重要分类表]说明
 * @author zhangxiaoyi
 */
public class FMGLM60172 extends BasicCheckRule {
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
					if(name.length()>150){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "原始英文名长度不应大于150");
						return;
					}
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
//				* 4.同一名称分组内，有标准化英文名，无原始英文名，报LOG4：同一名称分组内，有标准化英文名，无原始英文名
				if(!engNames.containsKey("originEng")){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "同一名称分组内，有标准化英文名，无原始英文名");
					return;
				}
				String originEng=engNames.get("originEng");
//			 * 2.同一名称分组内，原始英文名称长度小于等于150，无标准化英文名：
//			 * a.重要分类内(提供配置表)，官方原始英文名或别名原始英文名长度大于35，报LOG2a：
//			 * 重点分类原始官方英文名或别名原始英文名长度大于35无标准化英文名
//			 * b.非官方原始英文名，非别名原始英文名长度大于35，报LOG2b：非官方或别名原始英文名长度大于35无标准化英文名
				if(!engNames.containsKey("standardEng")){
					boolean isImportant=api.judgeScPointKind(poi.getKindCode(), poi.getChain());
					for(IxPoiName nameTmp:groupNameMap.get(groupId)){
						if(nameTmp.getName()==null||nameTmp.getName().length()<=35){continue;}
						if(isImportant&&nameTmp.isOriginName()&&(nameTmp.isOfficeName()||nameTmp.isAliasName())){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "重点分类原始官方英文名或别名原始英文名长度大于35无标准化英文名");
							return;
						}
						if(!(nameTmp.isOriginName()&&(nameTmp.isOfficeName()||nameTmp.isAliasName()))){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "非官方或别名原始英文名长度大于35无标准化英文名");
							return;
						}
					}
				}else{
//				 * 3.同一名称分组内，原始英文名称长度小于等于150，有标准化英文名
//				 * a.原始英文名小于等于35，报LOG3a：原始英文名长度小于35，不应存在标准化英文名
//				 * b.原始英文名和标准化英文名都大于35，报LOG3b：标准化英文名不应大于35
					if(originEng.length()<=35){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "原始英文名长度小于35，不应存在标准化英文名");
						return;
					}else if(originEng.length()>35&&engNames.get("standardEng")!=null
							&&engNames.get("standardEng").length()>35){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "标准化英文名不应大于35");
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
