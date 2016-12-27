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
 * FM-CHR71011	英文格式检查	DHM	
 * 检查条件：
 *    该POI发生变更(新增或修改主子表、删除子表)；
 *    检查原则：英文名称中若存在以下，则报出：
 *    1） 回车符检查：包含回车符的记录；
 *    2） Tab符检查：包含Tab符号的记录；
 *    3） 多个空格检查：两个及两个以上空格的记录；
 *    4） 前后空格检查：名称开始前或者结尾处包含空格的记录；
 *    提示：英文名称（LANG_CODE=""ENG""）格式：英文名称中不能名存在“xx” （提示信息中的符号全部用中文名称）
 *    检查名称：官方英文名称（NAME_TYPE={1,2}，NAME_CLASS=1，LANG_CODE=ENG）
 * @author zhangxiaoyi
 */
public class FMCHR71011 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			for(IxPoiName nameTmp:names){
				if((nameTmp.getNameType()==1||nameTmp.getNameType()==2)
						&&nameTmp.getNameClass()==1&&nameTmp.isEng()){
					String nameStr=nameTmp.getName();
					if(nameStr==null||nameStr.isEmpty()){continue;}
					List<String> error = CheckUtil.checkIllegalBlank(nameStr);
					if(error!=null&&error.size()>0){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), 
								"英文名称中不能存在“"+error.toString().replace("[", "").replace("]", "")+"”");
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
