package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.commons.util.StringUtils;

import net.sf.json.JSONObject;

/**
 *1.可以为空；2.字符串；3.不为空时，不能含有非法字符。Log：停车场开放时间含有非法字符“XX”。
 * @author gaopengrong
 */
public class FMZY20152 extends BasicCheckRule {
	
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
			String log;
			for(IxPoiParking parking : parkings){
				String openTiime=parking.getOpenTiime();
				
				if (StringUtils.isNotEmpty(openTiime)){
					
					//调用元数据请求接口
					MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
					JSONObject characterMap = metaApi.getCharacterMap();
					
					char[] openTiimeList=new char[openTiime.length()];
					openTiimeList=openTiime.toCharArray();
					
					String illegalChar = "";
					
					for(int i = 0; i < openTiimeList.length; i++){
						
						char c=openTiimeList[i];
						String str = String.valueOf(c);
						if (!(characterMap.has(str))){
							illegalChar+=str;
						}
					if (!"".equals(illegalChar)){
						log = "停车场开放时间含有非法字符'"+illegalChar+"';";
						setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
					}
					}
				}
				
			}

		}
	}
	
}
