package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiFlag;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;


/**
 * 
 * 查询条件：当IX_POI_NAME.LNNG_CODE="ENG",且IX_POI_FLAG.FLAG_CODE="002000080000"或"002000090000"时，英文原始官方名称记录NAME字段大于35个字符的记录，
 * 且未制作标准化官方英文名的记录或标准化官方英文名为空时。
 * 批处理：将NAME中单词（前后存在空格的作为一个单词，首尾单词只需要判断一侧）从右往左在元数据库SC_ENGSHORT_LIST中与FULL_NAME字段匹配，如果存在，
 * 将其用SHORT_NAME替换。如果替换后，长度小于等于35个字符，当标准化官方英文名为空时，将替换的结果更新到标准化官方英文名中；当没有标准化官方英文名时，
 * 则IX_POI_NAME新增一条记录，NAME_ID申请赋值，POI_PID赋值该POI的PID，NAME_GROUPID=对应中文NAME_GROUPID,LANG_CODE赋值ENG，NAME_CLASS=1，NAME_TYPE=1，
 * NAME赋值官方原始名称对应的名称，NAME_PHONETIC根据官方标准名称转拼音，如果从右往左替换后仍超过35个字符，则不用批处理。
 *
 */
public class FMBAT20135 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		List<IxPoiName> names = poiObj.getIxPoiNames();
		List<IxPoiFlag> flags = poiObj.getIxPoiFlags();
		boolean isFlag = false;
		for (IxPoiFlag flag:flags) {
			if (flag.getFlagCode().equals("002000080000") || flag.equals("002000090000")) {
				isFlag = true;
				break;
			}
		}
		if (isFlag) {
			String officialNameStr = "";
			IxPoiName officialName = null;
			IxPoiName standardName = null;
			boolean hasStandardName = false;
			for (IxPoiName name:names) {
				if (name.getNameClass()==1 && name.getNameType()==2 && name.getName().length()>35 && name.getLangCode().equals("ENG")) {
					officialName = name;
					officialNameStr = name.getName();
				} else if (name.getNameClass()==1 && name.getNameType()==1 && name.getLangCode().equals("ENG")) {
					if (name.getName().length()>0) {
						hasStandardName = true;
					} else {
						standardName = name;
					}
				}
			}
			if (!hasStandardName && officialNameStr.length()>35) {
				transName(standardName,officialNameStr,poiObj,officialName);
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
		for (int i=1;i<=officialNameStrList.length;i++) {
			String fullName = officialNameStrList[strLength-i];
			if (engshortList.containsKey(fullName)) {
				String shortName = engshortList.get(fullName);
				officialNameStr = officialNameStr.replace(fullName, shortName);
				if (officialNameStr.length()<=35) {
				break;
				}
			}
		}
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