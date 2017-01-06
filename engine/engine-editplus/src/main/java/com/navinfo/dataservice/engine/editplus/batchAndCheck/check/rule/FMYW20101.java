package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/** 
* @ClassName: FMYW20101 
* @author: zhangpengpeng 
* @date: 2017年1月4日
* @Desc: FMYW20101.java
* 检查条件：
	以下条件其中之一满足时，需要进行检查：
	(1)存在官方中文地址IX_POI_ADDRESS新增且FULLNAME不为空； 
	(2)存在官方中文地址IX_POI_ADDRESS修改且FULLNAME不为空；
检查原则：
	英文地址包含" NO."、" no."、" nO."、"N0."（前三个是空格开头，最后一个是数字0），
	报log：英文地址No及N0的检查
*/
public class FMYW20101 extends BasicCheckRule{
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			// 存在IxPoiAddress新增或者修改履历
			IxPoiAddress addr = poiObj.getCHAddress();
			if (addr == null){return;}
			if(addr.getHisOpType().equals(OperationType.INSERT)||(addr.getHisOpType().equals(OperationType.UPDATE))){
				List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();
				if (addresses == null || addresses.size() == 0){return;}
				for (IxPoiAddress address: addresses){
					if (address.isEng()){
						String engAddr = address.getFullname();
						if (StringUtils.isNotEmpty(engAddr)){
							Pattern pattern = Pattern.compile(".*( NO.| no.| nO.|N0.)+.*");
							Matcher matcher = pattern.matcher(engAddr);
							if (matcher.find()){
								setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
								return;
							}
						}
					}
				}
			}
		}
	}

}
