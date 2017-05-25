package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkKind;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @Title: PropertyCheckZoneimagelinkMapboundary
 * @Package: com.navinfo.dataservice.engine.check.rules
 * @Description: ZONE假想线必须在图廓线上
 * @Author: Crayeres
 * @Date: 2017/5/25
 * @Version: V1.0
 */
public class PropertyCheckZoneimagelinkMapboundary extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (!(row instanceof ZoneLinkKind) || row.status() == ObjStatus.DELETE) {
                continue;
            }

            ZoneLinkKind zoneLinkKind = (ZoneLinkKind) row;
            int kind = zoneLinkKind.getKind();
            if (zoneLinkKind.changedFields().containsKey("kind")) {
                kind = Integer.parseInt(zoneLinkKind.changedFields().get("kind").toString());
            }

            if (0 != kind) {
                continue;
            }

            ZoneLink link = (ZoneLink) new ZoneLinkSelector(getConn()).loadById(zoneLinkKind.getLinkPid(), false);
            Geometry geometry = GeoTranslator.transform(link.getGeometry(), GeoTranslator.dPrecisionMap, 5);
            if (!MeshUtils.isMeshLine(geometry)) {
                setCheckResult(geometry, String.format("[%s,%d]", link.tableName().toUpperCase(), link.pid()), 0);
            }
        }
    }
}
