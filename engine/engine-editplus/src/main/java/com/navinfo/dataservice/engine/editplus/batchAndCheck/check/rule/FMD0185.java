package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 *  FM-D01-85	B-9中文“号”翻译中需官方标准英文含No	DHM	
	检查条件：
	非删除POI对象
	检查原则：
	官方标准化中文名中存在“号”时，官方标准化英文名中不存在“No.”时，报log：中文“号”翻译错误
	官方标准中文名称（name_type=1，name_class=1，lang_Code=CHI或CHT）；
	官方标准英文名称（name_type=1，name_class=1，lang_Code=ENG）
 * @author sunjiawei
 */
public class FMD0185 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			String officeNameCH="";
			String officeNameEng="";
			for(IxPoiName nameTmp:names){
				if(nameTmp.isCH()&&nameTmp.isOfficeName()&&nameTmp.isStandardName()){
					String name=nameTmp.getName();
					if(name==null||name.isEmpty()){continue;}
					officeNameCH=name;
					continue;
				}
				if(nameTmp.isEng()&&nameTmp.isOfficeName()&nameTmp.isStandardName()){
					String name=nameTmp.getName();
					if(name==null||name.isEmpty()){continue;}
					officeNameEng=name;
					continue;
				}
			}
			if(officeNameCH==null||officeNameCH.isEmpty()){return;}
			if(officeNameEng==null||officeNameEng.isEmpty()){return;}
			if(officeNameCH.contains("号")&&!officeNameEng.toLowerCase().contains("no.")){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
