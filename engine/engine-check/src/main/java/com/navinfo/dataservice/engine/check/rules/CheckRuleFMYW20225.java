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
 * @date 2016-11-15 下午16:30:24 
 * @Description: 检查条件：
					非删除（根据履历判断删除）
				  检查原则：(收费方式字段：IX_POI_PARKING.TOLL_WAY；支付方式字段：IX_POI_PARKING.PAYMENT)
					1.收费方式与支付方式不能同时有值
					2.收费方式只有大陆数据才能有值
					3.支付方式只能港澳数据才能有值
					log1：收费方式与支付方式同时有值
					log2：港澳数据，收费方式不能有值
					log3：大陆数据，支付方式不能有值
 */
//注：目前没有定下来大陆港澳的标示，先只做大陆，港澳后续补充
public class CheckRuleFMYW20225 extends baseRule {
	
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
			//对于大陆数据，支付方式不能有值。
			String payment=poiPark.getPayment();
			if (!"".equals(payment)){
				log = "大陆数据，支付方式不能有值";
				this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
			}
		}
	}
}