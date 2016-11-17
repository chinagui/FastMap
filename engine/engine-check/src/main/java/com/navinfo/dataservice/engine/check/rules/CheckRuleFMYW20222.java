package com.navinfo.dataservice.engine.check.rules;

import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBusinessTime;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * @ClassName: CheckRuleFMYW20222
 * @author: zhangpengpeng
 * @date: 2016年11月14日
 * @Desc: 通用深度信息银行营业时长检查 检查条件：非删除（根据履历判断删除）and kindcode = 150101（银行）
 *        检查原则：银行营业时长IX_POI_BUSINESSTIME.TIME_DUR不能等于24小时 
 *        log：银行类营业时长为24小时
 */
public class CheckRuleFMYW20222 extends baseRule {
	public void preCheck(CheckCommand checkCommand) {
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			if (obj instanceof IxPoi) {
				IxPoi poi = (IxPoi) obj;
				//需要判断POI的状态不为删除
				LogReader logRead=new LogReader(this.getConn());
				int poiState = logRead.getObjectState(poi.getPid(), "IX_POI");
				//state=2为删除
				if (poiState == 2){
					return ;
				}
				String kindCode = poi.getKindCode();
				//银行营业时长 kindCode=150101
				if (kindCode.equals("150101")) {
					// 获取POI深度信息营业时间
					List<IRow> businessTimes = poi.getBusinesstimes();
					if (businessTimes.size() > 0) {
						for (IRow businessTime : businessTimes) {
							IxPoiBusinessTime ixPoiBusinessTime = (IxPoiBusinessTime) businessTime;
							String timeDur = ixPoiBusinessTime.getTimeDur();
							if (!timeDur.isEmpty()) {
								if (timeDur.startsWith("24")) {
									this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]",
											poi.getMeshId(), "银行类营业时长为24小时");
								}
							}
						}
					}
				}
			}
		}

	}
}
