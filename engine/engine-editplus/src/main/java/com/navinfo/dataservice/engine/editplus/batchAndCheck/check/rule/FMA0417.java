package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScPointNameckUtil;
/**
 * FM-A04-17	标准化中文名称与拼音匹配性检查	DHM
 * 检查条件：
 *     以下条件其中之一满足时，需要进行检查：
 *     (1) 存在IX_POI_NAME新增；
 *     (2) 存在IX_POI_NAME修改；
 *     检查原则：
 *     名称发音（LNAG_CODE=""ENG""或""POR""）不属于自动转拼音中的任何一个则报出。
 *     提示：标准化中文名称与拼音匹配性检查：POI名称与POI拼音不匹配
 *     检查名称：标准化中文名称（NAME_TYPE=1，NAME_CLASS={1,5}，LANG_CODE=CHI或CHT）
 * @author zhangxiaoyi
 *
 */
public class FMA0417 extends BasicCheckRule {
	MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			for(IxPoiName nameTmp:names){
				if(nameTmp.isCH()&&nameTmp.getNameType()==1
						&&(nameTmp.getNameClass()==1 ||nameTmp.getNameClass()==5)&&isCheck(nameTmp)){
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
	
	private boolean isCheck(IxPoiName poiName){
		if(poiName.getHisOpType().equals(OperationType.INSERT)){
			return true;
		}
		if(poiName.getHisOpType().equals(OperationType.UPDATE) && poiName.hisOldValueContains(IxPoiName.NAME)){
			String oldNameStr=(String) poiName.getHisOldValue(IxPoiName.NAME);
			String newNameStr=poiName.getName();
			if(!newNameStr.equals(oldNameStr)){return true;}
		}
		return false;
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
