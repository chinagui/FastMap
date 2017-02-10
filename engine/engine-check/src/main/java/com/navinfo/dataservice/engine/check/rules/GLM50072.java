package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkKind;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Created by chaixin on 2017/1/13 0013.
 */
public class GLM50072 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof ZoneLinkKind) {
                ZoneLinkKind linkKind = (ZoneLinkKind) row;
                int kind = linkKind.getKind();
                if (linkKind.changedFields().containsKey("kind"))
                    kind = (int) linkKind.changedFields().get("kind");
                if (kind != 0)
                    continue;

                RdLink link = (RdLink) new RdLinkSelector(getConn()).loadById(linkKind.getLinkPid(), false);
                Geometry geometry = GeoTranslator.transform(link.getGeometry(), 0.00001, 5);
                if (MeshUtils.isMeshLine(geometry))
                    setCheckResult(geometry, "[RD_LINK," + link.pid() + "]", link.mesh());
            }
        }
    }
}
