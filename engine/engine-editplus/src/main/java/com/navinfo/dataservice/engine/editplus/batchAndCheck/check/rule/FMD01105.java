package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 *  FM-D01-105	官方标准英文名翻译检查	DHM
	检查条件：
	    非删除POI对象
	检查原则：
	POI官方标准英文名如果包含“s'S”（之间无空格)或“s's”（之前无空格且后面S小写）或“s' s”（后面S小写）,报Log：无空格或后面的S小写！
	检查名称：官方标准英文名称（name_type=1，name_class=1，lang_Code=ENG）
 * @author sunjiawei
 */
public class FMD01105 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			Pattern p1 = Pattern.compile(".*s'S+.*");
			Pattern p2 = Pattern.compile(".*s's+.*");
			Pattern p3 = Pattern.compile(".*s' s+.*");
			for(IxPoiName nameTmp:names){
				if(nameTmp.isEng()&&nameTmp.isOfficeName()){
					String name=nameTmp.getName();
					if(name==null||name.isEmpty()){continue;}
					if(nameTmp.isStandardName()&&(p1.matcher(name).matches()||p2.matcher(name).matches()
							||p3.matcher(name).matches())){
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
