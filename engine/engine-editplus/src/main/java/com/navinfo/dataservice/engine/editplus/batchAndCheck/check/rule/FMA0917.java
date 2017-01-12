package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;


/**
 * 检查条件：
 * 该POI发生变更(新增或修改主子表、删除子表)；
 * 检查原则：
 * 将拆分后的18个字段按“省名、市名、区县名、乡镇街道办、地名小区名、街巷名、标志物名、前缀、门牌号、类型名、子号、后缀、附属设施名、楼栋号、楼门号、楼层、房间号、附加信息”，每个字段分别检查。
 * 允许存在半角空格以及“TY_CHARACTER_EGALCHAR_EXT”表，“EXTENTION_TYPE”字段中，“ENG_H_U”、“ENG_H_L”、“DIGIT_H”、“SYMBOL_H”类型对应的“CHARACTER”字段的内容，
 * 和 “EXTENTION_TYPE ”字段里“SYMBOL_F”类型，在全半角对照关系表中（TY_CHARACTER_FULL2HALF表）FULL_WIDTH字段一致，找到FULL_WIDTH字段对应的半角“HALF_WIDTH”
 * （如果“HALF_WIDTH”字段对应的半角字符为空，则FULL_WIDTH字段对应的全角字符也是拼音的合法字符）的字符，如果存在以外的POI，全部报出。
 * 提示：地址拼音非法字符检查：**字段含有非法字符“xx”。
 * 备注：如果有多个字段有错误，用半角逗号分隔。
 * @author gaopengrong
 */
public class FMA0917 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj=(IxPoiObj) obj;
		IxPoi poi=(IxPoi) poiObj.getMainrow();	
		List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();
		if (addresses.size()==0) {
			return;
		}
		MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		List<String> charList = metaApi.halfCharList();
		String errorStr = "";
		for (IxPoiAddress addr:addresses) {
			if (!addr.isCH()) {
				continue;
			}
			String mergeAddrPhonetic = CheckUtil.getMergerAddrPhonetic(addr);
			if(mergeAddrPhonetic==null||mergeAddrPhonetic.isEmpty()){continue;}
			for (int i=0;i<mergeAddrPhonetic.length();i++) {
				char character = mergeAddrPhonetic.charAt(i);
				if (!charList.contains(character)) {
					errorStr += character;
				}
			}
		}
		if (errorStr.length()>0) {
			String error = "地址拼音中含有非法字符“"+errorStr+"”。";
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),error);
		}
		

	}
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
