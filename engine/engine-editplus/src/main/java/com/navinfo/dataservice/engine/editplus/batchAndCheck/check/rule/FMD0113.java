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
 * FM-D01-13   别名中文名称中空格错误	DHM	
 * 检查条件：非删除POI对象
 * 检查原则：
 * 1、别名曾用名中文名称（name_type=1，name_class={3，6}，langCode=CHI或CHT）字段内容中存在空格，且空格前后若为以下组合，将Err的情况，程序报log：名称中存在空格，请检查！；---见空格规则表【备注】sheet页序号=6
 * 2、前后空格检查：不能以空格开头或结尾，报log：名称中存在空格，请检查！
 * 3、多个空格检查：不能出现连续空格，报log：名称中存在连续空格，请检查！
 * 4、回车符检查：不能包含回车符，报log：名称中存在回车符，请检查！
 * 5、Tab符检查：不能包含Tab符号，报log：名称中存在Tab符号，请检查！
 * 以上查出的问题有几种情况报几个log。
 * 排除：空格前后的字或词一样时，不用报Log。
 * @author gaopengrong
 *
 */
public class FMD0113 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			for(IxPoiName nameTmp:names){
				if(nameTmp.isCH()&&nameTmp.getNameType()==1
						&&(nameTmp.getNameClass()==3 ||nameTmp.getNameClass()==6)){
					String nameStr=CheckUtil.strQ2B(nameTmp.getName());
					List<String> errorList=new ArrayList<String>();
					errorList=CheckUtil.checkIllegalBlank(nameStr);
					if(!CheckUtil.blankRuleTable(nameStr)){
						errorList.add("非法空格");
					}
					if(errorList.size()>0){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),
								"标准化中文名称空格错误："+errorList.toString().replace("[", "").replace("]", ""));
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
