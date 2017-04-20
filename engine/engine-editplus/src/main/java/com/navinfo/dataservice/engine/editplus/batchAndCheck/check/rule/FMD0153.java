package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;
/**
 * 	FM-D01-53		官方标准英文名括号检查		DHM	
	检查条件：非删除POI对象；
	检查原则：官方标准英文名中若出现括号，则
	  1、括号“（”和“）”要成对出现，否则报log：英文名中括号需要成对出现！
	  2、括号“（”和“）”中间必须有内容，否则报log：英文中括号中必须存在内容！
	  3、括号中不能再嵌套括号，否则报log：英文中不能出现括号嵌套括号情况！
	检查名称：官方标准英文名称（type={1}，class=1，langCode=ENG）
 * @author sunjiawei
 */
public class FMD0153 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			for(IxPoiName nameTmp:names){
				if(nameTmp.getNameType()==1&&nameTmp.getNameClass()==1
						&&nameTmp.isEng()){
					String nameStr=nameTmp.getName();
					if(nameStr==null||nameStr.isEmpty()){continue;}
					if(nameStr.indexOf("（") > -1 || nameStr.indexOf("）") > -1){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"英文名称中括号应该都是半角的");
						return;
					}
					String error = CheckUtil.isRightKuohao(nameStr);
					if(error!=null&&!error.isEmpty()){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"英文名中"+error);
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
