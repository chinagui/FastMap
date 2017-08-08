package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.model.utils.CheckGeometryUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: GLM50027
 * @Package: com.navinfo.dataservice.engine.check.rules
 * @Description: 行政区划多边形中，相互不能存在重叠区域
 * @Author: Crayeres
 * @Date: 5/23/2017
 * @Version: V1.0
 */
public class GLM50027 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        List<Integer> excludes = new ArrayList<>();
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof AdFace && row.status() == ObjStatus.DELETE) {
                excludes.add(((AdFace) row).pid());
            }
        }

        for (IRow row : checkCommand.getGlmList()) {
            if (!(row instanceof AdFace) || row.status() == ObjStatus.DELETE) {
                continue;
            }

            AdFace face = (AdFace) row;
            Geometry geometry = face.getGeometry();
            if (face.changedFields().containsKey("geometry")) {
                geometry = GeoTranslator.geojson2Jts((JSONObject) row.changedFields().get("geometry"));
            }
            geometry = GeoTranslator.transform(geometry, GeoTranslator.dPrecisionMap, 5);
            String wkt = GeoTranslator.jts2Wkt(geometry);
            List<AdFace> list = new AdFaceSelector(getConn()).listAdface(wkt, excludes,false);
            for (AdFace adFace : list) {
                if (face.pid() == adFace.pid()) {
                    continue;
                }

                if (CheckGeometryUtils.isOnlyEdgeShared(geometry, adFace.getGeometry())) {
                    continue;
                }
                setCheckResult("", String.format("[%s,%d]", row.tableName().toUpperCase(), face.pid()),0);
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
