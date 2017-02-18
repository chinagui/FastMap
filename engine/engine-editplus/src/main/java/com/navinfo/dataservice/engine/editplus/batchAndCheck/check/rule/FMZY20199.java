package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiCarrental;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 检查条件： 非删除（根据履历判断删除） 检查原则：
 * 1.不能含有非法字符（如果值不在TY_CHARACTER_EGALCHAR_EXT.EXTENTION_TYPE in
 * (“GBK”,“ENG_F_U”,“ENG_F_L”,“DIGIT_F”,“SYMBOL_F”)对应的“CHARACTER”范围内） 2.不能含有空格
 * 3.字段应该是全角
 * 
 * log1：**是非法字符 log2：周边交通线路内容含有空格 Log3：周边交通线路还有半角字符
 * 
 * @author gaopengrong
 */
public class FMZY20199 extends BasicCheckRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}

		List<IxPoiCarrental> carrentals = poiObj.getIxPoiCarrentals();
		
		// 调用元数据请求接口
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

		for (IxPoiCarrental poiCarrental : carrentals) {
			String howToGo = poiCarrental.getHowToGo();

			if (StringUtils.isEmpty(howToGo)) {
				continue;
			}

			String illegalChar = "";
			for (char c : howToGo.toCharArray()) {
				if (!charList.contains(String.valueOf(c))) {
					illegalChar += c;
				} 

			}
			if (!"".equals(illegalChar)) {
				setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
						illegalChar + "是非法字符 ");
			}

			if (howToGo.indexOf(" ") >= 0 || howToGo.indexOf("　")>=0) {
				setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(), "周边交通线路内容含有空格 ");
			}

			if (!howToGo.equals(ExcelReader.h2f(howToGo))) {
				setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(), "周边交通线路还有半角字符");
			}
		}

	}

}
