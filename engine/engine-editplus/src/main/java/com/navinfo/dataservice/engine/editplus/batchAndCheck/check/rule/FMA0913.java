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

import net.sf.json.JSONObject;

/**
 * 检查条件：
 *	该POI发生变更(新增或修改主子表、删除子表)；
 *	检查原则：
 *	将拆分后的18个字段按 “省名、市名、区县名、乡镇街道办、地名小区名、街巷名、标志物名、前缀、门牌号、类型名、子号、后缀、附属设施名、楼栋号、楼门号、楼层、房间号、附加信息”顺序合并（各字段“LANG_CODE”为“CHI（中国大陆）或CHT（繁体）”）合并后检查：允许存在“TY_CHARACTER_EGALCHAR_EXT”表，“EXTENTION_TYPE”字段中 “GBK”、“ENG_F_U”、“ENG_F_L”、“DIGIT_F”、“SYMBOL_F”类型对应的“CHARACTER”字段的内容，如果合并后地址存在其他字符，将此条POI报出。
 *	提示：地址非法字符检查：地址中含有非法字符“xx”
 *
 */
public class FMA0913 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj=(IxPoiObj) obj;
		IxPoi poi=(IxPoi) poiObj.getMainrow();	
		List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();
		if (addresses.size()==0) {
			return;
		}
		MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		JSONObject characterMap = metaApi.tyCharacterEgalcharExt();
		String errorStr = "";
		for (IxPoiAddress addr:addresses) {
			if (!addr.getLangCode().equals("CHI") && !addr.getLangCode().equals("CHT")) {
				continue;
			}
			String mergeAddr = CheckUtil.getMergerAddr(addr);
			for (int i=0;i<mergeAddr.length();i++) {
				char character = mergeAddr.charAt(i);
				if (!characterMap.containsKey(String.valueOf(character))) {
					errorStr += character;
				}
			}
		}
		if (errorStr.length()>0) {
			String error = "地址中含有非法字符“"+errorStr+"”。";
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),error);
		}
		

	}
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
