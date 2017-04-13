package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 检查条件：
 * 非删除POI对象
 * 检查原则：
 * IX_POI_ADDRESS表中，街巷名（street）+前缀（prefix）+门牌号（housenum）+类型（type）+子号（subnum）+后缀（surfix）+
 * 附属设施(estab)为空，且楼栋号（building）+楼门（unit）+楼层（floor）+房间号（room）不为空，报LOG：街巷名至附属设施字段拆分错误！
 * 
 */
public class GLM60481 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		IxPoiAddress address = poiObj.getCHAddress();
		if (address == null) {
			return;
		}
		int len1=getLength(address.getStreet())+getLength(address.getPrefix())+getLength(address.getHousenum())+
				getLength(address.getType())+getLength(address.getSubnum())+getLength(address.getSurfix())+
				getLength(address.getEstab())
				+getLength(address.getAddons());
		int len2=getLength(address.getBuilding())+getLength(address.getFloor())+getLength(address.getUnit())+
				getLength(address.getRoom());
		
		if(len1==0&&len2>0){
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),"街巷名至附属设施字段拆分错误！");
		}
	}
	
	private int getLength(String str) {
		if (str == null) {
			return 0;
		} else {
			return str.length();
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
