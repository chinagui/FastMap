package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 查询条件：如果POI为重要分类POI(重要分类见【备注】sheet页)，英文别名原始英文名称字段大于35个字符的记录，
 * 且未制作别名标准化英文名称的记录或别名标准化英文名称为空时：
 * 
 * 将别名原始英文名称name中单词（前后存在空格的作为一个单词，首尾单词只需要判断一侧）从右往左在元数据库SC_ENGSHORT_LIST中与full_name字段匹配，
 * 如果存在，将其用Short_name替换。如果替换后，长度小于等于35个字符，当别名标准化英文名name为空时，将替换的结果更新到别名标准化英文名中；
 * 当没有别名标准化英文名时，则在ix_poi_name中新增一条记录；如果从右往左替换后仍超过35个字符，则不用批处理。
 * 
 * @author wangdongbin
 *
 */
public class FMBATM0106 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		String kindCode = poi.getKindCode();
		String chain = poi.getChain();
		MetadataApi metadata = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		if (metadata.judgeScPointKind(kindCode, chain)) {
			IxPoiName standarEngName = null;
			List<IxPoiName> aliasOriEngNameList = poiObj.getOriginAliasENGNameList();
			Map<String,String> engshortList = metadata.scEngshortListMap();
			for (IxPoiName aliasOriEngName:aliasOriEngNameList) {
				if (aliasOriEngName != null) {
					String aliasOriNameStr = aliasOriEngName.getName();
					if (aliasOriNameStr == null) {
						return;
					}
					standarEngName = poiObj.getOfficeStandardEngName(aliasOriEngName.getNameGroupid());
					if (aliasOriNameStr.length()>35) {
						transName(standarEngName,aliasOriNameStr,poiObj,aliasOriEngName,engshortList);
					}
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
	private void transName(IxPoiName standardName,String aliasOriNameStr,IxPoiObj poiObj,IxPoiName aliasOriEngName,Map<String,String> engshortList) throws Exception {
		String[] officialNameStrList = aliasOriNameStr.split(" ");
		int strLength = officialNameStrList.length;
		boolean hasShort = false;
		for (int i=1;i<=officialNameStrList.length;i++) {
			String fullName = officialNameStrList[strLength-i];
			if (engshortList.containsKey(fullName)) {
				String shortName = engshortList.get(fullName);
				aliasOriNameStr = aliasOriNameStr.replace(fullName, shortName);
				if (aliasOriNameStr.length()<=35) {
					hasShort = true;
					break;
				}
			}
		}
		if (hasShort) {
			if (standardName == null) {
				IxPoiName poiName = poiObj.createIxPoiName();
				poiName.setNameGroupid(aliasOriEngName.getNameGroupid());
				poiName.setLangCode(aliasOriEngName.getLangCode());
				poiName.setNameClass(3);
				poiName.setNameType(1);
				poiName.setName(aliasOriNameStr);
			} else {
				if (StringUtils.isEmpty(standardName.getName())) {
					standardName.setName(aliasOriNameStr);
				}
			}
		}
	}

}
