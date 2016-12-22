package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

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
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScPointNameckUtil;
/**
 * FM-CHR73001	繁体字检查	D	
 * 检查条件：
 *     该POI发生变更(新增或修改主子表、删除子表)
 *     检查原则：
 *     NAME(LANG_CODE=""CHI"")，不能有繁体字。查找的繁体字在TY_CHARACTER_FJT_HZ中所在行的
 *     CONVERT字段的值：
 *     1、如果是2表示必须转化，报log：**是繁体字，对应的简体字是**，必须转化
 *     检查名称：标准化中文名称（NAME_TYPE=1，NAME_CLASS={1}，LANG_code=CHI）
 * @author zhangxiaoyi
 *
 */
public class FMCHR73001 extends BasicCheckRule {
	MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			for(IxPoiName nameTmp:names){
				if(nameTmp.getLangCode().equals("CHI")&&nameTmp.getNameType()==1
						&&(nameTmp.getNameClass()==1 ||nameTmp.getNameClass()==5)){
					String name=nameTmp.getName();
					if(name==null){continue;}
					String[] pyList=metadataApi.pyConvert(name);
					boolean isRightPy=false;
					for(String py:pyList){
						if(py.equals(nameTmp.getNamePhonetic())){
							isRightPy=true;
							break;
						}
					}
					if(!isRightPy){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
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
