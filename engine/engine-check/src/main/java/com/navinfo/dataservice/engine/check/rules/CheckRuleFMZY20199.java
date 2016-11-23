package com.navinfo.dataservice.engine.check.rules;
import java.util.List;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiCarrental;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.commons.util.StringUtils;

/** 
 * @ClassName: CheckRuleFMZY20199
 * @author Gao Pengrong
 * @date 2016-11-14 下午8:41:24 
 * @Description: 1.可以为空；2.字符串；3.不为空时，不能含有非法字符,且不能还有空格
					Log1：周边交通路线含有非法字符“XX”。
					Log2：周边交通路线内容含有半角字符。
					Log3：周边交通线路内容还有空格。
 */

public class CheckRuleFMZY20199 extends baseRule {
	
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
			String howToGo=poiCarrental.getHowToGo();
			
			if (StringUtils.isNotEmpty(howToGo)){
				//调用元数据请求接口
				MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
				JSONObject characterMap = metaApi.getCharacterMap();
				
				char[] howToGolist=new char[howToGo.length()];
				howToGolist=howToGo.toCharArray();
				
				String illegalChar = "";
				
				for(int i = 0; i < howToGolist.length; i++){
					
					char c=howToGolist[i];
					String str=String.valueOf(c);
					//全半角空格
					if ("　".equals(str) || " ".equals(str)){
						log = "周边交通线路内容含有空格。";
						this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
					}
					
					if (!characterMap.has(str)){
						illegalChar+=str;
					}
				}
				if (!"".equals(illegalChar)){
					log = "周边交通路线含有非法字符'"+illegalChar+"';";
					this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
				}
				if (!howToGo.equals(ExcelReader.h2f(howToGo))){
					log = "周边交通路线内容含有半角字符。";
					this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
				}
			}
			
		}
	}
}