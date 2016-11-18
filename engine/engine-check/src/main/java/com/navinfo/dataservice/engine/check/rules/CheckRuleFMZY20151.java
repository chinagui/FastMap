package com.navinfo.dataservice.engine.check.rules;

import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * @ClassName: CheckRuleFMZY20151
 * @author: zhangpengpeng
 * @date: 2016年11月14日
 * @Desc: 收费方式值域检查 检查条件：1.可以为空；2.字符串；
 *        3.不为空时，字符串只能含有{0,1,2,|}，{0,1,2}中每个只能出现一次，多个存在时，用半角"|"分隔。
 *        Log1：收费方式的值没有"|"且不为空时，值不在{0,1,2}中； 
 *        Log2：收费方式的值有"|"时，"|"前后的值不在{0,1,2}中；
 *        Log3：（收费方式）重复。
 */
public class CheckRuleFMZY20151 extends baseRule {
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
						String tollWay = ixPoiParking.getTollWay();
						String errorLog = new String();
						if (StringUtils.isNotEmpty(tollWay)) {
							String value = "0,1,2";
							if (!tollWay.contains("|") && !value.contains(tollWay)) {
								errorLog = "收费方式的值没有" + "'" + "|" + "'" + "且不为空时，" + "值不在{0,1,2}中";
							}
							if (tollWay.contains("|")) {
								StringBuffer sf = new StringBuffer();
								String[] tollWayArry = tollWay.split("|");
								for (String toll : tollWayArry) {

									if (!toll.isEmpty() || !"0,1,2".contains(toll)) {
										errorLog = "收费方式的值有" + "'" + "|" + "'时，" + "'" + "|" + "'" + "前后的值不在{0,1,2}中";
									}
									if (sf.toString().contains(toll)) {
										errorLog = "（收费方式）重复";
									}
									sf.append(toll + ",");
								}
							}
						}
						if (StringUtils.isNotEmpty(errorLog)) {
							this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
									errorLog);
						}

					}
				}
			}
		}
	}
}
