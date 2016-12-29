package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiCarrental;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

import net.sf.json.JSONObject;

/**
 * 1.可以为空；2.字符串；3.不为空时，不能含有非法字符；营业时间格式应为 **：**-**：** ，多组|分隔；"-"分隔起始和终止时间，起始时间不能为24：00，终止时间不能为00:00；字段应该是全角
    Log1：汽车租赁开放时间含有非法字符“XX”。
    Log2：营业时间格式错误。
    Log3：营业时间开始时间或结束时间错误
    Log4: 营业时间还有半角字符
 * @author gaopengrong
 */
public class FMZY20198 extends BasicCheckRule {
	
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
			List<IxPoiCarrental> carrentals = poiObj.getIxPoiCarrentals();
			String log;
			for(IxPoiCarrental poiCarrental : carrentals){
				String openHour=poiCarrental.getOpenHour();
				
				if (StringUtils.isNotEmpty(openHour)){
				
					if (!openHour.equals(ExcelReader.h2f(openHour))){
						log = "营业时间含有半角字符。";
						setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
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
						setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
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
							setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
						} else if ("２４：００".equals(str.substring(0, 5)) || "００：００".equals(str.substring(6))){
							log = "营业时间开始时间或结束时间错误。";
							setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
						}
					}
				}
			}		
		}
	}
}
