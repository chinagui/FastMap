package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
/**
 * FM-A04-13	POI中文名称非法字符检查	DHM	
 * 检查条件：
 *  该POI发生变更(新增或修改主子表、删除子表)
 *  检查原则：
 *  标准化中文名称允许存在“TY_CHARACTER_EGALCHAR_EXT”表，“EXTENTION_TYPE”字段中“GBK”、“ENG_F_U”、“ENG_F_L”、
 *  “DIGIT_F”、“SYMBOL_F”类型对应的“CHARACTER”字段的内容，如果存在其他字符，将此条POI报出。
 *  提示：POI中文名称非法字符检查：POI中文名中含有非法字符“xx”
 *  检查名称：标准化中文名称（NAME_TYPE=1，class={1,5}，LANG_CODE=CHI或CHT）
 * @author zhangxiaoyi
 *
 */
public class FMA0413 extends BasicCheckRule {
	MetadataApi api=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<String> errorList=new ArrayList<String>();
			Map<String, List<String>> map = api.tyCharacterEgalcharExtGetExtentionTypeMap();
			for(IxPoiName nameTmp:poiObj.getIxPoiNames()){
				if(nameTmp.isCH()&&nameTmp.getNameType()==1&&(nameTmp.getNameClass()==1||nameTmp.getNameClass()==5)){
					String name=nameTmp.getName();
					for(char nameSub:name.toCharArray()){
						String nameSubStr=String.valueOf(nameSub);
						if(!map.get("GBK").contains(nameSubStr)&&!map.get("ENG_F_U").contains(nameSubStr)
								&&!map.get("ENG_F_L").contains(nameSubStr)&&!map.get("DIGIT_F").contains(nameSubStr)
								&&!map.get("SYMBOL_F").contains(nameSubStr)){
							errorList.add(nameSubStr);
						}
					}
				}
			}
			if(errorList.size()>0){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "POI中文名中含有非法字符“"
						+errorList.toString().replace("[", "").replace("]", "")+"”");
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
