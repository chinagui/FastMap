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
 * 1.可以为空；2.字符串；3.不为空时，字符串只能含有{0,1,2,3,4,5,6,7,11,12,13,14,15,16,17,|}。
 * {0,1,2,3,4,5,6,7}和{11,12,13,14,15,16,17,|}不能同时出现，且每个只能出现一次。
 * {0,1,2,3,4,5,6,7}可以同时存在，{11,12,13,14,15,16,17}可以同时存在，多个出现时，用半角"|"分割。
 * Log1：大陆停车场收费备注的值有"|"时，"|"前后的值不在{0,1,2,3,4,5,6,7}中；
 * Log2：大陆停车场收费备注的值没有"|"时，值不在{0,1,2,3,4,5,6,7}中；
 * Log3：港澳停车场收费备注的值有"|"时，"|"前后的值不在{11,12,13,14,15,16,17}中；
 * Log4：港澳停车场收费备注的值没有"|"时，值不在{11,12,13,14,15,16,17}中；
 * Log5：（停车场收费备注）重复。
 * @author gaopengrong
 */
public class FMZY20155 extends BasicCheckRule {
	
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
			List<String> defaultList = Arrays.asList("0","1","2","3","4","5","6","7");
			String log;
			for(IxPoiParking parking : parkings){
				String remark=parking.getRemark(); 
				Arrays.asList(defaultList).contains('|');
				if (StringUtils.isNotEmpty(remark) && (remark.indexOf("\\|")<0) && (!defaultList.contains(remark))){
	                log="大陆停车场收费备注的值没有'|'时，值不在{0,1,2,3,4,5,6,7}中";
	                setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
				}
				else if(StringUtils.isNotEmpty(remark) && remark.indexOf("|")>=0){
					List<String> strDuplicate = new ArrayList<String>();
					String[] strArray = remark.split("\\|");
					for(int i = 0; i < strArray.length; i++){
						String str=strArray[i];
						if (!defaultList.contains(str)){
							log="大陆停车场收费备注的值有'|'时，'|'前后的值不在{0,1,2,3,4,5,6,7}中";
							setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
						} else if (strDuplicate.contains(str)){
							log="（停车场收费备注）重复";
							setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
						}
						strDuplicate.add(str);
					}
				}

			}		
		}
	}
}
