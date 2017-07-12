package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import java.util.Collection;

/**
 * @Title: GLM60225
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description: 检查对象：种别为“230201”或“230202”的POI
 * 检查原则：
 * 1.如果引导Link的上下线分离属性为“否”，那么显示坐标到引导link的最短距离在1.5米到5米之间，报错
 * 2.如果引导link的上下线分离属性为“是”，那么显示坐标应位于该对上下线分离道路的中间
 * @Author: Crayeres
 * @Date: 7/10/2017
 * @Version: V1.0
 */
public class GLM60225 extends BasicCheckRule {
    @Override
    public void runCheck(BasicObj obj) throws Exception {
        if (obj.objName().equals(ObjectName.IX_POI)) {
            IxPoiObj poiObj = (IxPoiObj) obj;
            IxPoi poi = (IxPoi) poiObj.getMainrow();

            String kindCode = poi.getKindCode();
            if (StringUtils.isEmpty(kindCode)) {
                return;
            }

            if (!"230201".equals(kindCode) && !"230202".equals(kindCode)) {
                return;
            }

            RdLink link = (RdLink) new RdLinkSelector(getCheckRuleCommand().getConn()).loadById((int) poi.getLinkPid(), false);
            Geometry linkGeometry = GeoTranslator.transform(link.getGeometry(), GeoTranslator.dPrecisionMap, 5);
            Coordinate poiCoordinate = GeoTranslator.transform(poi.getGeometry(), GeoTranslator.dPrecisionMap, 5).getCoordinate();

            int multiDigitized = link.getMultiDigitized();
            if (0 == multiDigitized) {
                Coordinate pedalCoordinate = GeometryUtils.GetNearestPointOnLine(poiCoordinate, linkGeometry);
                double distance = GeometryUtils.getDistance(poiCoordinate, pedalCoordinate);
                if (distance > 1.5 && distance < 5) {
                    setCheckResult(poi.getGeometry(), String.format("[IX_POI,%s]", poi.getPid()), poi.getMeshId());
                }
            } else {

            }

        }
    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {

    }
}
