package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * 	FM-D01-40		POI简称中包含敏感词检查		DHM	
	检查条件：
	  非删除POI对象
	检查原则：
	POI中文简称（name_type=1,name_class=5,lang_code=CHI或CHT）
	不能包含“民国”、“民國”、“北平”、“民国政府”、“民國政府”、“国立”、“國立”敏感词字样，否则报log：官方简称包含敏感字：**
 * @author sunjiawei
 *
 */
public class FMD0140 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			for(IxPoiName nameTmp:names){
				if(nameTmp.isCH()&&nameTmp.getNameType()==1
						&&nameTmp.getNameClass()==5){
					String nameStr=nameTmp.getName();
					if(nameStr==null||nameStr.isEmpty()){continue;}
					String[] words = {"民国","民國","北平","民国政府","民國政府","国立","國立"};
					List<String> errorList = new ArrayList<String>();
					
					for (String word:words) {
						if(nameStr.contains(word)){
							errorList.add(word);
						}
					}
					
					if(errorList!=null&&errorList.size()>0){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "官方中文简称包含敏感字:“"
								+errorList.toString().replace("[", "").replace("]", "")+"”");
						return;
					}
				}
			}			
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
