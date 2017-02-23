package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiCarrental;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

import net.sf.json.JSONObject;

/**
 * 
 * 检查条件： 非删除（根据履历判断删除） 检查原则： 
 * 1.不能存在简体汉字（简介字段值包含TY_CHARACTER_FJT_HM_CHECK.HZ字段的值）
 * 
 * log1：**是简体字，请确认
 *
 */
public class FMMDP012 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}
		// 调用元数据请求接口
		MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		JSONObject characterMap = metaApi.getTyCharacterFjtHmCheckMap(null,0);

		List<IxPoiCarrental> carrentals = poiObj.getIxPoiCarrentals();

		for (IxPoiCarrental poiCarrental : carrentals) {
			String howToGo = poiCarrental.getHowToGo();

			if (StringUtils.isEmpty(howToGo)) {
				continue;
			}
			for (char c : howToGo.toCharArray()) {
				if (characterMap.containsKey(String.valueOf(c))) {
					this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
							c + "是简体字，请确认");
				}
			}
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
