package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

import net.sf.json.JSONObject;

/**
 * 
 * 使用TY_CHARACTER_FJT_HM_CHECK中TYPE=1的记录， 
 * 若被处理字段中出现HZ中的值，则替换为CORRECT
 *
 */
public class FMBATTEMP3 extends BasicBatchRule {

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
		MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		JSONObject charMap = metadataApi.getTyCharacterFjtHmCheckMap(null,1);
		List<IxPoiParking> parkings = poiObj.getIxPoiParkings();
		for (IxPoiParking parking : parkings) {
			String openTiime = parking.getOpenTiime();
			if (openTiime == null) {
				continue;
			}
			for (char c:openTiime.toCharArray()) {
				if (charMap.containsKey(String.valueOf(c))) {
					parking.setOpenTiime(openTiime.replace(c, charMap.getString(String.valueOf(c)).toCharArray()[0]));
				}
			}
		}

	}

}
