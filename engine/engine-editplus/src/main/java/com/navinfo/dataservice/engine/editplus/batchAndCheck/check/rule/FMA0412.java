package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
/**
 * FM-A04-12	POI中文名称长度检查	DHM	
 * 检查条件：
 * 该POI发生变更(新增或修改主子表、删除子表)
 * 检查原则： 
 * 标准化中文名称字段大于35个字符（不区分全半角，只计算字符个数，汉字也属于1个字符），需要报出。
 * 提示：POI中文名称长度检查：POI中文名称长度不能大于35个字符
 * 检查名称：标准化中文名称（NAME_TYPE=1，class={1,5}，LANG_CODE=CHI或CHT）
 * @author zhangxiaoyi
 *
 */
public class FMA0412 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			for(IxPoiName nameTmp:poiObj.getIxPoiNames()){
				if(nameTmp.isCH()&&nameTmp.getNameType()==1&&(nameTmp.getNameClass()==1||nameTmp.getNameClass()==5)){
					String name=nameTmp.getName();
					if(name.length()>35){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
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
