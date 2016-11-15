package com.navinfo.dataservice.engine.check.rules;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
 * @ClassName: CheckRuleFMZY20154 
 * @author Gao Pengrong
 * @date 2016-11-14 上午9:41:24 
 * @Description: 1.可以为空；2.字符串；3.不为空时，字符串只能含有{10,11,12,13,14,15，|}，{10,11,12,13,14，15}中的每个只能出现一次，多个出现时，用半角"|"分隔。
					Log1：停车场支付方式的值没有"|"且不为空时，值不在{10,11,12,13,14,15}中；
					Log2：停车场支付方式的值有"|"时，"|"前后的值不在{10,11,12,13,14,15}中；
					Log3：（停车场支付方式）重复。
 */

public class CheckRuleFMZY20154 extends baseRule {
	
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
		List<IRow> rows = ixPoi.getParkings();
		String log;
		List<String> defaultList = Arrays.asList("10","11","12","13","14","15");
		
		for(IRow row : rows)
		{
			IxPoiParking poiPark = (IxPoiParking) row;
			String payment=poiPark.getPayment(); 
			Arrays.asList(defaultList).contains('|');
			if ((!"".equals(payment)) && (payment.indexOf("\\|")<0) && (!defaultList.contains(payment))){
                log="停车场支付方式的值没有'|',且不为空时,值不在{10,11,12,13,14,15}中";
                this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
			}
			else if(!"".equals(payment) && payment.indexOf("|")>=0){
				List<String> strDuplicate = new ArrayList<String>();
				String[] strArray = payment.split("\\|");
				for(int i = 0; i < strArray.length; i++){
					String str=strArray[i];
					if (!defaultList.contains(str)){
						log="停车场支付方式的值有'|'时，'|'前后的值不在{10,11,12,13,14,15}中";
						this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
					} else if (strDuplicate.contains(str)){
						log="（停车场支付方式）重复";
						this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
					}
					strDuplicate.add(str);
				}
			}
			
		}
	}
}