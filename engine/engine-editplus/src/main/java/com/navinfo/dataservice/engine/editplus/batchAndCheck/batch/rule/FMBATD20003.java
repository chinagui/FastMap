package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * 批处理对象：日编提交的POI数据（新增、变更、鲜度验证）；
 * 批处理原则：若IX_POI表中OPEN_24H为0，则批处理赋值2，并追加履历，若OPEN_24H不为0，则不处理
 * 
 * @author gaopengrong
 *
 */
public class FMBATD20003 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			int open24h = poi.getOpen24h();
			if (open24h==0) {
				poi.setOpen24h(2);
			}
		}
	}

}
