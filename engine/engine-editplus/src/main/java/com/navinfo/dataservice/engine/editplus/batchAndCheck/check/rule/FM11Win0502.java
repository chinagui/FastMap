package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FM11Win0502
 * @author Han Shaoming
 * @date 2017年2月8日 下午6:07:27
 * @Description TODO
 * 检查条件：Lifecycle！=1（删除）
 * 检查原则：
 * 允许存在“TY_CHARACTER_EGALCHAR_EXT”表，“EXTENTION_TYPE”字段中 “GBK”、“ENG_F_U”、
 * “ENG_F_L”、“DIGIT_F”、“SYMBOL_F”类型对应的“CHARACTER”字段的内容，如果主地址（address）存在其他字符，将此条POI报出。
 * 充电桩（分类为230227）不参与检查。
 */
public class FM11Win0502 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			//充电桩（230227）不参与检查
			String kindCode = poi.getKindCode();
			if(kindCode == null || "230227".equals(kindCode)){return;}
			//存在IxPoiAddress
			IxPoiAddress ixPoiAddress=poiObj.getCHAddress();
			//错误数据
			if(ixPoiAddress==null){return;}
			String fullname = ixPoiAddress.getFullname();
			if(fullname==null){return;}
			List<String> errorList=new ArrayList<String>();
			MetadataApi api=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Map<String, List<String>> map = api.tyCharacterEgalcharExtGetExtentionTypeMap();
			for(char nameSub:fullname.toCharArray()){
				String nameSubStr=String.valueOf(nameSub);
				if(!map.get("GBK").contains(nameSubStr)&&!map.get("ENG_F_U").contains(nameSubStr)
						&&!map.get("ENG_F_L").contains(nameSubStr)&&!map.get("DIGIT_F").contains(nameSubStr)
						&&!map.get("SYMBOL_F").contains(nameSubStr)){
					errorList.add(nameSubStr);
				}
			}
			if(errorList.size()>0){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "地址中含有非法字符“"
						+errorList.toString().replace("[", "").replace("]", "")+"”");
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
