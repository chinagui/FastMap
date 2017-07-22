package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * 	FM-D01-33		POI中文名称未制作拼音		DHM	
	检查条件：
		非删除POI对象；
	检查原则：如果POI的标准化官方中文名称拼音、标准化简称拼音、标准化别名拼音、标准化曾用名拼音、为空时，程序报出：POI中文名称官方标准化中文（标准化简称中文、标准化别名中文、标准化曾用名中文）未制作拼音
	标准化中文名称（name_type=1，name_class={1,3,5,6}，langCode=CHI或CHT）
 * @author sunjiawei
 *
 */
public class FMD0133 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			for(IxPoiName nameTmp:names){
				if(nameTmp.isCH()){
					if(nameTmp.getNameType()==1){
						String nameStr = nameTmp.getNamePhonetic();
						if(nameTmp.getNameClass()==1){
							if(StringUtils.isBlank(nameStr)){
								setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "POI中文名称官方标准化中文未制作拼音");
								return;
							}
						}else if(nameTmp.getNameClass()==3){
							if(StringUtils.isBlank(nameStr)){
								setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "POI中文名称标准化别名中文未制作拼音");
								return;
							}
						}else if(nameTmp.getNameClass()==5){
							if(StringUtils.isBlank(nameStr)){
								setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "POI中文名称标准化简称中文未制作拼音");
								return;
							}
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
