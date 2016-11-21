package com.navinfo.dataservice.engine.check.rules;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.engine.check.core.baseRule;

import net.sf.json.JSONObject;

/**
 * @ClassName: CheckRuleFMYW20224
 * @author: zhangpengpeng
 * @date: 2016年11月14日
 * @Desc:停车场收费信息检查 检查条件：非删除（根据履历判断删除） 检查原则：(收费信息字段：IX_POI_PARKING.TOLL_DES)
 *                 1.存在非法字符报出：除汉字、字母、罗马数字、阿拉伯数字及特殊字符以外的字符都是非法字符。 特殊字符：＠ ＿ － ／ ；
 *                 ： ～ ＾ ” ‘ ’ ” ， ． ？ ！ ＊ ＃ （ ） ＜ ＞ ￥ ＄ ％ ＆ ＋ ＇ ＂ •《》、·、。、|
 *                 2.包含 半小时、半小時、0.5小时、0.5H，大型车、大型車、小型车、小型車、空格等字样内容时报出
 *                 3.包含年、月字样，并且收费标准（IX_POI_PARKING.TOLL_STD)不为0或者1不包含0或1的报出
 *                 4.收费信息超过127个字符报出 5.存在非全角的内容报出
 *                 6.收费信息包含“：00”，营业时间（IX_POI_PARKING.OPEN_TIME）为空 LOG:
 *                 log1:停车场收费信息存在非法字符
 *                 log2:收费信息内容不满足格式，存在半小时、0.5小时、0.5H，大型车、小型车、空格 log3:收费信息与收费标准矛盾
 *                 log4:收费信息超长 log5:简介收费信息存在半角字符 log6:收费信息包含“：00”，营业时间为空
 */
public class CheckRuleFMYW20224 extends baseRule {
	public void preCheck(CheckCommand checkCommand) {
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			if (obj instanceof IxPoi) {
				IxPoi poi = (IxPoi) obj;
				// 需要判断POI的状态不为删除
				LogReader logRead = new LogReader(this.getConn());
				int poiState = logRead.getObjectState(poi.getPid(), "IX_POI");
				// state=2为删除的，不执行检查
				if (poiState == 2) {
					return;
				}
				List<IRow> parkings = poi.getParkings();
				if (parkings.size() > 0) {
					for (IRow parking : parkings) {
						IxPoiParking ixPoiParking = (IxPoiParking) parking;
						String tollDes = ixPoiParking.getTollDes();
						if (StringUtils.isNotEmpty(tollDes)) {
							// 加载合法字符
							Connection metaConn = DBConnector.getInstance().getMetaConnection();
							//调用元数据请求接口
							MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metaApi");
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
								String tollStd = ixPoiParking.getTollStd();
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
								String openTime = ixPoiParking.getOpenTiime();
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
			}
		}
	}
}
