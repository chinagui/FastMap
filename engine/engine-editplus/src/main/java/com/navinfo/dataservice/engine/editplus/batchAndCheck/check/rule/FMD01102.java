package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 *  FM-D01-102		F-3官方原始英文出入口翻译检查
	检查条件：
	非删除POI对象
	检查原则：
	1）官方标准化中文名中包含“口”且不包含“入”和“出”，官方原始英文名中包含“exit”或“entrance”且不包含“exit/entrance”时，报log：出入口翻译错误！
	2）官方标准化中文名中包含“出入口”，官方原始英文名中不包含“exit/entrance”时，报log：出入口翻译错误！
	官方标准中文名称（name_type=1，name_class=1，lang_Code=CHI或CHT）；
	官方原始英文名称（name_type=2，name_class=1，lang_Code=ENG）
 * @author sunjiawei
 */
public class FMD01102 extends BasicCheckRule {
	
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
			IxPoiName name=poiObj.getOfficeStandardCHIName();
			if(name==null){return;}
			String nameStr=name.getName();
			if(nameStr==null||nameStr.isEmpty()){return;}
			IxPoiName engName=poiObj.getOfficeOriginEngName();
			if(engName==null){return;}
			String engNameStr=engName.getName();
			if(engNameStr==null||engNameStr.isEmpty()){return;}
			if(nameStr.contains("口")&&!nameStr.contains("入")&&!nameStr.contains("出")
					&&(engNameStr.contains("exit")||engNameStr.contains("entrance"))
					&&!engNameStr.contains("exit/entrance")){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
				return;
			}
			if(nameStr.contains("出入口")&&!engNameStr.contains("exit/entrance")){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
				return;
			}
		}
	}
	
	
}
