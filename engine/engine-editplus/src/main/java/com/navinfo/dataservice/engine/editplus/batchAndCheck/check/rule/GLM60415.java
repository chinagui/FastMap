package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * 	GLM60415	名称拼音错误检查		DHM
	检查条件：
	非删除POI对象
	检查原则：
	官方标准中文名称拼音、官方标准别名或曾用名中文拼音、官方标准简称中文拼音NAME_PHONETIC中存在“Uu”的，报LOG:拼音中存在非法值，需要确认拼音正确性!
	name_type=1,name_class=1,3,5,6,lang_code=CHI或CHT
 * @author sunjiawei
 */
public class GLM60415 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			for(IxPoiName nameTmp:names){
				if(nameTmp.isCH()&&(nameTmp.getNameClass()==1||nameTmp.getNameClass()==3||
						nameTmp.getNameClass()==5||nameTmp.getNameClass()==6)&&nameTmp.getNameType()==1){
					String nameStr = nameTmp.getNamePhonetic();
					if(nameStr==null||nameStr.isEmpty()){continue;}
					if(nameStr.contains("Uu")){
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
