package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * 	GLM60414		代替字符检查		DHM
	检查条件：
	非删除POI对象
	检查原则：
	官方标准中文名称中存在全角“$”字符的，报LOG：名称中存在打不出来字代替符号，需要确认名称正确性！
	name_type=1，name_class=1，lang_code=CHI或CHT
 * @author sunjiawei
 */
public class GLM60414 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			for(IxPoiName nameTmp:names){
				if(nameTmp.isCH()&&nameTmp.getNameClass()==1&&nameTmp.getNameType()==1){
					String nameStr = nameTmp.getName();
					if(nameStr==null||nameStr.isEmpty()){continue;}
					if(nameStr.contains("＄")){
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
