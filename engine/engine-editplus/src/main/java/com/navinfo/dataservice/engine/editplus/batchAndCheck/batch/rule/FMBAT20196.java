package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiBusinesstime;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

public class FMBAT20196 extends BasicBatchRule {

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {return;}
		// POI的open24h=1批处理
		if (poi.getOpen24h() == 1) {
			List<IxPoiBusinesstime> poiBusinessTimes = poiObj.getIxPoiBusinesstimes();
			for (IxPoiBusinesstime poiBusinessTime : poiBusinessTimes) {
				poiBusinessTime.setMonSrt("1");
				poiBusinessTime.setMonEnd("12");
				poiBusinessTime.setWeekInYearSrt("1");
				poiBusinessTime.setWeekInYearEnd("-1");
				poiBusinessTime.setWeekInMonthSrt("1");
				poiBusinessTime.setWeekInMonthEnd("-1");
				poiBusinessTime.setValidWeek("1111111");
				poiBusinessTime.setDaySrt("1");
				poiBusinessTime.setDayEnd("-1");
				poiBusinessTime.setTimeSrt("00:00");
				poiBusinessTime.setTimeDur("24:00");
			}
		}

	}
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}
}
