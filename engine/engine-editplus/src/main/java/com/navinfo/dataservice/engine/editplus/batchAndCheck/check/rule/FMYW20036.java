package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 *检查条件：
 *该POI发生变更(新增或修改主子表、删除子表)
 *检查原则：
 *NAME（LANG_CODE="CHI")，不能有繁体字。查找的繁体字在TY_CHARACTER_FJT_HZ中所在行的CONVERT字段的值：
 *1、如果是1表示不转化，不用报log；
 *2、如果是0表示需要确认后转化，报log：**是繁体字，对应的简体字是**，需确认是否转化；
 *检查名称：标准化中文名称（NAME_TYPE=1，NAME_CLASS={1}，LANG_CODE=CHI）
 * @author gaopengrong
 */
public class FMYW20036 extends BasicCheckRule {
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow(); 

			List<IxPoiName> Names=poiObj.getIxPoiNames();
			for(IxPoiName name:Names){
				if((name.getNameClass()==1||name.getNameClass()==5)&&name.getNameType()==1
						&&(name.getLangCode().equals("CHI")||name.getLangCode().equals("CHT"))){
					String nameStr= name.getName();
					if(nameStr.contains("|")||nameStr.contains("｜")){
						setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId());
					}
				}
			}
		}
	}
	
}