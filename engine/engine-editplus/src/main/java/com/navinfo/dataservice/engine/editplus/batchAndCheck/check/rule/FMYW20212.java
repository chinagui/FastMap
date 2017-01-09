package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/** 
* @ClassName: FMYW20212 
* @author: zhangpengpeng 
* @date: 2017年1月6日
* @Desc: FMYW20212.java
* 检查条件：
	   以下条件全部满足时，进行检查：
	  （1）该POI发生变更(新增或修改主子表、删除子表)；
	  （2）KIND_CODE在重要分类表中；
	  （3）存在IX_POI_ADDRESS记录；
检查原则：
	  （1）数据中无LANG_CODE=“ENG”的地址组；
	  （2）数据中存在LANG_CODE=“ENG”的地址组，但FULLANME字段为空；
	   以上原则满足其一，即报log。
   log描述：重要分类POI必须存在英文地址
*/
public class FMYW20212 extends BasicCheckRule{
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			List<IxPoiAddress> addresses= poiObj.getIxPoiAddresses();
			MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			if (!metadataApi.judgeScPointKind(poi.getKindCode(), poi.getChain())){
				return;
			}
			if (addresses == null || addresses.size() == 0){return;}
			boolean hasEngAddr = false;
			for (IxPoiAddress address: addresses){
				if (address.isEng()){
					hasEngAddr = true;
					String fullName = address.getFullname();
					if (StringUtils.isEmpty(fullName)){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
					}
				}
			}
			if (!hasEngAddr){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
			}
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
