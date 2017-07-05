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
 * FM-D01-69	官方标准英文名非法字符检查	DHM
 * 检查条件：
 * 非删除POI对象
 * 检查原则：
 * 如果官方标准英文名中存在数字，字母，符号（-_/:;'"~^.,?!*()<>$%&#@+半角空格）以外的POI，全部
 * 报log：英文名中含有非法字符“xx”！
 * 检查名称：官方标准英文名称（name_type=1，name_class=1，langCode=ENG）
 * @author sunjiawei
 */
public class FMD0169 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			for(IxPoiName nameTmp:names){
				if(nameTmp.isEng()&&nameTmp.getNameClass()==1&&nameTmp.getNameType()==1){
					String nameStr = nameTmp.getName();
					if(nameStr==null||nameStr.isEmpty()){continue;}
					List<String> errorList=new ArrayList<String>();
					for(char c:nameStr.toCharArray()){
						String cStr=String.valueOf(c);
						if(!CheckUtil.isValidEngChar(cStr)){
							errorList.add(cStr);
						}
					}
					if(errorList!=null&&errorList.size()>0){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "英文名中含有非法字符“"
					+errorList.toString().replace("[", "").replace("]", "")+"”");}
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
