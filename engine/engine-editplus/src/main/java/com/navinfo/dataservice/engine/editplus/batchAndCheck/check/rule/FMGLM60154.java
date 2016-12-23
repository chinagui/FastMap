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
/**
 * FM-GLM60154	POI中文名称检查	DHM	
 * 检查条件：
 *   该POI发生变更(新增或修改主子表、删除子表)
 *   检查原则：
 *     1.检查POI标准化官方中文（LANG_CODE=CHI或CHT）名称中“(”与“)”应成对出现；
 *     2、括号“（”和“）”中间必须有内容；
 *     3、不允许括号嵌套；
 *     否则报出：POI中文名称中“(”与“)”应成对出现
 *     备注：此处只能是全角的括号
 * @author zhangxiaoyi
 */
public class FMGLM60154 extends BasicCheckRule {
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			for(IxPoiName nameTmp:poiObj.getIxPoiNames()){
				if(nameTmp.isCH()&&nameTmp.isOfficeName()&&nameTmp.isStandardName()){
					String name=nameTmp.getName();
					if(name.contains("(")||name.contains(")")){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "POI中文名称中存在半角空格");
						return;
					}
					String errorMsg=CheckUtil.isRightKuohao(name);
					if(errorMsg!=null){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), errorMsg);
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
