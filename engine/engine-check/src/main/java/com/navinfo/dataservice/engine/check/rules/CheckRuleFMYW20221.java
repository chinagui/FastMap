package com.navinfo.dataservice.engine.check.rules;

import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBusinessTime;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
* @ClassName: CheckRuleFMYW20221 
* @author: zhangpengpeng 
* @date: 2016年11月12日
* @Desc: 通用深度信息营业开始时间检查
		检查条件：非删除（根据履历判断删除）
		检查原则：营业开始时间IX_POI_BUSINESSTIME.TIME_SRT不能为24:00
*/
public class CheckRuleFMYW20221 extends baseRule{
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
						String timeSrt = ixPoiBusinessTime.getTimeSrt();
						if (!timeSrt.isEmpty()){
							if (timeSrt.equals("24:00")){
								this.setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(), "营业开始时间不能为24:00");
							}
						}
					}
				}
			}
		}
		
	}
}
