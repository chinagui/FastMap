package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

import static com.sun.tools.doclint.Entity.ge;

/**
 * Created by chaixin on 2017/1/9 0009.
 */
public class CheckRdelectroniceyeOnMeshborder extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdElectroniceye) {
                RdElectroniceye eleceye = (RdElectroniceye) row;
                Geometry geometry = eleceye.getGeometry();
                if (eleceye.changedFields().containsKey("geometry"))
                    geometry = GeoTranslator.geojson2Jts((JSONObject) eleceye.changedFields().get("geometry"));
                if (MeshUtils.isPointAtMeshBorderWith100000(geometry.getCoordinate().x, geometry.getCoordinate().y))
                    setCheckResult(geometry, "[RD_ELECTRONICEYE, " + eleceye.pid() + "]", eleceye.mesh());
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
