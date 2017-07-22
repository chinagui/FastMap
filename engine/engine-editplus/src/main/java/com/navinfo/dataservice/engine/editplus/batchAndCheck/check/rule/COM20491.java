package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

import java.util.Collection;
import java.util.Map;

/**
 * @Title: COM20491
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description: IX_POI中，字段KIND_CODE的值必须存在于SC_POINT_POICODE_NEW(KIND_CODE)中
 * @Author: Crayeres
 * @Date: 7/7/2017
 * @Version: V1.0
 */
public class COM20491 extends BasicCheckRule {
    @Override
    public void runCheck(BasicObj obj) throws Exception {
        if (obj.objName().equals(ObjectName.IX_POI)) {
            IxPoiObj poiObj = (IxPoiObj) obj;
            IxPoi poi = (IxPoi) poiObj.getMainrow();
            String kindCode = poi.getKindCode();
            if (StringUtils.isEmpty(kindCode)) {
                return;
            }

            MetadataApi metadata = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
            Map<String, String> kindCodeMap = metadata.getKindNameByKindCode();
            if (!kindCodeMap.keySet().contains(kindCode)) {
                setCheckResult(poi.getGeometry(), String.format("[IX_POI,%s]", poi.getPid()), poi.getMeshId());
            }
        }
    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
    }
}
