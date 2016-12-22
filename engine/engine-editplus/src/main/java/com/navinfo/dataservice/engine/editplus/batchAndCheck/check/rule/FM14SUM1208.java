package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScPointNameckUtil;
/**
 * FM-14Sum-12-08	名称中空格错误	DHM	
 * 检查条件：该POI发生变更(新增或修改主子表、删除子表)
 * 检查原则：
 * 1、字段内容中存在空格，且空格前后若为以下组合，将Err的情况，程序报出；---见空格规则表
 * 2、前后空格检查：不能以空格开头或结尾；
 * 3、多个空格检查：不能出现连续空格；
 * 4、回车符检查：不能包含回车符；
 * 5、Tab符检查：不能包含Tab符号；
 * 以上查出的问题有几种情况报几个log。
 * 排除：空格前后的字或词一样时，不用报Log。
 * 检查名称：标准化中文名称（NAME_TYPE=1，class={1,5}，LANG_CODE=CHI或CHT）
 * @author zhangxiaoyi
 *
 */
public class FM14SUM1208 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			for(IxPoiName nameTmp:names){
				if(nameTmp.isCH()&&nameTmp.getNameType()==1
						&&(nameTmp.getNameClass()==1 ||nameTmp.getNameClass()==5)){
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
