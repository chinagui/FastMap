package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * 检查条件：
 * 以下条件其中之一满足时，需要进行检查：
 * (1)存在官方中文地址IX_POI_ADDRESS新增且FULLNAME不为空； 
 * (2)存在官方中文地址IX_POI_ADDRESS修改且FULLNAME不为空；
 * 检查原则：
 * 前缀内容字段长度大于3时，报log：前缀内容长度大于3
 * @author gaopengrong
 *
 */
public class FMYW20080 extends BasicCheckRule {
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiAddress> addrs = poiObj.getIxPoiAddresses();
			if(addrs.size()==0){return;}
			for(IxPoiAddress addr:addrs){
				if(addr.getLangCode().equals("CHI")){
					String fullname = addr.getFullname();
					if(fullname==null||fullname.isEmpty()){continue;}
					String prefix = addr.getPrefix();
					if(prefix==null||prefix.isEmpty()){continue;}
					if(prefix.length()>3){setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "前缀内容长度大于3");}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
