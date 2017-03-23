package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 查询条件：存在IX_POI_ADDRESS新增或者修改,且IX_POI_ADDRESS.U_RECORD!=2（删除） 
 * 批处理：
 * （1）IX_POI_ADDRESS.FULLNAME转全角； 
 * （2）FULLRNAME转拼音赋值给FULLNAME_PHONETIC；
 * （3）以上批处理生成履历；
 * 
 * @author wangdongbin
 *
 */
public class FMBAT20103 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}
		MetadataApi apiService=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		IxPoiAddress chiAddress = poiObj.getChiAddress();
		if (chiAddress.getHisOpType().equals(OperationType.INSERT) || chiAddress.getHisOpType().equals(OperationType.UPDATE)) {
			chiAddress.setFullname(ExcelReader.h2f(chiAddress.getFullname()));
			chiAddress.setFullnamePhonetic(apiService.pyConvertHz(chiAddress.getFullname()));
		} 
	}

}
