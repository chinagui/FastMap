package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 检查条件：
 * 非删除POI对象
 * 检查原则：
 * 中文地址为空(记录或fullname)时，则报log：PID=**地址为空
 * Created by ly on 2017/7/7.
 */
public class FMYW20140 extends BasicCheckRule {
    @Override
    public void runCheck(BasicObj obj) throws Exception {

        if (!obj.objName().equals(ObjectName.IX_POI)) {

            return;
        }

        IxPoiObj poiObj = (IxPoiObj) obj;

        List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();

        if (addresses == null || addresses.size() == 0) {

            IxPoi poi = (IxPoi) poiObj.getMainrow();

            String strLog = "PID=" + poi.getPid() + "地址为空";

            setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), strLog);

            return;
        }

        String fullName = null;

        for (IxPoiAddress address : addresses) {

            String langCode = address.getLangCode() == null ? "" : address.getLangCode();

            if (!langCode.equals("CHI") && !langCode.equals("CHT")) {

                continue;
            }

            fullName = address.getFullname();

            if (fullName == null || fullName.isEmpty()) {

                fullName = null;

                break;
            }
        }

        if (fullName == null) {

            IxPoi poi = (IxPoi) poiObj.getMainrow();

            String strLog = "PID=" + poi.getPid() + "地址为空";

            setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), strLog);
        }
    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {

    }
}
