package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 *  FM-D01-38	别名分店名中“ＮＯ．”错误
	检查条件：
	   非删除POI对象
	检查原则：
	官方中文别名中文名称包含“Ｎｏ．”、“Ｎ０．”（这个是全角的零）、“ｎｏ．”、“ｎＯ．”，报log：POI分店名称错误，正确写法是“ＮＯ．”
	检查名称：标准化中文名称（type=1，class={3}，langCode=CHI或CHT）
 * @author sunjiawei
 */
public class FMD0138 extends BasicCheckRule {
	
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
			List<IxPoiName> names = poiObj.getIxPoiNames();
			for(IxPoiName nameTmp:names){
				if(nameTmp.isCH()&&nameTmp.getNameType()==1
						&&nameTmp.getNameClass()==3){
					String nameStr=nameTmp.getName();
					if(nameStr==null||nameStr.isEmpty()){continue;}
					if(nameStr.contains("Ｎｏ．")||nameStr.contains("Ｎ０．")||nameStr.contains("ｎｏ．")||nameStr.contains("ｎＯ．")){
						setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId());
					}
				}
			}
			
			
		}
	}
}
