package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.navicommons.geo.computation.GeometryUtils;

import java.util.List;

/**
 * Created by chaixin on 2017/1/19 0019.
 */
public class PermitCheckNodeIsNotInNotsimpleLink extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow nodeRow : checkCommand.getGlmList()) {
            if (nodeRow instanceof ZoneNode) {
                ZoneNode node = (ZoneNode) nodeRow;
                if (node.status().equals(OperType.CREATE)) {
                    for (IRow linkRow : checkCommand.getGlmList()) {
                        if (linkRow instanceof ZoneLink) {
                            ZoneLink link = (ZoneLink) linkRow;
                            if (!link.status().equals(OperType.DELETE)) continue;

                            ZoneFaceSelector selector = new ZoneFaceSelector(getConn());
                            List<ZoneFace> faces = selector.loadZoneFaceByLinkId(link.pid(), false);
                            for (ZoneFace face : faces) {
                                if (!GeometryUtils.getInterPointFromSelf(GeoTranslator.transform(face.getGeometry(),
                                        0.00001, 5)).isEmpty())
                                    setCheckResult("", "", 0);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
