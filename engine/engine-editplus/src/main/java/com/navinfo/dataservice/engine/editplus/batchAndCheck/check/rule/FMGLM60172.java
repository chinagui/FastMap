package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-GLM60172	英文名长度检查	DHM
 * 检查条件：
 * 该POI发生变更(新增或修改主子表、删除子表)；
 * 检查原则：
 * 1.原始英文名长度大于150,报LOG1：原始英文名长度不应大于150
 * 2.同一名称分组内，原始英文名称长度小于等于150，无标准化英文名：
 * a.重要分类内(提供配置表)，官方原始英文名长度大于35，报LOG2a：重点分类原始官方英文名长度大于35无标准化英文名
 * b.官方原始英文名长度大于35，报LOG2b：非官方原始英文名长度大于35无标准化英文名
 * 3.同一名称分组内，原始英文名称长度小于等于150，有标准化英文名
 * a.原始英文名小于等于35，报LOG3a：原始英文名长度小于35，不应存在标准化英文名
 * b.原始英文名和标准化英文名都大于35，报LOG3b：标准化英文名不应大于35
 * 4.同一名称分组内，有标准化英文名，无原始英文名，报LOG4：同一名称分组内，有标准化英文名，无原始英文名
 * 注：以上条件中“重点分类内”，即元数据表SC_POINT_SPEC_KINDCODE表中type为8的基础分类代码
 * 检查名称：官方英文名称（NAME_TYPE={1,2}，NAME_CLASS=1，LANG_CODE=ENG）
 * @author zhangxiaoyi
 */
public class FMGLM60172 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			if(names==null||names.size()==0){return;}
			for(IxPoiName nameTmp:names){
				if(!nameTmp.isCH()){
					String nameStr=nameTmp.getNamePhonetic();
					if(!(nameStr==null||nameStr.isEmpty())){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),null);
                        return;}
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
