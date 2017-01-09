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
 * 1.可以为空；2.字符串；3.不为空时，不能含有非法字符。
 * Log：地址道路名含有非法字符“XX”。
 *
 */
public class FMZY20136 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj=(IxPoiObj) obj;
		IxPoi poi=(IxPoi) poiObj.getMainrow();	
		List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();
		if (addresses.size()==0) {
			return;
		}
		MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		JSONObject characterMap = metaApi.getCharacterMap();
		List<String> errList = new ArrayList<String>();
		for (IxPoiAddress address:addresses) {
			String addStr = address.getRoadname();
			if (addStr == null || addStr.isEmpty()) {
				continue;
			}
			addStr = addStr.replace("|","");
			for (int i=0;i<addStr.length();i++) {
				char character = addStr.charAt(i);
				if (!characterMap.containsKey(String.valueOf(character))) {
					errList.add("地址道路名含有非法字符“"+character+"”。");
				}
			}
		}
		if (errList.size()>0) {
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),StringUtils.join(errList, ";"));
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
