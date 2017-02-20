package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * @ClassName FMYW20224
 * @author Han Shaoming
 * @date 2017年2月14日 上午10:37:37
 * @Description TODO
 * 检查条件：  Lifecycle！=1（删除）
 * 检查原则：(收费信息字段：poi.parkings.tollDes)
 * 1.存在非法字符报出：除汉字、字母、罗马数字、阿拉伯数字及特殊字符以外的字符都是非法字符。
 * 特殊字符：＠　＿　－　／　；　：　～　＾　” ‘ ’　”　，　．　？　！　＊　＃　（　）　＜　＞　￥　＄　％　＆  ＋ ＇　　＂ •《》、·、。、|
 * 2.包含  半小时、半小時、0.5小时、0.5H，大型车、大型車、小型车、小型車、空格等字样内容时报出
 * 3.包含  年、月字样，并且收费标准（poi.parkings.tollStd)不包含0或1的报出
 * 4.收费信息超过127个字符报出
 * 5.存在非全角的内容报出
 * 6.收费信息包含“：00”，营业时间（parkings.openTime）为空
 */
public class FMYW20224 extends BasicCheckRule {

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
				//判断停车场收费信息中的字符是在合法字符集中
				List<String> errorList=new ArrayList<String>();
				for (char nameSubStr : tollDes.toCharArray()) {
					String subStr = String.valueOf(nameSubStr);
					if(!map.get("GBK").contains(subStr)&&!map.get("ENG_F_U").contains(subStr)
							&&!map.get("ENG_F_L").contains(subStr)&&!map.get("DIGIT_F").contains(subStr)
							&&!map.get("SYMBOL_F").contains(subStr)){
						errorList.add(subStr);
					}
				}
				if(errorList.size()>0){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "停车场收费信息存在非法字符“"
							+errorList.toString().replace("[", "").replace("]", "")+"”");
				}
				//2.包含  半小时、半小時、0.5小时、0.5H，大型车、大型車、小型车、小型車、空格等字样内容时报出
				if (tollDes.contains("半小时") || tollDes.contains("半小時") || tollDes.contains("０．５小时")
						|| tollDes.contains("０．５Ｈ") || tollDes.contains("大型车") || tollDes.contains("大型車")
						|| tollDes.contains("小型车") || tollDes.contains("小型車") || tollDes.contains("　")) {
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), 
							"收费信息内容不满足格式，存在半小时、0.5小时、0.5H，大型车、小型车、空格");
				}
				//3.包含  年、月字样，并且收费标准（poi.parkings.tollStd)不包含0或1的报出
				String tollStd = ixPoiParking.getTollStd();
				boolean tollFlag = false;
				if (tollDes.contains("年")) {
					if (StringUtils.isEmpty(tollStd) || !tollStd.contains("0")) {
						tollFlag = true;
					}
				}
				if (tollDes.contains("月")) {
					if (StringUtils.isEmpty(tollStd) || !tollStd.contains("1")) {
						tollFlag = true;
					}
				}
				if(tollFlag){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"收费信息与收费标准矛盾");
				}
				//4.收费信息超过127个字符报出
				if (tollDes.length() > 127) {
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"收费信息超长");
				}
				//5.存在非全角的内容报出
				String newTollDes = CheckUtil.strB2Q(tollDes);
				if (!newTollDes.equals(tollDes)) {
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"收费信息存在半角字符");
				}
				//6.收费信息包含“：00”，营业时间（parkings.openTime）为空
				if (tollDes.contains("：００")) {
					String openTime = ixPoiParking.getOpenTiime();
					if (StringUtils.isEmpty(openTime)) {
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"收费信息包含“：00”，营业时间为空");
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
