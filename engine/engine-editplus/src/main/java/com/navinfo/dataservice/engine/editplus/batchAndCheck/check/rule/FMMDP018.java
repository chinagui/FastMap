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
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * 检查条件：
 * 非删除（根据履历判断删除）
 * 检查原则：(收费信息字段：IX_POI_PARKING.TOLL_DES)
 * 1.包含  半小时、半小時、0.5小时、0.5H，大型车、大型車、小型车、小型車、空格等字样内容时报出
 * 2.收费信息包含“：00”，营业时间（IX_POI_PARKING.OPEN_TIME）为空
 * 3.存在连续两个及两个以上的字符（此处字符特指TY_CHARACTER_EGALCHAR_EXT.EXTENTION_TYPE in (“SYMBOL_F”，“GBK_SYMBOL_F”)对应的“CHARACTER”的字符）时报出
 * 4.时间段中—前后两个时间中的“：”之后只能紧跟两位阿拉伯数字，否则报出；单个时间格式为H：MM，小时位若小于10时，只能是一位，例如7点，不能是07，分钟位无要求

 * log1:收费信息内容不满足格式，存在半小时、0.5小时、0.5H，大型车、小型车、空格  
 * log2:收费信息包含“：00”，营业时间为空
 * log3：符号连续出现多次
 * log4：时间格式错误
 */
public class FMMDP018 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();	
			List<IxPoiParking> ixPoiParkings = poiObj.getIxPoiParkings();
			//错误数据
			if(ixPoiParkings==null || ixPoiParkings.isEmpty()){return;}
			//返回“TY_CHARACTER_EGALCHAR_EXT”表中数据
			MetadataApi api=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Map<String, List<String>> map = api.tyCharacterEgalcharExtGetExtentionTypeMap();
			for (IxPoiParking ixPoiParking : ixPoiParkings) {
				//收费描述
				String tollDes = ixPoiParking.getTollDes();
				if(tollDes == null){return;}

				//1.包含  半小时、半小時、0.5小时、0.5H，大型车、大型車、小型车、小型車、空格等字样内容时报出
				if (tollDes.contains("半小时") || tollDes.contains("半小時") || tollDes.contains("０．５小时")
						|| tollDes.contains("０．５Ｈ") || tollDes.contains("大型车") || tollDes.contains("大型車")
						|| tollDes.contains("小型车") || tollDes.contains("小型車") || tollDes.contains("　")) {
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), 
							"收费信息内容不满足格式，存在半小时、0.5小时、0.5H，大型车、小型车、空格");
				}
	
				//2.收费信息包含“：00”，营业时间（parkings.openTime）为空
				if (tollDes.contains("：００")) {
					String openTime = ixPoiParking.getOpenTiime();
					if (StringUtils.isEmpty(openTime)) {
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"收费信息包含“：00”，营业时间为空");
					}
				}
				//4.时间段中—前后两个时间中的“：”之后只能紧跟两位阿拉伯数字，否则报出；单个时间格式为H：MM，小时位若小于10时，只能是一位，例如7点，不能是07，分钟位无要求
				String tollDesH = ExcelReader.f2h(tollDes);
				Pattern pattern = Pattern.compile("\\d+:\\d+-\\d+:\\d+");
				Matcher matcher = pattern.matcher(tollDesH);
				String time = "";
				if(matcher.find()){
					matcher.reset();
					while(matcher.find()){
						time = matcher.group(0);
						String[] timeArray = time.split("-");
						if(timeArray.length == 2){
							if(StringUtils.isNotEmpty(timeArray[0])){
								String[] hoursArray = timeArray[0].split(":");
								if(hoursArray.length == 2){
									if(StringUtils.isNotEmpty(hoursArray[0])){
										if(hoursArray[0].startsWith("0")){
											setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
													"时间格式错误");
											break;
										}
									}
									if(StringUtils.isNotEmpty(hoursArray[1])&&hoursArray[1].length()!=2){
										setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
												"时间格式错误");
										break;
									}
								}
							}
							if(StringUtils.isNotEmpty(timeArray[1])){
								String[] hoursArray = timeArray[0].split(":");
								if(hoursArray.length == 2){
									if(StringUtils.isNotEmpty(hoursArray[0])){
										if(hoursArray[0].startsWith("0")){
											setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
													"时间格式错误");
											break;
										}
									}
									if(StringUtils.isNotEmpty(hoursArray[1])&&hoursArray[1].length()!=2){
										setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
												"时间格式错误");
										break;
									}
								}
							}
						}
					}
				}
				//3.存在连续两个及两个以上的字符（此处字符特指TY_CHARACTER_EGALCHAR_EXT.EXTENTION_TYPE in (“SYMBOL_F”，“GBK_SYMBOL_F”)对应的“CHARACTER”的字符）时报出
				//判断停车场收费信息中的字符是在合法字符集中
				List<String> strList=new ArrayList<String>();
				List<String> errorList=new ArrayList<String>();
				for (char nameSubStr : tollDes.toCharArray()) {
					String subStr = String.valueOf(nameSubStr);
					if(!map.get("GBK").contains(subStr)&&!map.get("ENG_F_U").contains(subStr)
							&&!map.get("ENG_F_L").contains(subStr)&&!map.get("DIGIT_F").contains(subStr)
							&&!map.get("SYMBOL_F").contains(subStr)&&!map.get("GBK_SYMBOL_F").contains(subStr)){
						strList.add(subStr);
					}
				}
				
				for(String str : strList) {
					int len = tollDes.length();
					int index=tollDes.indexOf(str);
					if(index>=0&&index<len){
						if(tollDes.substring(index, index+1).equals(str)){
							errorList.add(str);
							tollDes.replace(str, "");
						}
					}
				}
				if(errorList.size()>0){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "符号连续出现多次:“"
							+errorList.toString().replace("[", "").replace("]", "")+"”");
				}

			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
