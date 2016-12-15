package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * 检查条件：
 * 以下条件其中之一满足时，需要进行检查：
 * (1)存在IX_POI_ADDRESS新增 ；
 * (2)存在IX_POI_ADDRESS修改且IX_POI_ADDRESS存在记录；
 * 检查原则：将符合检查条件的POI筛选出来。
 * 提示：地址拆分检查与标准化
 * @author gaopengrong
 */
public class FMA0901 extends BasicCheckRule {
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			if(!isCheck(poiObj)){return;}
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId());		
		}
	}

	/**
	 * 本次日编存在IX_POI_ADDRESS新增或者修改履历
	 * @param poiObj
	 * @return
	 */
	private boolean isCheck(IxPoiObj poiObj){
		IxPoi poi= (IxPoi) poiObj.getMainrow();
		List<IxPoiAddress> Addresses= poiObj.getIxPoiAddresses();
		for (IxPoiAddress Address : Addresses) {
			if(Address.getHisOpType().equals(OperationType.INSERT)){return true;}
			if (Address.getHisOpType().equals(OperationType.UPDATE) &&Address.getLangCode()=="CHI"&&Address.hisOldValueContains(IxPoiAddress.FULLNAME)){
				String oldFullname=(String) Address.getHisOldValue(IxPoiAddress.FULLNAME);
				String newFullname=Address.getFullname();
				if(!oldFullname.equals(newFullname)){
					return true;
				}
			}
		}

		return false;
	}
	
	
}
