package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

import net.sf.json.JSONObject;

public class FMYW20224 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}

		List<IxPoiParking> parkings = poiObj.getIxPoiParkings();
		for (IxPoiParking parking : parkings) {
			String tollDes = parking.getTollDes();
			if (StringUtils.isNotEmpty(tollDes)) {
				// 调用元数据请求接口
				MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
				JSONObject characterMap = metaApi.getCharacterMap();
				StringBuffer errorLog = new StringBuffer();
				// 判断停车场收费信息中的字符是在合法字符集中
				for (char c : tollDes.toCharArray()) {
					if (!characterMap.containsKey(String.valueOf(c))) {
						errorLog.append("停车场收费信息存在非法字符; ");
						break;
					}
				}
				// 包含 半小时、半小時、0.5小时、0.5H，大型车、大型車、小型车、小型車、空格等字样内容时报出
				if (tollDes.contains("半小时") || tollDes.contains("半小時") || tollDes.contains("０．５小时")
						|| tollDes.contains("０．５Ｈ") || tollDes.contains("大型车") || tollDes.contains("大型車")
						|| tollDes.contains("小型车") || tollDes.contains("小型車") || tollDes.contains("　")) {
					errorLog.append("收费信息内容不满足格式，存在半小时、0.5小时、0.5H，大型车、小型车、空格; ");
				}

				// 包含年、月字样，并且收费标准（IX_POI_PARKING.TOLL_STD)不为0或者1不包含0或1的报出
				if (tollDes.contains("年") || tollDes.contains("月")) {
					String tollStd = parking.getTollStd();
					if (StringUtils.isEmpty(tollStd) || !tollStd.contains("0") || !tollStd.contains("1")) {
						errorLog.append("收费信息与收费标准矛盾; ");
					}
				}

				// 收费信息超过127个字符报出
				if (tollDes.length() > 127) {
					errorLog.append("收费信息超长; ");
				}

				// 存在非全角的内容报出
				String newTollDes = ExcelReader.h2f(tollDes); // 调用半角转全角方法
				if (!newTollDes.equals(tollDes)) {
					errorLog.append("简介收费信息存在半角字符; ");
				}

				// 收费信息包含“：00”，营业时间（IX_POI_PARKING.OPEN_TIME）为空
				if (tollDes.contains("：00")) {
					// IX_POI_PARKING.OPEN_TIME营业时间为空
					String openTime = parking.getOpenTiime();
					if (StringUtils.isEmpty(openTime)) {
						errorLog.append("收费信息包含“：00”，营业时间为空; ");
					}
				}

				if (errorLog.toString().length() > 0) {
					this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
							errorLog.toString());
				}
			}
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
