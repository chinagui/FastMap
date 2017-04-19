package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * FM-D01-21	官方标准中文繁体字检查	 D	
 * 检查条件：
 * 非删除POI对象
 * 检查原则：
 * 官方标准化中文名称不能有繁体字。查找的繁体字在TY_CHARACTER_FJT_HZ中所在行的CONVERT字段的值：
 * 1、如果是2表示必须转化，报log：**是繁体字，对应的简体字是**，必须转化
 * 检查名称：标准化中文名称（name_type=1，name_class={1}，langCode=CHI）
 * 
 * @author gaopengrong
 *
 */
public class FMD0121 extends BasicCheckRule {
	MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			for(IxPoiName nameTmp:names){
				if(nameTmp.getLangCode().equals("CHI")&&nameTmp.getNameType()==1
						&&nameTmp.getNameClass()==1){
					String name=nameTmp.getName();
					if(name==null){continue;}
					Map<Integer, Map<String, String>> convertFtMap = metadataApi.tyCharacterFjtHzConvertFtMap();
					Map<String, String> convert2Map = convertFtMap.get(2);
					for(String ft:convert2Map.keySet()){
						if(name.contains(ft)){
							String log="“"+ft+"”是繁体字，对应的简体字是“"+convert2Map.get(ft)+"”，必须转化";
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),log);
						}
					}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
