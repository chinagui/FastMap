package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
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
 * FM-GLM60314	英文名称分类唯一检查	DHM	
 * 检查条件：该POI发生变更(新增或修改主子表、删除子表)；
 * 1.一个POI，官方标准化名称，至多有一个标准化官方英文名称，报log：一个POI的一组官方标准化名至多有一个标准化官方英文名称
 * 2.一个POI，官方标准化名称，有且只有一个原始官方英文名称，报log：一个POI的一组官方标准化名有且只有一个原始官方英文名称
 * 3.一个POI，如存在别名中文名称，同一个组内，至多有一个标准化别名英文名称，报log：一个POI的一组别名至多有一个标准化别名英文名称
 * 4.一个POI，如存在别名中文名称，同一个组内，至多有一个原始别名英文名称，报log：一个POI的一组别名至多有一个原始别名英文名称
 * 5.一个POI，如存在子冠父名中文名称，则至多有一个标准化子冠父名英文名称，报log：一个POI的一组子冠父名至多有一个标准化子冠父名英文名称
 * 6.一个POI，如存在子冠父名中文名称，必须有且只有一个原始子冠父名英文名称，报log：一个POI的一组子冠父名必须有且只有一个原始子冠父名英文名称
 * 7.一个POI，如存在站点线路名，则至多有一个标准化站点线路名英文名，报log：一个POI的一组站点线路名至多有一个标准化站点线路名英文名
 * 8.一个POI，如存在站点线路名，有且只有一个原始站点线路名英文名，报log：一个POI的一组站点线路名有且只有一个原始站点线路名英文名
 * 9.名称分类为菜单、简称、曾用名、古称的名称存在英文名，报log：名称分类为XX的名称不应存在英文名
 * @author zhangxiaoyi
 */
public class FMGLM60314 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			List<String> errorList=new ArrayList<String>();
			Map<String,Integer> groupNameModel=new HashMap<String, Integer>();
			groupNameModel.put("StandardEng",0);
			groupNameModel.put("OrginalEng",0);
			groupNameModel.put("CH",0);
			Map<String,Map<String,Integer>> namesMap=new HashMap<String, Map<String,Integer>>();
			for(IxPoiName nameTmp:names){
			//nameClass 官方office 别名 alias 子冠父名 parent 站点线路 station 菜单 Menu 简称 曾用名 古称
            //name_type 标准standard  原始 orginal  lang_code 英文eng 中文 ch
				if(nameTmp.isEng()){
					if(nameTmp.isMenuName()||nameTmp.isUsedName()||nameTmp.isShortName()||nameTmp.isOldName()){
						errorList.add("名称分类为"+getNameByClass(nameTmp.getNameClass())+"的名称不应存在英文名");
						continue;
					}
					if(!(nameTmp.isStandardName()||nameTmp.isOriginName())){continue;}
					String namekeyStr=nameTmp.getNameGroupid()+","+nameTmp.getNameClass();
					if(!namesMap.containsKey(namekeyStr)){
						namesMap.put(namekeyStr, new HashMap<String, Integer>());
						namesMap.get(namekeyStr).put("StandardEng",0);
						namesMap.get(namekeyStr).put("OrginalEng",0);
						namesMap.get(namekeyStr).put("CH",0);
					}
					if(nameTmp.isStandardName()){
						int stand=namesMap.get(namekeyStr).get("StandardEng");
						stand+=1;
						namesMap.get(namekeyStr).put("StandardEng",stand);}
					else if(nameTmp.isOriginName()){
						int org=namesMap.get(namekeyStr).get("OrginalEng");
						org+=1;
						namesMap.get(namekeyStr).put("OrginalEng",org);}
				}else if(nameTmp.isCH()){
					if(nameTmp.getNameClass()==1&&!nameTmp.isStandardName()){continue;}
					String namekeyStr=nameTmp.getNameGroupid()+","+nameTmp.getNameClass();
					if(!namesMap.containsKey(namekeyStr)){
						namesMap.put(namekeyStr, new HashMap<String, Integer>());
						namesMap.get(namekeyStr).put("StandardEng",0);
						namesMap.get(namekeyStr).put("OrginalEng",0);
						namesMap.get(namekeyStr).put("CH",0);
					}
					int ch=namesMap.get(namekeyStr).get("CH");
					ch+=1;
					namesMap.get(namekeyStr).put("CH",ch);
				}
			}
            for (String namekeyStr:namesMap.keySet()){
            	int nameClass=Integer.valueOf(namekeyStr.split(",")[1]);
            	if(nameClass==1 &&namesMap.get(namekeyStr).get("CH")>0){
            		if(namesMap.get(namekeyStr).get("StandardEng")>1){
            			errorList.add("一个POI的一组官方标准化名至多有一个标准化官方英文名称");
            		}
            		if(namesMap.get(namekeyStr).get("OrginalEng")>1){
            			errorList.add("一个POI的一组官方标准化名必须有且只有一个原始官方英文名称");
            		}
            	}else if(nameClass==3){
            		if(namesMap.get(namekeyStr).get("StandardEng")>1){
            			errorList.add("一个POI的一组别名至多有一个标准化别名英文名称");
            		}
            		if(namesMap.get(namekeyStr).get("OrginalEng")>1){
            			errorList.add("一个POI的一组别名至多有一个原始别名英文名称");
            		}
            	}else if(nameClass!=4&&nameClass!=5&&nameClass!=6&&nameClass!=7){
            		if(namesMap.get(namekeyStr).get("CH")>0&&namesMap.get(namekeyStr).get("StandardEng")>1){
            			errorList.add("一个POI的一组"+getNameByClass(nameClass)+"至多有一个标准化"+getNameByClass(nameClass)+"英文名称");
            		}
            		if(namesMap.get(namekeyStr).get("CH")>0&&namesMap.get(namekeyStr).get("OrginalEng")>1){
            			errorList.add("一个POI的一组"+getNameByClass(nameClass)+"必须有且只有一个原始"+getNameByClass(nameClass)+"英文名称");
            		}
            	}
            }
            if(errorList!=null&&errorList.size()>0){
            	setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), errorList.toString().replace("[", "").replace("]", ""));
            }
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	private String getNameByClass(int nameClass){
        if(nameClass==3){return "别名";}
        if(nameClass==4){return "菜单";}
        if(nameClass==5){return "简称";}
        if(nameClass==6){return "曾用名";}
        if(nameClass==7){return "古称";}
        if(nameClass==8){return "站点线路名";}
        if(nameClass==9){return "子冠父名";}
		return null;
    }

}
