package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

/**
 * Created by chaixin on 2017/1/19 0019.
 */
public class ShapingCheckLinkRingobreak2 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof ZoneLink && row.status() != ObjStatus.DELETE) {
                ZoneLink link = (ZoneLink) row;

                Geometry geometry = GeoTranslator.transform(link.getGeometry(), 0.00001, 5);
                if (link.changedFields().containsKey("geometry"))
                    geometry = GeoTranslator.geojson2Jts((JSONObject) link.changedFields().get("geometry"), 0.00001, 5);

                double length = GeometryUtils.getLinkLength(geometry);
                log.info("ShapingCheckLinkRingobreak2:[geometry=" + geometry + ",length=" + length + "]");
                if (length <= 2) {
                    setCheckResult(link.getGeometry(), "[ZONE_LINK," + link.pid() + "]", link.mesh());
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
