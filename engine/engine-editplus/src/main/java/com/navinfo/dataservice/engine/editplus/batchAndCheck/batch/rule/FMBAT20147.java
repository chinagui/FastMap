package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 
 * 查询条件：如果POI为重要分类POI(重要分类见【备注】sheet页)，英文原始官方名称name字段大于35个字符的记录，
 * 且未制作标准化官方英文名的记录或标准化官方英文名为空时：
 * 将官方原始英文名称name中单词（前后存在空格的作为一个单词，首尾单词只需要判断一侧）从右往左在元数据库SC_ENGSHORT_LIST中与full_name字段匹配，
 * 如果存在，将其用Short_name替换。如果替换后，长度小于等于35个字符，当标准化官方英文名name为空时，将替换的结果更新到标准化官方英文名中；
 * 当没有标准化官方英文名时，则在names中新增一条记录；如果从右往左替换后仍超过35个字符，则不用批处理。
 *
 */
public class FMBAT20147 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		List<IxPoiName> names = poiObj.getIxPoiNames();
		String kindCode = poi.getKindCode();
		String chain = poi.getChain();
		MetadataApi metadata = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		if (metadata.judgeScPointKind(kindCode, chain)) {
			IxPoiName officialEngName = null;
			IxPoiName standarEngName = null;
			for (IxPoiName poiName:names) {
				if (poiName.getNameClass()==1&&poiName.getNameType()==2&&poiName.getLangCode().equals("ENG")) {
					officialEngName = poiName;
				}
				if (poiName.getNameClass()==1&&poiName.getNameType()==1&&poiName.getLangCode().equals("ENG")) {
					standarEngName = poiName;
				}
			}
			if (officialEngName != null) {
				if (officialEngName.getName().length()>35) {
					transName(standarEngName,officialEngName.getName(),poiObj,officialEngName);
				}
			}
		}
	}
	
	/**
	 * 通过原始英文名转出标准英文名
	 * @param standardName
	 * @param officialName
	 * @param poiObj
   */
	private void transName(IxPoiName standardName,String officialNameStr,IxPoiObj poiObj,IxPoiName officialName) throws Exception {
		MetadataApi metadata = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		Map<String,String> engshortList = metadata.scEngshortListMap();
		String[] officialNameStrList = officialNameStr.split(" ");
		int strLength = officialNameStrList.length;
		boolean hasShort = false;
		for (int i=1;i<=officialNameStrList.length;i++) {
			String fullName = officialNameStrList[strLength-i];
			if (engshortList.containsKey(fullName)) {
				String shortName = engshortList.get(fullName);
				officialNameStr = officialNameStr.replace(fullName, shortName);
				if (officialNameStr.length()<=35) {
					hasShort = true;
					break;
				}
			}
		}
		if (hasShort) {
			if (standardName == null) {
				IxPoiName poiName = poiObj.createIxPoiName();
				poiName.setNameGroupid(officialName.getNameGroupid());
				poiName.setLangCode(officialName.getLangCode());
				poiName.setNameClass(1);
				poiName.setNameType(1);
				poiName.setName(officialNameStr);
			} else {
				standardName.setName(officialNameStr);
			}
		}
	}

}
