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
 *  FM-D01-86	C-4官方原始英文单词中含有零的检查		DHM
	检查条件：
	非删除POI对象
	检查原则：
	1）官方原始英文名中存在“字母+0（数字零）+字母”或“字母+0（数字零）+空格”或“空格+0（数字零）+字母”时，报log：**英文名单词中含有数字零！
	检查名称：官方原始英文名称（name_type=2，name_class=1，lang_Code=ENG）
 * @author sunjiawei
 */
public class FMD0186 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			Pattern p1 = Pattern.compile(".* +0+[a-zA-z]+.*");
			Pattern p2 = Pattern.compile(".*[a-zA-z]+0+[a-zA-z]+.*");
			Pattern p3 = Pattern.compile(".*[a-zA-z]+0+ +.*");
			for(IxPoiName nameTmp:names){
				if(nameTmp.isEng()&&nameTmp.isOfficeName()){
					String name=nameTmp.getName();
					if(name==null||name.isEmpty()){continue;}
					if(nameTmp.isOriginName()&&(p1.matcher(name).matches()||p2.matcher(name).matches()
							||p3.matcher(name).matches())){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "官方原始英文名单词中含有数字零");
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
