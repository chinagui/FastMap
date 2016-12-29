package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiCarrental;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

import net.sf.json.JSONObject;

/**
 * 1.可以为空；2.字符串；3.不为空时，不能含有非法字符,且不能还有空格
					Log1：周边交通路线含有非法字符“XX”。
					Log2：周边交通路线内容含有半角字符。
					Log3：周边交通线路内容还有空格。
 * @author gaopengrong
 */
public class FMZY20199 extends BasicCheckRule {
	
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
							setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
						}
						
						if (!characterMap.has(str)){
							illegalChar+=str;
						}
					}
					if (!"".equals(illegalChar)){
						log = "周边交通路线含有非法字符'"+illegalChar+"';";
						setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
					}
					if (!howToGo.equals(ExcelReader.h2f(howToGo))){
						log = "周边交通路线内容含有半角字符。";
						setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
					}
				}
			}
	
		}
	}
}
