package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

import net.sf.json.JSONObject;

/**
 * 
 * 1.可以为空；2.字符串；3.不为空时，不能含有汉字和非法字符。 Log：地址**拼音含有汉字和非法字符“XX”。
 * 18个拼音字段：省名拼音、市名拼音、区县名拼音、街道名拼音、小区名拼音、街巷名拼音、标志物名拼音、前缀拼音、门牌号拼音、类型名拼音、子号拼音、后缀拼音、
 * 附属设施名拼音、楼栋号拼音、楼门号拼音、楼层拼音、房间号拼音、附加信息拼音
 *
 */
public class FMZY20137 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();
		if (addresses.size() == 0) {
			return;
		}
		MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		JSONObject characterMap = metaApi.getCharacterMap();
		List<String> errList = new ArrayList<String>();
		for (IxPoiAddress address : addresses) {
			checkPhonetic(address.getProvPhonetic(),characterMap,"省名",errList);
			checkPhonetic(address.getCityPhonetic(),characterMap,"市名",errList);
			checkPhonetic(address.getCountyPhonetic(),characterMap,"区县名",errList);
			checkPhonetic(address.getTownPhonetic(),characterMap,"乡镇街道办",errList);
			checkPhonetic(address.getPlacePhonetic(),characterMap,"地名小区名",errList);
			checkPhonetic(address.getStreetPhonetic(),characterMap,"街巷名",errList);
			checkPhonetic(address.getLandmarkPhonetic(),characterMap,"标志物名",errList);
			checkPhonetic(address.getPrefixPhonetic(),characterMap,"前缀",errList);
			checkPhonetic(address.getHousenumPhonetic(),characterMap,"门牌号",errList);
			checkPhonetic(address.getTypePhonetic(),characterMap,"类型名",errList);
			checkPhonetic(address.getSubnumPhonetic(),characterMap,"子号",errList);
			checkPhonetic(address.getSubnumPhonetic(),characterMap,"后缀",errList);
			checkPhonetic(address.getEstabPhonetic(),characterMap,"附属设施名",errList);
			checkPhonetic(address.getBuildingPhonetic(),characterMap,"楼栋号",errList);
			checkPhonetic(address.getFloorPhonetic(),characterMap,"楼层",errList);
			checkPhonetic(address.getUnitPhonetic(),characterMap,"楼门号",errList);
			checkPhonetic(address.getRoomPhonetic(),characterMap,"房间号",errList);
			checkPhonetic(address.getAddonsPhonetic(),characterMap,"附加信息",errList);
			
		}
		if (errList.size() > 0) {
			setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
					StringUtils.join(errList, ";"));
		}
	}
	
	private void checkPhonetic(String addStr,JSONObject characterMap,String colName,List<String> errList) {
		if (addStr == null || addStr.isEmpty()) {
			return;
		}
		addStr = addStr.replace("|", "");
		for (int i = 0; i < addStr.length(); i++) {
			char character = addStr.charAt(i);
			if (!characterMap.containsKey(String.valueOf(character))
					|| CheckUtil.isChinese(String.valueOf(character))) {
				errList.add("地址"+colName+"拼音含有汉字和非法字符“" + character + "”。");
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
