package com.navinfo.dataservice.engine.check.rules;

import java.util.List;

import com.ctc.wstx.util.StringUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBusinessTime;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
* @ClassName: CheckRuleFMYW20220 
* @author: zhangpengpeng 
* @date: 2016年11月12日
* @Desc: 通用深度信息营业时长检查
* 		检查条件：非删除（根据履历判断删除）
		检查原则：
		1.营业时长IX_POI_BUSINESSTIME.TIME_DUR不能超过（大于）24小时        log1：营业时长超过24小时
		2.营业时长IX_POI_BUSINESSTIME.TIME_DUR不能为负数         log2：营业时长为负数
*/
public class CheckRuleFMYW20220 extends baseRule{
	public void preCheck(CheckCommand checkCommand){
	}

	public void postCheck(CheckCommand checkCommand) throws Exception{
		for (IRow obj: checkCommand.getGlmList()){
			if (obj instanceof IxPoi){
				IxPoi poi = (IxPoi) obj;
				//需要判断POI的状态不为删除
				LogReader logRead=new LogReader(this.getConn());
				int poiState = logRead.getObjectState(poi.getPid(), "IX_POI");
				//state=2为删除
				if (poiState == 2){
					return ;
				}
				//获取POI深度信息营业时间
				List<IRow> businessTimes = poi.getBusinesstimes();
				if (businessTimes.size() > 0){
					for (IRow businessTime: businessTimes){
						IxPoiBusinessTime ixPoiBusinessTime = (IxPoiBusinessTime) businessTime;
						String timeDur = ixPoiBusinessTime.getTimeDur();
						if (StringUtils.isNotEmpty(timeDur) && timeDur.contains(":")){
							//取出time_cur值，以":"分隔,判断小时数
							String[] time = timeDur.split(":");
							int hour = Integer.parseInt(time[0]);
							if (hour > 24){
								this.setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(), "营业时长超过24小时");
							}
							if (hour < 0){
								this.setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(), "营业时长为负数");
							}
						}
					}
				}
			}
		}
		
	}

}
