package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
/**
 * 	FM-D01-30	标准化中文名称与拼音匹配性检查	DHM
	检查条件：
	    非删除POI对象；
	检查原则：
	名称发音（NAME_PHONETIC）不属于自动转拼音中的任何一个则报出。
	提示：标准化中文名称与拼音匹配性检查：POI名称与POI拼音不匹配
	检查名称：标准化中文名称（name_type=1，name_class={1，3，5}，langCode=CHI或CHT）
 * @author sunjiawei
 *
 */
public class FMD0130 extends BasicCheckRule {
	private Map<Long,Long> pidAdminId;
	MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String adminId=null;
			if(pidAdminId!=null&&pidAdminId.containsKey(poi.getPid())){
				adminId=pidAdminId.get(poi.getPid()).toString();
			}
			List<IxPoiName> names = poiObj.getIxPoiNames();
			for(IxPoiName nameTmp:names){
				if(nameTmp.isCH()&&nameTmp.getNameType()==1
						&&(nameTmp.getNameClass()==1 ||nameTmp.getNameClass()==3||nameTmp.getNameClass()==5)){
					String name=nameTmp.getName();
					if(name==null){continue;}
					String py=metadataApi.pyConvert(name,adminId,null);
					boolean isRightPy=false;
					if(py.equals(nameTmp.getNamePhonetic())){
						isRightPy=true;
						break;
					}
					if(!isRightPy){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
						return;
					}
				}
			}
		}
	}
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		pidAdminId = IxPoiSelector.getAdminIdByPids(getCheckRuleCommand().getConn(), pidList);
	}

}
