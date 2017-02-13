package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiCarrental;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

import net.sf.json.JSONObject;

/**
 * 
 * 使用TY_CHARACTER_FJT_HZ.CONVERT=0的记录，
 * 若被处理字段中出现FT中的值，则替换为JT
 *
 */
public class FMBATTEMP12 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}
		MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		Map<String, JSONObject> charMap = metadataApi.tyCharacterFjtHzCheckSelectorGetFtExtentionTypeMap();
		List<IxPoiCarrental> carrentals = poiObj.getIxPoiCarrentals();
		for (IxPoiCarrental poiCarrental : carrentals) {
			String howToGo = poiCarrental.getHowToGo();
			if (howToGo == null) {
				continue;
			}
			for (char c:howToGo.toCharArray()) {
				if (charMap.containsKey(String.valueOf(c))) {
					JSONObject data = charMap.get(String.valueOf(c));
					int convert = data.getInt("convert");
					if (convert == 0) {
						poiCarrental.setHowToGo(howToGo.replace(c, data.getString("jt").toCharArray()[0]));
					}
				}
			}
		}

	}

}
