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
 * @ClassName: CheckRuleFMZY20155
 * @author Gao Pengrong
 * @date 2016-11-14 上午9:41:24 
 * @Description: 1.可以为空；2.字符串；3.不为空时，字符串只能含有{0,1,2,3,4,5,6,7,11,12,13,14,15,16,17,|}。
				{0,1,2,3,4,5,6,7}和{11,12,13,14,15,16,17,|}不能同时出现，且每个只能出现一次。{0,1,2,3,4,5,6,7}可以同时存在，
				{11,12,13,14,15,16,17}可以同时存在，多个出现时，用半角"|"分割。
					Log1：大陆停车场收费备注的值有"|"时，"|"前后的值不在{0,1,2,3,4,5,6,7}中；
					Log2：大陆停车场收费备注的值没有"|"时，值不在{0,1,2,3,4,5,6,7}中；
					Log3：港澳停车场收费备注的值有"|"时，"|"前后的值不在{11,12,13,14,15,16,17}中；
					Log4：港澳停车场收费备注的值没有"|"时，值不在{11,12,13,14,15,16,17}中；
					Log5：（停车场收费备注）重复。
 */

//注：目前没有定下来大陆港澳的标示，先只做大陆，港澳后续补充
public class CheckRuleFMZY20155 extends baseRule {
	
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
		List<String> defaultList = Arrays.asList("0","1","2","3","4","5","6","7");
		
		for(IRow row : rows)
		{
			IxPoiParking poiPark = (IxPoiParking) row;
			String remark=poiPark.getRemark(); 
			Arrays.asList(defaultList).contains('|');
			if ((!"".equals(remark)) && (remark.indexOf("\\|")<0) && (!defaultList.contains(remark))){
                log="大陆停车场收费备注的值没有'|'时，值不在{0,1,2,3,4,5,6,7}中";
                this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
			}
			else if((!"".equals(remark)) && remark.indexOf("|")>=0){
				List<String> strDuplicate = new ArrayList<String>();
				String[] strArray = remark.split("\\|");
				for(int i = 0; i < strArray.length; i++){
					String str=strArray[i];
					if (!defaultList.contains(str)){
						log="大陆停车场收费备注的值有'|'时，'|'前后的值不在{0,1,2,3,4,5,6,7}中";
						this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
					} else if (strDuplicate.contains(str)){
						log="（停车场收费备注）重复";
						this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
					}
					strDuplicate.add(str);
				}
			}
		}
	}
}