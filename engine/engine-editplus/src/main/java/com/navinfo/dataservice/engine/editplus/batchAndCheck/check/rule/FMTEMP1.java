package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiDetail;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 检查条件： 非删除（根据履历判断删除） 检查原则：（简介字段：IX_POI_DETAIL.BRIEF_DESC）
 * 1.简介中不能含有非法字符（遍历简介的值，如果值不在TY_CHARACTER_EGALCHAR_EXT.EXTENTION_TYPE in
 * (“GBK”,“ENG_F_U”,“ENG_F_L”,“DIGIT_F”,“SYMBOL_F”)对应的“CHARACTER”范围内）
 * 2.简介不超过127个字符 
 * 3.必须为全角 
 * 4.不能存在空格
 * 
 * log1：简介中**是非法字符 
 * log2：简介超长 
 * log3：简介存在半角字符 
 * log4：简介存在空格
 * 
 */
public class FMTEMP1 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}
		MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		Map<String, List<String>> charMap = metadataApi.tyCharacterEgalcharExtGetExtentionTypeMap();
		List<String> charList = new ArrayList<String>();
		if (charMap.containsKey("GBK")) {
			charList.addAll(charMap.get("GBK"));
		}
		if (charMap.containsKey("ENG_F_U")) {
			charList.addAll(charMap.get("ENG_F_U"));
		}
		if (charMap.containsKey("ENG_F_L")) {
			charList.addAll(charMap.get("ENG_F_L"));
		}
		if (charMap.containsKey("DIGIT_F")) {
			charList.addAll(charMap.get("DIGIT_F"));
		}
		if (charMap.containsKey("SYMBOL_F")) {
			charList.addAll(charMap.get("SYMBOL_F"));
		}
		List<IxPoiDetail> poiDetails = poiObj.getIxPoiDetails();
		for (IxPoiDetail poiDetail : poiDetails) {
			String briefDesc = poiDetail.getBriefDesc();
			if (briefDesc == null) {
				continue;
			}
			for (char c:briefDesc.toCharArray()) {
				if (!charList.contains(String.valueOf(c))) {
					this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
							"简介中"+c+"是非法字符");
				}
			}
			if (briefDesc.length()>127) {
				this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
						"简介超长");
			}
			
			String newBriefDesc = ExcelReader.h2f(briefDesc); // 调用半角转全角方法
			if (!newBriefDesc.equals(briefDesc)) {
				this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
						"简介存在半角字符");
			}

	        if (briefDesc.indexOf(" ")>=0||briefDesc.indexOf("　")>=0) {
				this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
						"简介存在空格");
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
