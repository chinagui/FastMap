package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

import net.sf.json.JSONObject;

/**
 * 检查条件： 非删除（根据履历判断删除） 检查原则：(收费信息字段：IX_POI_PARKING.TOLL_DES)
 * 1.停车场收费描述TOLL_DES不能有繁体字（TY_CHARACTER_FJT_HZ.CONVERT=
 * 0且TOLL_DES字段包含TY_CHARACTER_FJT_HZ.FT的值）
 * 
 * log1**是繁体字，对应的简体是**，请确认是否需要简化 注：对应的简体：TY_CHARACTER_FJT_HZ.JT的值
 * 
 */
public class FMMDP005 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}

		// 调用元数据请求接口
		MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		Map<String, JSONObject> charMap = metaApi.tyCharacterFjtHzCheckSelectorGetFtExtentionTypeMap();

		List<IxPoiParking> parkings = poiObj.getIxPoiParkings();
		for (IxPoiParking parking : parkings) {
			String tollDes = parking.getTollDes();
			if (tollDes == null) {
				continue;
			}
			for (char c : tollDes.toCharArray()) {
				if (charMap.containsKey(String.valueOf(c))) {
					JSONObject data = charMap.get(String.valueOf(c));
					int convert = data.getInt("convert");
					if (convert == 0) {
						this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
								c + "是繁体字，对应的简体是" + data.getString("jt") + "，请确认是否需要简化 ");
					}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
