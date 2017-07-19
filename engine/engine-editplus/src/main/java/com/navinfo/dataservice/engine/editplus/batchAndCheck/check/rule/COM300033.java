package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.geo.computation.GeometryTypeName;
import com.vividsolutions.jts.geom.Geometry;

import java.util.Collection;

/**
 * @Title: COM300033
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description: IX_POI ( GEOMETRY )
 * @Author: Crayeres
 * @Date: 7/10/2017
 * @Version: V1.0
 */
public class COM300033 extends BasicCheckRule{

    @Override
    public void runCheck(BasicObj obj) throws Exception {
        if (obj.objName().equals(ObjectName.IX_POI)) {
            IxPoiObj poiObj = (IxPoiObj) obj;
            IxPoi poi = (IxPoi) poiObj.getMainrow();

            Geometry geometry = poi.getGeometry();

            String type = geometry.getGeometryType();
            if(!GeometryTypeName.POINT.equals(type)) {
                setCheckResult(geometry, String.format("[IX_POI,%s]", poi.getPid()), poi.getMeshId());
            }
        }
    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {

    }
}
