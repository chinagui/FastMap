package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 查询条件：该POI发生变更(新增或修改主子表)且KIND_CODE在重要分类表中
 * 批处理：
	(1)当FULLNAME字段修改且不为空，且存在对应的英文地址时，更新英文地址（LANG_CODE=""ENG""）生成履历；
	(2)当FULLNAME字段修改且不为空，但没有英文地址时，增加英文地址（LANG_CODE=""ENG""）生成履历，；
	(3)当FULLNAME字段不为空且没有修改，但没有英文地址（LANG_CODE=""ENG""）时，增加英文地址（LANG_CODE=""ENG""）生成履历，
	(4)需要翻译的中文地址：将中文地址拆分后的15个字段按照“附加信息、房间号、楼层、楼门号、楼栋号、附属设施名、后缀、子号、类型名、门牌号、前缀、标志物名、街巷名、地名小区名、乡镇街道办”进行合并;
	(5)合并后的中文地址，进行分词时，应将人工拆分的中文地址作为一级分词结果;
	(6)在(5)的基础上对一级分词的结果里的房间号（ROOM）、楼层（FLOOR）、楼门号（UNIT）、楼栋号（BUILDING）等四个词，进行二级分词后采用再次倒序翻译，即“5层”翻译为“Floor 5”而不是“5 Floor”；
	(7)当子号为“－”开头时，按“门牌号+子号”的顺序翻译。如果子号有关键字，需要将“子号”的关键字提到门牌号前面，按照“类型名+门牌号+子号”顺序翻译。如果翻译后，出现“No.No.”时，去掉多余的“No.”；如果翻译后，出现“No.no.”时，去掉多余的“no.”;
	(8)当子号为“－”开头时，按“门牌号+子号”的顺序翻译。如果子号有关键字，需要将“子号”的关键字提到门牌号前面，按照“类型名+门牌号+子号”顺序翻译。如果翻译后，出现“No.No.”时，去掉多余的“No.”；如果翻译后，出现“No.no.”时，去掉多余的“no.”;
	(9)当子号不是“－”开头时，按照“子号+类型名+门牌号”的顺序翻译。如果子号有关键字，需要将“子号”的关键字提到子号前面;
	(10)首字母大写的原则，可避免关键词库中大小问题，如“DAZHONG ELECTRONICS”、”town”、“village”
	(11)“No.”中点后如果有空格，应去掉空格,当没有对应词库时，翻译的多个拼音之间没有空格;
 *
 */
public class FMBAT20125 extends BasicBatchRule {

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
			List<IxPoiAddress> address=poiObj.getIxPoiAddresses();
			IxPoiAddress CHIaddress = null;
			IxPoiAddress ENGaddress = null;
			for (IxPoiAddress add:address) {
				if (add.getLangCode().equals("CHI")) {
					CHIaddress = add;
					break;
				}
			}
			if (CHIaddress != null) {
				for (IxPoiAddress add:address) {
					if (add.getLangCode().equals("ENG") && add.getNameGroupid()==CHIaddress.getNameGroupid()) {
						ENGaddress = add;
						break;
					}
				}
				
				if (CHIaddress.getFullname() != null && !CHIaddress.getFullname().isEmpty()) {
					if (CHIaddress.getOldValues().containsKey("fullname")) {
						if (ENGaddress != null) {
							ENGaddress.setFullname(metadata.convertEng(CHIaddress.getFullname()));
						} else {
							IxPoiAddress newEngAdd = poiObj.createIxPoiAddress();
							newEngAdd.setPoiPid(CHIaddress.getPoiPid());
							newEngAdd.setFullname(metadata.convertEng(CHIaddress.getFullname()));
							newEngAdd.setNameGroupid(CHIaddress.getNameGroupid());
							newEngAdd.setLangCode("ENG");
						}
					} else {
						if (ENGaddress == null) {
							IxPoiAddress newEngAdd = poiObj.createIxPoiAddress();
							newEngAdd.setPoiPid(CHIaddress.getPoiPid());
							newEngAdd.setFullname(metadata.convertEng(CHIaddress.getFullname()));
							newEngAdd.setNameGroupid(CHIaddress.getNameGroupid());
							newEngAdd.setLangCode("ENG");
						}
					}
				}
			}
		}
	}

}
