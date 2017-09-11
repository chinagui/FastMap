package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
 * FM-D01-41	POI官方标准中文名称全部为符号检查			DHM	
	 检查条件：
	 非删除POI对象 
	检查原则：
	官方标准化中文名称中，除了全角合法特殊字符（通用元数据库ty_character_egalchar_ext中extention_type 为SYMBOL_F 的记录）外，没有其他字符即全是字符，则报log：名称全部是字符，需要删除设施！
	name_type=1，name_class=1，lang_code=CHI或CHT
 * @author sunjiawei
 *
 */
public class FMD0141 extends BasicCheckRule {
	private MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
	private Map<String, List<String>> map = new HashMap<>();

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			List<String> errorList=new ArrayList<String>();
			for(IxPoiName nameTmp:names){
				if(nameTmp.isCH()&&nameTmp.getNameType()==1
						&&nameTmp.getNameClass()==1){
					String nameStr=nameTmp.getName();
					if(nameStr==null||nameStr.isEmpty()){continue;}
					List<String> list = (List<String>) map.get("SYMBOL_F");
					for(char nameSub:nameStr.toCharArray()){
						String nameSubStr=String.valueOf(nameSub);
						if(list.contains(nameSubStr)){
							errorList.add(nameSubStr);
						}
					}
					if(nameStr.toCharArray().length==errorList.size()){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
						return;
					}
					
				}
			}
			
		}
	}
	

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		map  = metadataApi.tyCharacterEgalcharExtGetExtentionTypeMap();
	}

}
