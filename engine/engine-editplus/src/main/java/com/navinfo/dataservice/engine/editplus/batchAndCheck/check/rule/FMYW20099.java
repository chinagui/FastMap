package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/** 
* @ClassName: FMYW20099 
* @author: zhangpengpeng 
* @date: 2017年1月4日
* @Desc: FMYW20099.java
* 检查条件：
	该POI发生变更(新增或修改主子表、删除子表)；
     检查原则：
	1）中文拆分地址合并以“附”或“附近”结尾时
	2）中文拆分地址合并包含“增”时
	报log：地址中含有附增，请检查翻译结果
	备注：将中文地址拆分后的15个字段按照
“附加信息、房间号、楼层、楼门号、楼栋号、附属设施名、后缀、子号、类型名、门牌号、前缀、标志物名、街巷名、地名小区名、乡镇街道办”
进行合并
*/
public class FMYW20099 extends BasicCheckRule{
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
			if (addresses ==null || addresses.size() == 0){
				return;
			}
			for (IxPoiAddress address: addresses){
				if (address.isCH()){
					String splitAddr = address.getChiSplitAddr();
					if (StringUtils.isEmpty(splitAddr)){return;}
					if (splitAddr.endsWith("附近") || splitAddr.endsWith("附") || splitAddr.contains("增")){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
						return;
					}
				}
			}
		}
	}
}
