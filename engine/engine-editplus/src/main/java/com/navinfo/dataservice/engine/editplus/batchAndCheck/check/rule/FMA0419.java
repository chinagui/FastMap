package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-A04-19	中文名称拼音非法字符检查	DHM	
 * 检查条件：
 *     以下条件其中之一满足时，需要进行检查：
 *     (1) 存在IX_POI_NAME新增；
 *     (2) 存在IX_POI_NAME修改；
 *     检查原则： 
 *     POI拼音允许存在半角空格以及“TY_CHARACTER_EGALCHAR_EXT”表，“EXTENTION_TYPE”字段中，
 *     “ENG_H_U”、“ENG_H_L”、“DIGIT_H”、“SYMBOL_H”类型对应的“CHARACTER”字段的内容，
 *     和“EXTENTION_TYPE”字段里“SYMBOL_F”类型，在全半角对照关系表中（TY_CHARACTER_FULL2HALF表）
 *     FULL_WIDTH字段一致，找到FULL_WIDTH字段对应的半角“HALF_WIDTH”
 *     （如果“HALF_WIDTH”字段对应的半角字符为空，则FULL_WIDTH字段对应的全角字符也是拼音的合法字符）
 *     的字符，如果存在以外的POI，全部报出。
 *     提示：中文名称拼音非法字符检查：POI拼音中含有非法字符“xx”
 *     检查名称：标准化中文名称（NAME_TYPE=1，NAME_CLASS={1,5}，langCode=CHI或CHT）
 * @author zhangxiaoyi
 *
 */
public class FMA0419 extends BasicCheckRule {
	private MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			List<String> errorList=new ArrayList<String>();
			for(IxPoiName nameTmp:names){
				if(nameTmp.isCH()&&nameTmp.getNameType()==1
						&&(nameTmp.getNameClass()==1 ||nameTmp.getNameClass()==5)&&isCheck(nameTmp)){
					String nameStr=nameTmp.getNamePhonetic();
					if(nameStr==null||nameStr.isEmpty()){continue;}
					List<String> halfCharList=metadataApi.halfCharList();
					for(char nameSub:nameStr.toCharArray()){
						String nameSubStr=String.valueOf(nameSub);
						if(!halfCharList.contains(nameSubStr)){
							errorList.add(nameSubStr);}
					}
				}
			}
			if(errorList.size()>0){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "POI拼音中含有非法字符“"
						+errorList.toString().replace("[", "").replace("]", "")+"”");
				return;
			}
		}
	}
	
	private boolean isCheck(IxPoiName poiName){
		if(poiName.getHisOpType().equals(OperationType.INSERT)){
			return true;
		}
		if(poiName.getHisOpType().equals(OperationType.UPDATE) && poiName.hisOldValueContains(IxPoiName.NAME)){
			String oldNameStr=(String) poiName.getHisOldValue(IxPoiName.NAME);
			String newNameStr=poiName.getName();
			if(!newNameStr.equals(oldNameStr)){return true;}
		}
		return false;
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
