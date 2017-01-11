package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * 1.可以为空；2.字符串；3.不为空时，字符串只能含有{10,11,12,13,14,15，|}，{10,11,12,13,14，15}中的每个只能出现一次，多个出现时，用半角"|"分隔。
					Log1：停车场支付方式的值没有"|"且不为空时，值不在{10,11,12,13,14,15}中；
					Log2：停车场支付方式的值有"|"时，"|"前后的值不在{10,11,12,13,14,15}中；
					Log3：（停车场支付方式）重复。
 * @author gaopengrong
 */
public class FMZY20154 extends BasicCheckRule {
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();	
			List<IxPoiParking> parkings = poiObj.getIxPoiParkings();
			List<String> defaultList = Arrays.asList("10","11","12","13","14","15");
			String log;
			for(IxPoiParking parking : parkings){
				String payment=parking.getPayment(); 
				if (StringUtils.isNotEmpty(payment) && (payment.indexOf("\\|")<0) && (!defaultList.contains(payment))){
	                log="停车场支付方式的值没有'|',且不为空时,值不在{10,11,12,13,14,15}中";
	                setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
				}
				else if(StringUtils.isNotEmpty(payment) && payment.indexOf("|")>=0){
					List<String> strDuplicate = new ArrayList<String>();
					String[] strArray = payment.split("\\|");
					for(int i = 0; i < strArray.length; i++){
						String str=strArray[i];
						if (!defaultList.contains(str)){
							log="停车场支付方式的值有'|'时，'|'前后的值不在{10,11,12,13,14,15}中";
							setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
						} else if (strDuplicate.contains(str)){
							log="（停车场支付方式）重复";
							setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
						}
						strDuplicate.add(str);
					}
				}
			}		
		}
	}
}
