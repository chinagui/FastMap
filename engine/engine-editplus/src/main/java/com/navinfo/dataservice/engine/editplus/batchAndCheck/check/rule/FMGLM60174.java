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
 * FM-GLM60174	英文名括号检查	DHM	
 * 检查条件：该POI发生变更(新增或修改主子表、删除子表)；
 * 检查原则：英文名中若出现括号，则
 *   1、括号“（”和“）”要成对出现，否则报log1；
 *   2、括号“（”和“）”中间必须有内容，否则报log2；
 *   3、括号中不能再嵌套括号，否则报log3；
 *   log1：英文名中括号需要成对出现
 *   log2：英文中括号中必须存在内容
 *   log3：英文中不能出现括号嵌套括号情况
 *   备注：此处的括号应该都是半角的
 *   检查名称：官方英文名称（NAME_TYPE={1,2}，NAME_CLASS=1，LANG_CODE=ENG）
 * @author zhangxiaoyi
 */
public class FMGLM60174 extends BasicCheckRule {

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
					String error = CheckUtil.isRightKuohao(nameStr);
					if(error!=null&&!error.isEmpty()){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),error);
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
