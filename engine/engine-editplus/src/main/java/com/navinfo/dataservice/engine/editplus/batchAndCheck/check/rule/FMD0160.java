package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 *  FM-D01-60	官方原始英文名长度检查		DHM
	检查条件：
	    非删除POI对象；
	检查原则：
	如果官方原始英文名称长度大于150，则报log：官方原始英文名称长度不能大于150！
	检查名称：官方原始英文名称（type={2}，class=1，langCode=ENG）
 * @author sunjiawei
 */
public class FMD0160 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			for(IxPoiName nameTmp:names){
				if(nameTmp.isEng()&&nameTmp.isOfficeName()&&nameTmp.isOriginName()){
					String name=nameTmp.getName();
					if(name==null||name.isEmpty()){continue;}
					if(name.length()>150){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "官方原始英文名称长度不能大于150！");
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
