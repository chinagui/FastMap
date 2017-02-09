package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiCarrental;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

import net.sf.json.JSONObject;

/**
 * 检查条件： 非删除（根据履历判断删除） 检查原则：
 * 1.不能含有非法字符（如果值不在TY_CHARACTER_EGALCHAR_EXT.EXTENTION_TYPE in
 * (“GBK”,“ENG_F_U”,“ENG_F_L”,“DIGIT_F”,“SYMBOL_F”)对应的“CHARACTER”范围内） 2.不能含有空格
 * 3.字段应该是全角
 * 
 * log1：**是非法字符 log2：周边交通线路内容含有空格 Log3：周边交通线路还有半角字符
 * 
 * @author gaopengrong
 */
public class FMZY20199 extends BasicCheckRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}

		List<IxPoiCarrental> carrentals = poiObj.getIxPoiCarrentals();
		
		// 调用元数据请求接口
		MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		JSONObject characterMap = metaApi.getCharacterMap();

		for (IxPoiCarrental poiCarrental : carrentals) {
			String howToGo = poiCarrental.getHowToGo();

			if (StringUtils.isEmpty(howToGo)) {
				continue;
			}

			String illegalChar = "";
			for (char c : howToGo.toCharArray()) {
				if (!characterMap.has(String.valueOf(c))) {
					illegalChar += c;
				} else if (characterMap.has(String.valueOf(c))) {
					String type = characterMap.getString(String.valueOf(c));
					if (!type.equals("GBK") && !type.equals("ENG_F_U") && !type.equals("ENG_F_L")
							&& !type.equals("DIGIT_F") && !type.equals("SYMBOL_F")) {
						illegalChar += c;
					}
				}

			}
			if (!"".equals(illegalChar)) {
				setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
						illegalChar + "是非法字符 ");
			}

			if (howToGo.indexOf(" ") >= 0) {
				setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(), "周边交通线路内容含有空格 ");
			}

			if (!howToGo.equals(ExcelReader.h2f(howToGo))) {
				setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(), "周边交通线路还有半角字符");
			}
		}

	}

}
