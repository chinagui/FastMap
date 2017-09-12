package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
/**
 * 	GLM60048		医院POI名称检查		DHM
	检查条件：
	非删除POI对象
	检查原则：
	①与分类为(170100、170101、170102)做父子关系的子POI的官方标准化中文名必须以“父POI的官方标准中文名称-”开头，“-”为全角，否则报log：医院POI标准化官方名称检查！
	②与分类为(170100、170101、170102)做父子关系的子POI的官方标准中文名中只能含一个“医院”，否则报log：医院POI标准化官方名称检查！
	检查名称：官方标准中文名称（name_type=1，name_class=1，lang_Code=CHI或CHT）
 * @author sunjiawei
 */
public class GLM60048 extends BasicCheckRule {
	private Map<Long, Long> parentMap=new HashMap<Long, Long>();
	private static int counter = 0;  //判断医院出现几次

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			String officeChildName = getOfficeStandardCHNameStr(poiObj);
			IxPoiObj parentObj=getParentObj(poi.getPid());
			if(parentObj!=null){
				IxPoi parentPoi=(IxPoi)parentObj.getMainrow();
				String parentKind=parentPoi.getKindCode();
				if(parentKind.equals("170100")||parentKind.equals("170101")||parentKind.equals("170102")){
					String parentOfficeNameStr=getOfficeStandardCHNameStr(parentObj);
					if(StringUtils.isNotBlank(parentOfficeNameStr)&&StringUtils.isNotBlank(officeChildName)){
						if(!officeChildName.startsWith(parentOfficeNameStr+"－")){
							setCheckResult(poi.getGeometry(),poiObj,poi.getMeshId(),null);
							return;
						}
						if(officeChildName.contains("医院")&&officeChildName.contains("醫院")){
							counter =  countStr(officeChildName, "医院")+countStr(officeChildName, "醫院");
						}else if(officeChildName.contains("医院")){
							counter =  countStr(officeChildName, "医院");
						}else if(officeChildName.contains("醫院")){
							counter =  countStr(officeChildName, "醫院");
						}
						if(counter>1){
							setCheckResult(poi.getGeometry(),poiObj,poi.getMeshId(),null);
							return;
						}
					}
				}
			}
			
		}
	}
	
	/** 
     * 判断str1中包含str2的个数 
      * @param str1 
     * @param str2 
     * @return counter 
     */  
    public static int countStr(String str1, String str2) {  
        if (str1.indexOf(str2) == -1) {  
            return 0;  
        } else if (str1.indexOf(str2) != -1) {  
            counter++;
            if(counter<=1){
            	 countStr(str1.substring(str1.indexOf(str2) +  
                         str2.length()), str2);  
            }
            return counter;  
        }  
        return 0;  
    }  
	
	/**
	 * 获取官方标准中文名的 名称 name
	 * @param poiObj
	 * @return
	 */
	private String getOfficeStandardCHNameStr(IxPoiObj poiObj){
		IxPoiName office = poiObj.getOfficeStandardCHName();
		if(office==null){return null;}
		String officeNameStr=office.getName();
		if(officeNameStr!=null&&!officeNameStr.isEmpty()){return officeNameStr;}
		return null;
	}
	
	/**
	 * 获取父poi对象
	 * @param pid
	 * @return
	 */
	private IxPoiObj getParentObj(Long pid){
		if(!parentMap.containsKey(pid)){return null;}
		Long parentPid=parentMap.get(pid);
		Map<Long, BasicObj> poiMap = myReferDataMap.get(ObjectName.IX_POI);
		if(!poiMap.containsKey(parentPid)){return null;}
		IxPoiObj parentObj = (IxPoiObj) poiMap.get(parentPid);
		return parentObj;
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
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
