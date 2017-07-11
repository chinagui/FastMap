package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Title: COM60038
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description: IX_POI_CONTACT(POI_PID,CONTACT,CONTACT_TYPE,CONTACT_DEPART)应唯一
 * @Author: Crayeres
 * @Date: 7/7/2017
 * @Version: V1.0
 */
public class COM60038 extends BasicCheckRule {
    @Override
    public void runCheck(BasicObj obj) throws Exception {
        if (obj.objName().equals(ObjectName.IX_POI)) {
            IxPoiObj  poiObj = (IxPoiObj) obj;
            IxPoi poi = (IxPoi) poiObj.getMainrow();

            List<IxPoiContact> contacts = poiObj.getIxPoiContacts();
            Set<String> sets = new HashSet<>();
            for (IxPoiContact contact : contacts) {
                sets.add(contact.getPoiPid() + contact.getContact() + contact.getContactType() + contact.getContactDepart());
            }
            if (contacts.size() != sets.size()) {
                setCheckResult(poi.getGeometry(), String.format("[IX_POI,%s]", poi.getPid()), poi.getMeshId());
            }
        }
    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
    }
}
