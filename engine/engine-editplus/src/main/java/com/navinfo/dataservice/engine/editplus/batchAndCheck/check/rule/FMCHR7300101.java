package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.Map;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-CHR73001-01	繁体字检查	D	
 * 检查条件：
 *     Lifecycle！=1（删除）
 *     检查原则：
 *     主名称（name）字段不能有繁体字。查找的繁体字在TY_CHARACTER_FJT_HZ中所在行的CONVERT字段的值：
 *     1、如果是2表示必须转化，报log：**是繁体字，对应的简体字是**，必须转化
 * @author zhangxiaoyi
 *
 */
public class FMCHR7300101 extends BasicCheckRule {
	MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			
			IxPoiName nameObj = poiObj.getOfficeOriginCHName();
			if(nameObj==null){return;}
			String nameStr = nameObj.getName();
			if(nameStr==null||nameStr.isEmpty()){return;}
			
			Map<Integer, Map<String, String>> convertFtMap = metadataApi.tyCharacterFjtHzConvertFtMap();
			Map<String, String> convert2Map = convertFtMap.get(2);
			for(String ft:convert2Map.keySet()){
				if(nameStr.contains(ft)){
					String log="“"+ft+"”是繁体字，对应的简体字是“"+convert2Map.get(ft)+"”，必须转化";
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),log);
					return;
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
