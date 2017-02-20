package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 检查条件： 非删除（根据履历判断删除） 检查原则：（开放时间字段：IX_POI_PARKING.OPEN_TIME）
 * 1.不能含有非法字符（如果值不在TY_CHARACTER_EGALCHAR_EXT.EXTENTION_TYPE in
 * (“GBK”,“ENG_F_U”,“ENG_F_L”,“DIGIT_F”,“SYMBOL_F”)对应的“CHARACTER”范围内）
 * 2.找出字段中时间段（一组或多组，时间段为“起始时间：终止时间”的样式(起始/终止时间可能是**:**，也可能是*:**)；
 * 以"-"分隔起始和终止时间，参考右侧数据情况），要求，起始时间不能为24：00，终止时间不能为00:00或0:00 3.字段应该是全角
 * 
 * log1：**是非法字符 Log2：营业时间开始时间或结束时间错误 Log3: 营业时间存在半角字符
 * 
 * @author gaopengrong
 */
public class FMZY20152 extends BasicCheckRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}
		List<IxPoiParking> parkings = poiObj.getIxPoiParkings();
		// 调用元数据请求接口
		MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		Map<String, List<String>> charMap = metadataApi.tyCharacterEgalcharExtGetExtentionTypeMap();
		List<String> charList = new ArrayList<String>();
		if (charMap.containsKey("GBK")) {
			charList.addAll(charMap.get("GBK"));
		}
		if (charMap.containsKey("ENG_F_U")) {
			charList.addAll(charMap.get("ENG_F_U"));
		}
		if (charMap.containsKey("ENG_F_L")) {
			charList.addAll(charMap.get("ENG_F_L"));
		}
		if (charMap.containsKey("DIGIT_F")) {
			charList.addAll(charMap.get("DIGIT_F"));
		}
		if (charMap.containsKey("SYMBOL_F")) {
			charList.addAll(charMap.get("SYMBOL_F"));
		}

		for (IxPoiParking parking : parkings) {
			String openTiime = parking.getOpenTiime();
			if (openTiime == null) {
				continue;
			}

			String illegalChar = "";
			for (char c : openTiime.toCharArray()) {
				String str = String.valueOf(c);
				if (!charList.contains(str)) {
					illegalChar += str;
				}
			}
			if (!"".equals(illegalChar)) {
				setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
						illegalChar + "是非法字符");
			}
			
			if (!openTiime.equals(ExcelReader.h2f(openTiime))) {
				setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(), "营业时间含有半角字符");
			}

			// 上面已经做的全半角及非法字符检查，因此下面直接转成半角做格式检查
			openTiime = ExcelReader.f2h(openTiime);
			String[] openTimeArray = openTiime.split("-");
			for (int i = 0; i < openTimeArray.length; i++) {
				String startTime = openTimeArray[i];
				if (startTime.length()>=5) {
					startTime = startTime.substring(startTime.length() - 5, startTime.length());
				} 
				if (startTime.equals("24:00")) {
					setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
							"营业时间开始时间或结束时间错误");
					break;
				}
				if (i + 1 == openTimeArray.length) {
					break;
				}
				String endTime = openTimeArray[i + 1];
				if (endTime.length()>=5) {
					endTime = endTime.substring(0, 5);
				} else if (endTime.length()==4) {
					if (endTime.equals("0:00")) {
						setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
								"营业时间开始时间或结束时间错误");
						break;
					}
				}
				
				if (endTime.equals("00:00")) {
					setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
							"营业时间开始时间或结束时间错误");
					break;
				}
			}
		}
	}

}
