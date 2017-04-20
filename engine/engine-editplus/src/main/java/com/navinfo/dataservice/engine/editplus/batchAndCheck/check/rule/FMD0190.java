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
 *  FM-D01-90	C-5官方原始英文连续数字中含有欧	DHM
	检查条件：
	非删除POI对象
	检查原则：
	官方原始英文名中存在“数字+o（字母欧）+数字”或“数字+o（字母欧）空格”或“空格+o（字母欧）+数字”时，报log：**英文名连续数字中含有欧！
	检查名称：官方原始英文名称（name_type=2，name_class=1，lang_Code=ENG）
 * @author sunjiawei
 */
public class FMD0190 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			Pattern p1 = Pattern.compile(".* +o+[0-9]+.*");
			Pattern p2 = Pattern.compile(".*[0-9]+o+[0-9]+.*");
			Pattern p3 = Pattern.compile(".*[0-9]+o+ +.*");
			for(IxPoiName nameTmp:names){
				if(nameTmp.isEng()&&nameTmp.isOfficeName()){
					String name=nameTmp.getName();
					if(name==null||name.isEmpty()){continue;}
					name=name.toLowerCase();
					if(nameTmp.isOriginName()&&(p1.matcher(name).matches()||p2.matcher(name).matches()
							||p3.matcher(name).matches())){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "官方原始英文名连续数字中含有欧");
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
