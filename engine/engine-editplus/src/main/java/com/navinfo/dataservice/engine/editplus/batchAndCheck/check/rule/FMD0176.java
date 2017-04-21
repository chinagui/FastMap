package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * FM-D01-76 	A-7官方原始英文名括号前后空格检查		DHM	
 * 非删除POI对象
         检查原则：
         官方原始英文名中括号里面存在空格或官方原始英文名中括号外面不存在空格，则报log：英文名括号前后空格错误
        检查名称：官方原始英文名称（name_type=2，name_class=1，lang_Code=ENG)
 * @author sunjiawei
 */
public class FMD0176 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			for(IxPoiName nameTmp:names){
				if(nameTmp.isEng()&&nameTmp.isOfficeName()
						&&nameTmp.isOriginName()){
					String name=nameTmp.getName();
					if(name==null||name.isEmpty()){continue;}
					if(name.contains("( ")||name.contains(" )")){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
						return;
					}
					Pattern p = Pattern.compile(".*[^ ]+\\(.+");
					Matcher m = p.matcher(name);
					Pattern p1 = Pattern.compile(".+\\)[^ ]+.*");
					Matcher m1 = p1.matcher(name);
					if(m.matches()||m1.matches()){
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
