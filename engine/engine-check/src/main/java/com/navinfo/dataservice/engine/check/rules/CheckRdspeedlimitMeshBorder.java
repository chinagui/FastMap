package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

/**
 * Created by chaixin on 2016/12/27 0027.
 */
public class CheckRdspeedlimitMeshBorder extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow obj : checkCommand.getGlmList()) {
            if (obj instanceof RdElectroniceye) {
                RdElectroniceye electroniceye = (RdElectroniceye) obj;
                Geometry geometry = electroniceye.getGeometry();
                if (electroniceye.changedFields().containsKey("geometry")) {
                    geometry = GeoTranslator.geojson2Jts((JSONObject) electroniceye.changedFields().get("geometry"));
                }
                if (MeshUtils.isPointAtMeshBorderWith100000(geometry.getCoordinate().x, geometry.getCoordinate().y)) {
                    this.setCheckResult("", "", 0);
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
    }
}
