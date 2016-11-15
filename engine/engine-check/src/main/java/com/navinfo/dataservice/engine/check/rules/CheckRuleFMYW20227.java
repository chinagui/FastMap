package com.navinfo.dataservice.engine.check.rules;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.dao.log.LogReader;

/** 
 * @ClassName: CheckRuleFMYW20227
 * @author Gao Pengrong
 * @date 2016-11-15 上午10:30:24 
 * @Description: 检查条件：
				   非删除（根据履历判断删除）
				   检查原则：
				 1.收费标准（IX_POI_PARKING.TOLL_STD)为5,停车场收费备注(IX_POI_PARKING.REMARK)有值，则报log:收费标准为免费，停车场备注不为空。
 */

public class CheckRuleFMYW20227 extends baseRule {
	
	public void preCheck(CheckCommand checkCommand){
	}

	public void postCheck(CheckCommand checkCommand) throws Exception{
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof IxPoi){
				IxPoi ixPoi = (IxPoi)obj;
				innerCheck(ixPoi);	
				}
			}
	}
	
	public void innerCheck(IxPoi ixPoi) throws Exception{
		
		LogReader logReader = new LogReader(getConn());
		if (logReader.getObjectState(ixPoi.getPid(),"IX_POI")==2){
			return;
		}
		List<IRow> rows = ixPoi.getParkings();
		String log;
		for(IRow row : rows)
		{
			IxPoiParking poiPark = (IxPoiParking) row;
			String tollStd=poiPark.getTollStd();
			
			if ("5".equals(tollStd)){
				String remark=poiPark.getRemark();
				if (!"".equals(remark)){
					log = "收费标准为免费，停车场备注不为空。";
					this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
				}
			}
		}
	}
}