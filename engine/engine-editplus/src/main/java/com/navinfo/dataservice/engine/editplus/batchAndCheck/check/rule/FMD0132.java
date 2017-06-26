package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;
/**
 * 	FM-D01-32		中文名称拼音格式检查		DHM	
	检查条件：
	    非删除POI对象；
	检查原则：
	1） 回车符检查：包含回车符的记录；
	2） Tab符检查：包含Tab符号的记录；
	3） 多个空格检查：两个及两个以上连续空格的记录；
	4） 前后空格检查：名称开始前或者结尾处包含空格的记录；
	提示：中文名称拼音格式检查：POI拼音中不能存在“xx” （提示信息中的符号全部用中文名称）
	检查名称：标准化中文名称（name_type=1，name_class={1,3,5}，lang_code=CHI或CHT
 * @author sunjiawei
 *
 */
public class FMD0132 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			List<String> errorList=new ArrayList<String>();
			for(IxPoiName nameTmp:names){
				if(nameTmp.isCH()&&nameTmp.getNameType()==1
						&&(nameTmp.getNameClass()==1 ||nameTmp.getNameClass()==3
						||nameTmp.getNameClass()==5)){
					String nameStr=nameTmp.getNamePhonetic();
					if(nameStr==null||nameStr.isEmpty()){continue;}
					errorList=CheckUtil.checkIllegalBlank(nameStr);
					if(errorList.size()>0){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "POI拼音中不能存在“"
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
