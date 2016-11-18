package com.navinfo.dataservice.engine.check.rules;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiDetail;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * @ClassName: CheckRuleFMYW20218
 * @author: zhangpengpeng
 * @date: 2016年11月12日
 * @Desc: 通用深度信息网址格式检查 原则：非删除（根据履历判断删除）
 *        检查原则：（网址：IX_POI_DETAIL.WEB_SITE）（只针对有值得检查） 1.网址信息以http://开头
 *        log:网址信息格式错误，网址不是以”http://”开头 2.网址信息不能存在空格，tab符，回车符
 *        log:网址信息格式错误，网址中存在Tab符、回车符或者空格 3.网址信息不能为http://* /*格式
 *        log:网址信息格式错误，网址中存在多余的“/” 4.网址信息不以“\”结尾 log:网址信息格式错误，网址以“\”结尾
 */
public class CheckRuleFMYW20218 extends baseRule {

	public void preCheck(CheckCommand checkCommand) {
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {
		List<IRow> objList = checkCommand.getGlmList();
		for (IRow obj : objList) {
			if (obj instanceof IxPoi) {
				IxPoi poi = (IxPoi) obj;
				// 需要判断POI的状态不为删除
				LogReader logRead = new LogReader(this.getConn());
				int poiState = logRead.getObjectState(poi.getPid(), "IX_POI");
				// state=2为删除
				if (poiState == 2) {
					return;
				}
				// 获取POI通用深度信息
				List<IRow> poiDetails = poi.getDetails();
				if (poiDetails.size() > 0) {
					for (IRow detail : poiDetails) {
						IxPoiDetail poiDetail = (IxPoiDetail) detail;
						String webSite = poiDetail.getWebSite();
						// 只针对有值的检查
						if (StringUtils.isNotEmpty(webSite)) {
							StringBuffer logMsg = new StringBuffer();
							// 网址信息以http://开头
							if (!webSite.startsWith("http://")) {
								logMsg.append("网址不是以”http://”开头; ");
							}
							// 网址信息不能存在空格，tab符，回车符
							Pattern pattern = Pattern.compile("\\s|\t|\r|\n");
							Matcher matcher = pattern.matcher(webSite);
							boolean find = matcher.find();
							if (find) {
								logMsg.append("网址中存在Tab符、回车符或者空格; ");
							}
							// 网址信息不能为http://*/*格式
							// 对website去掉开头的“http://”部分后，判断有无"/",有则报log
							String webSiteEnd = webSite.replaceFirst("\\w+://", "");
							if (webSiteEnd.contains("/")) {
								logMsg.append("网址中存在多余的“/”; ");
							}
							// 网址信息不以“\”结尾
							if (webSite.endsWith("\\")) {
								logMsg.append("网址以'\'结尾; ");
							}
							// 判断有没有log信息
							String log = logMsg.toString();
							if (log.length() > 0) {
								String checkLog = "网址信息格式错误：" + log;
								this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
										checkLog);
							}

						}
					}
				}
			}
		}
	}
}
