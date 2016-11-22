package com.navinfo.dataservice.engine.check.rules;
import java.util.List;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;

/** 
 * @ClassName: CheckRuleFMZY20152
 * @author Gao Pengrong
 * @date 2016-11-14 下午8:41:24 
 * @Description: 1.可以为空；2.字符串；3.不为空时，不能含有非法字符。
					Log：停车场开放时间含有非法字符“XX”。
 */

public class CheckRuleFMZY20152 extends baseRule {
	
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
		for(IRow row : rows)
		{
			IxPoiParking poiPark = (IxPoiParking) row;
			String openTiime=poiPark.getOpenTiime();
			
			if (StringUtils.isNotEmpty(openTiime)){
				
				//调用元数据请求接口
				MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
				JSONObject characterMap = metaApi.getCharacterMap();
				
				char[] openTiimeList=new char[openTiime.length()];
				openTiimeList=openTiime.toCharArray();
				
				String illegalChar = "";
				int aa = openTiimeList.length;
				
				for(int i = 0; i < aa; i++){
					
					char c=openTiimeList[i];
					String str = String.valueOf(c);
					if (!(characterMap.has(str))){
						illegalChar+=str;
					}
				if (!"".equals(illegalChar)){
					log = "停车场开放时间含有非法字符'"+illegalChar+"';";
					this.setCheckResult(ixPoi.getGeometry(), "[IX_POI,"+ixPoi.getPid()+"]", ixPoi.getMeshId(),log);
				}
				}
			}
			
		}
	}
}