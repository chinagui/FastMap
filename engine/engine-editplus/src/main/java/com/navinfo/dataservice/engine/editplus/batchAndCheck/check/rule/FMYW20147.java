package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
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
 * 检查条件：
 * 该POI发生变更(新增或修改主子表、删除子表)
 * 检查原则：
 * NAME（LANG_CODE="CHI")，不能有繁体字。查找的繁体字在TY_CHARACTER_FJT_HZ中所在行的CONVERT字段的值：
 * 1、如果是2表示必须转化，报log：**是繁体字，对应的简体字是**，必须转化
 * 检查名称：标准化中文名称（type=1，class=5，langCode=CHI）
 * @author gaopengrong
 */
public class FMYW20147 extends BasicCheckRule {
	
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
			List<IxPoiName> names=poiObj.getShortStandardCHIName();
			if(names.size()==0){return;}
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Map<String, JSONObject> ft = metadataApi.tyCharacterFjtHzCheckSelectorGetFtExtentionTypeMap();
			for(IxPoiName name:names){
				String nameStr= name.getName();
				if(nameStr.isEmpty()){continue;}
				for (int i = 0; i < nameStr.length(); i++) {
					char  item =nameStr.charAt(i);
					String str=String.valueOf(item);
					if(ft.containsKey(str)){
						JSONObject data= ft.get(str);
						Object convert= data.get("convert");
						if(convert.equals(2)){
							String jt=(String) data.get("jt");
							String log="“"+str+"”是繁体字，对应的简体字是“"+jt+"”，必须转化";
							setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),log);
						}
					}
			    }
			}

		}
	}
	
}
