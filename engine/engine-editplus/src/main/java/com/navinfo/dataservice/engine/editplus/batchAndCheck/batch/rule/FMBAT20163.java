package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;


/**
 * 
 * 查询条件：该POI发生变更(新增或修改主子表)
 * 批处理：当名称中存在名称为空（NAME）的官方标准化英文名称（LANG_CODE="ENG",NAME_TYPE=1,NAME_CLASS=1）时，删除官方标准化英文名称，生成删除履
 * 当英文原始官方名称记录NAME字段小于等于35个字符的记录，且存在标准化官方英文名时，删除标准化官方英文，生成删除履历，U_RECORD=2；
 *
 */
public class FMBAT20163 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		List<IxPoiName> names = poiObj.getIxPoiNames();
		boolean hasOfficialName = false;
		boolean hasStandardName = false;
		IxPoiName standardName = null;
		for (IxPoiName name:names) {
			if (name.getNameClass()==1 && name.getNameType()==2 && name.getName().length()<=35 && name.getLangCode().equals("ENG")) {
				hasOfficialName = true;
			} else if (name.getNameClass()==1 && name.getNameType()==1 && name.getLangCode().equals("ENG")) {
				if (name.getName()==null || name.getName().isEmpty()) {
					poiObj.deleteSubrow(name);
				} else {
					standardName = name;
					hasStandardName = true;
				}
			}
		}
		
		if (hasOfficialName && hasStandardName) {
			poiObj.deleteSubrow(standardName);
		}

	}

}
