package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * 查询条件： (1)别名原始英文(name_class=3,name_type=2,lang_code='ENG')小于等于35且存在别名标准化英文(
 * name_class=3,name_type=1,lang_code='ENG')；
 * (2)别名原始英文(name_class=3,name_type=2,lang_code='ENG')大于35且为重要分类(
 * 参考SC_POINT_SPEC_KINDCODE_NEW中TYPE=8) 批处理： 满足条件(1)时，删除别名标准英文记录；
 * 满足条件(2)时，如果该组存在别名标准化英文，则更新别名标准英文name值；如果不存在别名标准英文记录，则需要新增一条记录(name_id申请赋值，
 * name_groupid与同组中文别名一致，lang_code与同组中文别名一致，name_class=3，name_type=1)
 * 
 * @author jch
 */
public class FMBATM0102 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi mainPoi = (IxPoi) poiObj.getMainrow();
			// 查询别名原始英文列表
			List<IxPoiName> brList = poiObj.getOriginAliasENGNameList();
			for (IxPoiName br : brList) {
				if (br.getOpType().equals(OperationType.DELETE)) {
					continue;
				}
				IxPoiName standardAliasEngName = poiObj.getStandardAliasENGName(br.getNameGroupid());
				if ((br.getName()).length() <= 35 && standardAliasEngName != null) {
					poiObj.deleteSubrow(standardAliasEngName);
				}
				MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
				Map<String, String> typeMap8 = metadataApi.scPointSpecKindCodeType8();
				IxPoiName aliasCHIName = poiObj.getAliasCHIName(br.getNameGroupid());
				String aliasCHINameStr = "";
				if(aliasCHIName!=null){aliasCHINameStr=aliasCHIName.getName();}
				if (((br.getName()).length() > 35) && typeMap8.containsKey(mainPoi.getKindCode())) {
					if (standardAliasEngName != null&&!aliasCHINameStr.isEmpty()) {
						standardAliasEngName.setName(metadataApi.convertEng(aliasCHIName.getName()));
					} else {
						IxPoiName poiName = (IxPoiName) poiObj.createIxPoiName();
						poiName.setNameGroupid(br.getNameGroupid());
						poiName.setLangCode(br.getLangCode());
						poiName.setNameClass(3);
						poiName.setNameType(2);
						poiName.setName(aliasCHIName.getName());
					}
				}
			}

		}
	}

}
