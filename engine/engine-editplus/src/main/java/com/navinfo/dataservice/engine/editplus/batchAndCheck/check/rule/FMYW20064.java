package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/** 
* @ClassName: FMYW20064 
* @author: zhangpengpeng 
* @date: 2017年1月3日
* @Desc: FMYW20064.java
* 检查条件：该POI发生变更(新增或修改主子表、删除子表)；
     检查原则：英文地址（FULLNAME）中若出现括号，则
	  1、括号“（”和“）”要成对出现，否则报log1；
	  2、括号“（”和“）”中间必须有内容，否则报log2；
	  3、括号中不能再嵌套括号，否则报log3；
	log1：英文地址中括号需要成对出现
	log2：英文地址中括号中必须存在内容
	log3：英文地址中不能出现括号嵌套括号情况
	备注：此处的括号应该都是半角的
*/
public class FMYW20064 extends BasicCheckRule{
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
			List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();
			if (addresses == null || addresses.size() == 0) {
				return;
			}
			for (IxPoiAddress addrTmp : addresses) {
				if (addrTmp.isEng()) {
					String addrStr = addrTmp.getFullname();
					if (StringUtils.isEmpty(addrStr)) {
						continue;
					}
					String errorLog = CheckUtil.isRightKuohao(addrStr);
					if (StringUtils.isNotEmpty(errorLog)){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "英文地址中" + errorLog);
					}
				}
			}
		}
	}
}
