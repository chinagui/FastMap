package com.navinfo.dataservice.engine.check.rules;
import java.sql.Connection;
import java.util.List;
import net.sf.json.JSONObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiCarrental;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;

/** 
 * @ClassName: CheckRuleFMZY20198
 * @author Gao Pengrong
 * @date 2016-11-14 下午8:41:24 
 * @Description: 1.可以为空；2.字符串；3.不为空时，不能含有非法字符；营业时间格式应为 **：**-**：** ，多组|分隔；"-"分隔起始和终止时间，
					起始时间不能为24：00，终止时间不能为00:00；字段应该是全角
					Log1：汽车租赁开放时间含有非法字符“XX”。
					Log2：营业时间格式错误。
					Log3：营业时间开始时间或结束时间错误
					Log4: 营业时间还有半角字符
 */

public class CheckRuleFMZY20198 extends baseRule {
	
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
		List<IRow> rows = ixPoi.getCarrentals();
		String log;
		for(IRow row : rows)
		{
			IxPoiCarrental poiCarrental = (IxPoiCarrental) row;
			String openHour=poiCarrental.getOpenHour();
			
			if (!"".equals(openHour)){
			
				if (!openHour.equals(ExcelReader.h2f(openHour))){
					log = "营业时间含有半角字符。";
					this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
				}
				//调用元数据请求接口
				MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
				JSONObject characterMap = metaApi.getCharacterMap();
				
				char[] openHourlist=new char[openHour.length()];
				openHourlist=openHour.toCharArray();
				
				String illegalChar = "";
				
				for(int i = 0; i < openHourlist.length; i++){
					 
					char c=openHourlist[i];
					String str=String.valueOf(c);
					
					if (!characterMap.has(str)){
						illegalChar+=str;
					}
				}
				if (!"".equals(illegalChar)){
					log = "汽车租赁开放时间含有非法字符'"+illegalChar+"';";
					this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
				}
				
				//上面已经做的全半角及非法字符检查，因此下面直接转成全角做格式检查
				openHour = ExcelReader.h2f(openHour);
				String[] strArray = openHour.split("\\｜");
				for(int i = 0; i < strArray.length; i++){
					String str=strArray[i];
					String rexp = "[０-９]{2}：[０-９]{2}－[０-９]{2}：[０-９]{2}";
					Pattern pat = Pattern.compile(rexp);  
					Matcher mat = pat.matcher(str);  
					if (!mat.matches()){
						log = "营业时间格式错误。";
						this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
					} else if ("２４：００".equals(str.substring(0, 5)) || "００：００".equals(str.substring(6))){
						log = "营业时间开始时间或结束时间错误。";
						this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
					}
				}
			}
			
		}
	}
}