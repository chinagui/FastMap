package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * GLM60029 IX_POI_CONTACT表中关联到同一个poi的多条记录的TELE_NUMBER不能相同
 * 
 * @author zhangxiaoyi
 */
public class GLM60029 extends BasicCheckRule {
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			List<IxPoiContact> contactList = poiObj.getIxPoiContacts();
			if (contactList == null || contactList.isEmpty()) {
				return;
			}
			// 判断电话子表有没有新增和修改的履历,且电话不为空
			boolean flag = false;
			for (IxPoiContact contact : contactList) {
				String tele = contact.getContact();
				if ((contact.getHisOpType().equals(OperationType.INSERT)
						|| contact.getHisOpType().equals(OperationType.UPDATE)) && StringUtils.isNotEmpty(tele)) {
					flag = true;
				}
			}
			if (!flag) {return;}
			Set<String> contactType1Set = new HashSet<String>();
			List<String> contactType1 = new ArrayList<String>();
			Set<String> contactType2Set = new HashSet<String>();
			List<String> contactType2 = new ArrayList<String>();
			for (IxPoiContact contactObj : contactList) {
				int type = contactObj.getContactType();
				String contact = ExcelReader.f2h(contactObj.getContact());
				if(1 == type){
					contactType1Set.add(contact);
					contactType1.add(contact);
				}
				if(2 == type){
					contactType2Set.add(contact);
					contactType2.add(contact);
				}
			}
			if (contactType1Set.size() != contactType1.size() || contactType2Set.size() != contactType2.size()) {
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
	}

}
