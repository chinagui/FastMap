package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * 别名删除或新增批处理: 查询条件： (1)别名中文(name_class=3,name_type=1,lang_code='CHI')删除；
 * (2)别名中文新增或修改 批处理原则： 满足条件(1)时，如果同组存在别名英文，则需要删除(name_class=3,name_type in
 * （1,2）,lang_code='ENG')的记录
 * 满足条件(2)时，如果该组存在别名原始英文，则更新别名原始英文name值；如果没有别名原始英文，则需要新增一条记录(name_id申请赋值，
 * name_groupid与同组中文别名一致，lang_code与同组中文别名一致，name_class=3，name_type=2)
 * 
 * @author jch
 */
public class FMBATM0101 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;

			// 查询别名中文列表
			List<IxPoiName> brList = poiObj.getAliasCHIName();
			if (brList.size()!=0) {
				for (IxPoiName br : brList) {
					// 满足条件（2）时，执行对应操作
					if ((br.getHisOpType().equals(OperationType.INSERT))
							|| (br.getHisOpType().equals(OperationType.UPDATE)
									&& br.hisOldValueContains(IxPoiName.NAME))) {
						IxPoiName originEngAlias = poiObj.getOriginAliasENGName(br.getNameGroupid());
						MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
						if (originEngAlias != null) {
							originEngAlias.setName(metadataApi.convertEng(br.getName()));
						} else {
							IxPoiName poiName = (IxPoiName) poiObj.createIxPoiName();
							poiName.setNameGroupid(originEngAlias.getNameGroupid());
							poiName.setLangCode(originEngAlias.getLangCode());
							poiName.setNameClass(3);
							poiName.setNameType(2);
							poiName.setName(metadataApi.convertEng(br.getName()));
						}
					}

				}

			} else {
				// 满足条件（1）时，执行对应操作
				List<IxPoiName> brEngList = poiObj.getAliasENGName();
				for (IxPoiName br : brEngList) {
					if (br.getNameType() == 1 || br.getNameType() == 2) {
						poiObj.deleteSubrow(br);
					}
				}
			}

		}
	}

}
