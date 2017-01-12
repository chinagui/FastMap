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

import net.sf.json.JSONObject;

/**
 * 
 * 1.可以为空；2.字符串；3.不为空时，不能含有非法字符。 Log：地址**含有非法字符“XX”。
 * 18个字段：省名、市名、区县名、街道名、小区名、街巷名、标志物名、前缀、门牌号、类型名、子号、后缀、附属设施名、楼栋号、楼门号、楼层、房间号、附加信息
 *
 */
public class FMZY20136 extends BasicCheckRule {

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
			checkStr(address.getProvince(),characterMap,"省名",errList);
			checkStr(address.getCity(),characterMap,"市名",errList);
			checkStr(address.getCounty(),characterMap,"区县名",errList);
			checkStr(address.getTown(),characterMap,"乡镇街道办",errList);
			checkStr(address.getPlace(),characterMap,"地名小区名",errList);
			checkStr(address.getStreet(),characterMap,"街巷名",errList);
			checkStr(address.getLandmark(),characterMap,"标志物名",errList);
			checkStr(address.getPrefix(),characterMap,"前缀",errList);
			checkStr(address.getHousenum(),characterMap,"门牌号",errList);
			checkStr(address.getType(),characterMap,"类型名",errList);
			checkStr(address.getSubnum(),characterMap,"子号",errList);
			checkStr(address.getSurfix(),characterMap,"后缀",errList);
			checkStr(address.getEstab(),characterMap,"附属设施名",errList);
			checkStr(address.getBuilding(),characterMap,"楼栋号",errList);
			checkStr(address.getFloor(),characterMap,"楼层",errList);
			checkStr(address.getUnit(),characterMap,"楼门号",errList);
			checkStr(address.getRoom(),characterMap,"房间号",errList);
			checkStr(address.getAddons(),characterMap,"附加信息",errList);
		}
		if (errList.size() > 0) {
			setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
					StringUtils.join(errList, ";"));
		}
	}
	
	private void checkStr(String addStr,JSONObject characterMap,String colName,List<String> errList) {
		if (addStr == null || addStr.isEmpty()) {
			return;
		}
		for (int i = 0; i < addStr.length(); i++) {
			char character = addStr.charAt(i);
			if (!characterMap.containsKey(String.valueOf(character))) {
				errList.add("地址"+colName+"含有非法字符“" + character + "”。");
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
