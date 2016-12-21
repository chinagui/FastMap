package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 查询条件：满足以下任一条件均执行批处理：
 * (1)存在IX_POI_NAME新增履历；
 * (2)存在IX_POI_NAME修改履历；
 * (3)存在KIND_CODE或CHAIN的修改履历且修改前后在word_kind表中对应的词库不一样；
 * 批处理：当NAME_TYPE=1且NAME_CLASS=1时，进行如下批处理：
 * (1)当同组（NAME_GROUPID相同）名称中，没有原始英文名称（LANG_CODE="ENG",NAME_TYPE=2）时,新增一条原始英文名(组号一样，NAME_CLASS=1，NAME_TYPE=2，LANG_CODE="ENG"，NAME转拼音赋值，NAME_PHONETIC赋值空），并生成新增履历；
 * (2)当同组（NAME_GROUPID相同）名称中，有原始英文名称（LANG_CODE="ENG",NAME_TYPE=2）时,更新英文名NAME；有标准化英文名称时，清空标准化英文NAME，并生成履历；
 * NAME统一处理：统一处理No.中N和o的大小写问题：将“NO.”，“nO.”，“no.”修改成“No.”。
 * 注：标准化（type=1）中文名,当class={1,3,8,9}时，必须有原始英文名；标准化（type=1）中文名，当class<>{1,3,8,9}时，没有英文名；官方原始中文（type=2，class=1）名，没有英文名。
 *
 */
public class FMBAT20115 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		List<IxPoiName> names=poiObj.getIxPoiNames();
		MetadataApi metadata = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		boolean isChanged = false;
		for (IxPoiName name:names) {
			if (name.getHisOpType().equals(OperationType.INSERT) || name.getHisOpType().equals(OperationType.UPDATE)) {
				isChanged = true;
				break;
			}
		}
		// 存在KIND_CODE或CHAIN的修改履历且修改前后在word_kind表中对应的词库不一样；
		if (poi.hisOldValueContains(IxPoi.KIND_CODE) || poi.hisOldValueContains(IxPoi.CHAIN)) {
			String wordNew = metadata.wordKind(poi.getKindCode(), poi.getChain());
			String wordOld = metadata.wordKind((String)poi.getHisOldValue("kindCode"),(String)poi.getHisOldValue("chain"));
			if (!wordNew.equals(wordOld)) {
				isChanged = true;
			}
		}
		
		if (isChanged) {
			IxPoiName standarName = null;
			for (IxPoiName name:names) {
				if (name.getNameClass()==1 && name.getNameType()==1 && name.getLangCode().equals("CHI")) {
					standarName = name;
				}
			}
			
			if (standarName != null) {
				IxPoiName engOfficialName = null;
				IxPoiName engStandarName = null;
				for (IxPoiName name:names) {
					if (name.getNameType()==2 && name.getLangCode().equals("ENG") && standarName.getNameGroupid()==name.getNameGroupid()) {
						engOfficialName = name;
					}
					if (name.getNameType()==1 && name.getLangCode().equals("ENG") && standarName.getNameGroupid()==name.getNameGroupid()) {
						engStandarName = name;
					}
				}
				
				if (engOfficialName == null) {
					engOfficialName = poiObj.createIxPoiName();
					engOfficialName.setNameType(2);
					engOfficialName.setNameClass(1);
					engOfficialName.setPoiPid(poi.getPid());
					engOfficialName.setNameGroupid(standarName.getNameGroupid());
					engOfficialName.setLangCode("ENG");
					engOfficialName.setName(metadata.convertEng(standarName.getName()));
				} else {
					engOfficialName.setName(metadata.convertEng(standarName.getName()));
					if (engStandarName != null) {
						engStandarName.setName("");
					}
				}
				
			}
			
		}

	}

}
