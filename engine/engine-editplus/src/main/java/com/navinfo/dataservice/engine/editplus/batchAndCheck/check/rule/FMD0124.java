package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
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
 * FM-D01-24  官方标准中文繁体字检查  D
 *检查条件：
 *非删除POI对象
 *检查原则：
 *官方标准化中文名称不能有繁体字。查找的繁体字在TY_CHARACTER_FJT_HZ中所在行的CONVERT字段的值：
 *1、如果是1表示不转化，不用报log；
 *2、如果是0表示需要确认后转化，报log：**是繁体字，对应的简体字是**，需确认是否转化；
 *检查名称：标准化中文名称（name_type=1，name_class={1}，lang_code=CHI)
 * @author gaopengrong
 */
public class FMD0124 extends BasicCheckRule {
	MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
	Map<String, JSONObject> ft = new HashMap<>();
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		ft = metadataApi.tyCharacterFjtHzCheckSelectorGetFtExtentionTypeMap();
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow(); 
			IxPoiName name=poiObj.getOfficeStandardCHIName();
			if(name==null){return;}
			String nameStr= name.getName();
			if(nameStr.isEmpty()){return;}
			for (int i = 0; i < nameStr.length(); i++) {
				char  item =nameStr.charAt(i);
				String str=String.valueOf(item);
				if(ft.containsKey(str)){
					JSONObject data= ft.get(str);
					Object convert= data.get("convert");
					if(convert.equals(0)){
						String jt=(String) data.get("jt");
						String log="“"+str+"”是繁体字，对应的简体字是“"+jt+"”，需确认是否转化";
						setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
					}
				}
		    }
			
		}
	}
	
}
