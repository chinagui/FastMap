package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
/**
 * GLM60378
 * 检查对象：
 * IX_POI表中“STATE(状态)”非“1（删除）”的POI
 * 检查原则：
 * 同一条POI存在多个电话时，多个电话对应的区号应一致，否则报log
 * @author zhangxiaoyi
 */
public class GLM60378 extends BasicCheckRule {
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();			
			List<IxPoiContact> contactList = poiObj.getIxPoiContacts();
			if(contactList==null||contactList.isEmpty()){return;}
			Set<String> contactSet=new HashSet<String>();
			for(IxPoiContact contactObj:contactList){
				if(contactObj.getContactType()!=1){continue;}
				String contactStr=contactObj.getContact();
				if(!contactStr.contains("-")){continue;}
				contactSet.add(contactStr.split("-")[0]);
			}
			if(contactSet.size()>1){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {}

}
