package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;


/**
 * 查询条件：本次日编存在IX_POI_ADDRESS新增或者修改履历，且kindCode不在重要分类表中
 * 批处理:IX_POI_ADDRESS存在LANG_CODE=“ENG”记录，则标识删除，生成批处理履历,IX_POI_ADDRESS.U_RECORD赋值2；否则不处理；
 * 
 * @author wangdongbin
 */
public class FMBAT20205 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		List<IxPoiAddress> address=poiObj.getIxPoiAddresses();
		boolean isChanged = false;
		for (IxPoiAddress poiAddress:address) {
			if (poiAddress.getHisOpType().equals(OperationType.INSERT) || poiAddress.getHisOpType().equals(OperationType.UPDATE)) {
				isChanged = true;
				break;
			}
		}
		if (isChanged) {
			String kindCode = poi.getKindCode();
			String chain = poi.getChain();
			MetadataApi metadata = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			if (!metadata.judgeScPointKind(kindCode, chain)) {
				for (IxPoiAddress poiAddress:address) {
					if (poiAddress.getLangCode().equals("ENG")) {
						poiObj.deleteSubrow(poiAddress);
					}
				}
			}
			
		}
		
	}

}
