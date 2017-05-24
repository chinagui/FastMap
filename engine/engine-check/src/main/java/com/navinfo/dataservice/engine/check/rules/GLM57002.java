package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildfaceSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

/**
 * @Title: GLM57002
 * @Package: com.navinfo.dataservice.engine.check.rules
 * @Description: 市街图中的面和面不应完全或部分重叠，否则报log
 * @Author: Crayeres
 * @Date: 5/22/2017
 * @Version: V1.0
 */
public class GLM57002 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (!(row instanceof CmgBuildface) || row.status() == ObjStatus.DELETE) {
                continue;
            }

            CmgBuildface face = (CmgBuildface) row;
            Geometry geometry = face.getGeometry();
            if (face.changedFields().containsKey("geometry")) {
                geometry = GeoTranslator.geojson2Jts((JSONObject) row.changedFields().get("geometry"));
            }
            String wkt = GeoTranslator.jts2Wkt(geometry, GeoTranslator.dPrecisionMap,5);
            int count = new CmgBuildfaceSelector(getConn()).countCmgBuildface(wkt, false);
            if (0 < count) {
                setCheckResult("市街图面重合", String.format("[%s,%d]", row.tableName().toUpperCase(), face.pid()),0);
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
