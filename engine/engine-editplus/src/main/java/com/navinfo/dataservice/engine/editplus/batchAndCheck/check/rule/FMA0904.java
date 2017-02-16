package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * 检查条件：
   以下条件其中之一满足时，需要进行检查：
	(1)存在IX_POI_ADDRESS新增且FULLNAME不为空； 
	(2)存在IX_POI_ADDRESS修改且FULLNAME不为空；
	检查原则：
	“PLACE（地名小区名）”字段非空，并且以“地址检查地址拆分用检查配置表(SC_POINT_ADDRCK)”配置表中TYPE=9且HM_FLAG=’M’对应的关键字（PRE_KEY）结尾的报出来。
	提示：地名小区名检查：地名小区名字段不允许以xxx（PRE_KEY）关键字结尾；
 *
 */
public class FMA0904 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			//存在IxPoiAddress新增或者修改履历
			IxPoiAddress address=poiObj.getCHAddress();
			if (address == null) {
				return;
			}
			if(!address.getHisOpType().equals(OperationType.INSERT)&&!address.getHisOpType().equals(OperationType.UPDATE)){
				return;
			}
			if (address.getFullname() == null || address.getFullname().isEmpty()) {
				return;
			}
			String place = address.getPlace();
			if (place == null || place.isEmpty()) {
				return;
			}
			MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			List<String> addrck = metaApi.getAddrck(9, "M");
			String error = "";
			if (addrck.contains(place.substring(place.length()-1))) {
				error = place.substring(place.length()-1);
			}
			if (!error.isEmpty()) {
				setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),"地名小区名检查：地名小区名字段不允许以"+error+"关键字结尾；");
			}
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
