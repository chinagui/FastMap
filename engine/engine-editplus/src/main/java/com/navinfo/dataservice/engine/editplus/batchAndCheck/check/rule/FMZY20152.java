package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 检查条件： 非删除（根据履历判断删除） 检查原则：（开放时间字段：IX_POI_PARKING.OPEN_TIME）
 * 1.不能含有非法字符（如果值不在TY_CHARACTER_EGALCHAR_EXT.EXTENTION_TYPE in
 * (“GBK”,“ENG_F_U”,“ENG_F_L”,“DIGIT_F”,“SYMBOL_F”,“GBK_SYMBOL_F”)对应的“CHARACTER”范围内）
 * 2.找出字段中时间段（一组或多组，时间段为“起始时间：终止时间”的样式(起始/终止时间是**:**，即小时和分钟必须是两位)；
 * 以"-"分隔起始和终止时间，参考右侧数据情况），要求，起始时间不能为24：00，终止时间不能为00:00或0:00 3.字段应该是全角
 * 
 * log1：**是非法字符 Log2：营业时间开始时间或结束时间错误 Log3: 营业时间存在半角字符
 * 
 * @author gaopengrong
 * ===============================
 * @version 2.0
 * @desc 修改bug8744,判断开始时间和结束时间算法修改，并且考虑多组情况
 * @author z
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
		if (charMap.containsKey("GBK_SYMBOL_F")) {
			charList.addAll(charMap.get("GBK_SYMBOL_F"));
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
			Pattern pattern = Pattern.compile("\\d+:\\d+-\\d+:\\d+");
			Matcher matcher = pattern.matcher(openTiime);
			String time = "";
			if(matcher.find()){
				matcher.reset();
				while(matcher.find()){
					time = matcher.group(0);
					String[] timeArray = time.split("-");
					if(timeArray.length == 2){
						if((StringUtils.isNotEmpty(timeArray[0]) && timeArray[0].length()!=5)||
								(StringUtils.isNotEmpty(timeArray[1]) && timeArray[1].length()!=5)){
							setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
									"营业时间开始时间或结束时间错误");
							break;
						}
						if(StringUtils.isNotEmpty(timeArray[0]) && "24:00".equals(timeArray[0])){
							setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
									"营业时间开始时间或结束时间错误");
							break;
						}
						if(StringUtils.isNotEmpty(timeArray[1]) && ("00:00".equals(timeArray[1]) || "0:00".equals(timeArray[1]))){
							setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
									"营业时间开始时间或结束时间错误");
							break;
						}
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		String openTime = "早上：1:00-5:00,晚上：16:00-24:00abcasdewudbhai24:00-00:00";
		Pattern pattern = Pattern.compile("\\d+:\\d+-\\d+:\\d+");
		Matcher matcher = pattern.matcher(openTime);
		String time = "";
		if(matcher.find()){
			matcher.reset();
			while(matcher.find()){
				time = matcher.group(0);
				String[] timeArray = time.split("-");
				if("24:00".equals(timeArray[0])){
					System.out.println(timeArray[0]);
					System.out.println("startTime error");
				}
				if("00:00".equals(timeArray[1]) || "0:00".equals(timeArray[1])){
					System.out.println(timeArray[1]);
					System.out.println("endTime error");
				}
			}
		}
	}

}
