package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 *  FM-D01-99	F-2官方标准英文名公司缩写检查	DHM
	检查条件：
	非删除POI对象；
	检查原则：
	官方标准英文名称中存在“co,.”或“Co,.”时，报log：英文名“公司”缩写错误！
	检查名称：官方标准英文名称（name_type=1，name_class=1，lang_Code=ENG）
 * @author sunjiawei
 */
public class FMD0199 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			for(IxPoiName nameTmp:names){
				if(nameTmp.isEng()&&nameTmp.isOfficeName()){
					String name=nameTmp.getName();
					if(name==null||name.isEmpty()){continue;}
					if(nameTmp.isStandardName()&&(name.contains("co,.")||name.contains("Co,."))){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "官方标准化英文名“公司”缩写错误");
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
