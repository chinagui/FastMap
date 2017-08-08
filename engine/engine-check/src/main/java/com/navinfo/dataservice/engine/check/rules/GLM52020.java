package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: GLM52020
 * @Package: com.navinfo.dataservice.engine.check.rules
 * @Description: 地上停车场面(kind=6)与其它地上停车场面不能存在重叠区域，否则报log
 * @Author: Crayeres
 * @Date: 5/23/2017
 * @Version: V1.0
 */
public class GLM52020 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        List<Integer> excludes = new ArrayList<>();
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof LuFace && row.status() == ObjStatus.DELETE) {
                excludes.add(((LuFace) row).pid());
            }
        }

        for (IRow row : checkCommand.getGlmList()) {
            if (!(row instanceof LuFace) || row.status() == ObjStatus.DELETE) {
                continue;
            }

            LuFace face = (LuFace) row;
            int kind = face.getKind();
            if (face.changedFields().containsKey("kind")) {
                kind = Integer.parseInt(face.changedFields().get("kind").toString());
            }
            if (6 != kind) {
                continue;
            }

            Geometry geometry = face.getGeometry();
            if (face.changedFields().containsKey("geometry")) {
                geometry = GeoTranslator.geojson2Jts((JSONObject) face.changedFields().get("geometry"));
            }
            String wkt = GeoTranslator.jts2Wkt(geometry, GeoTranslator.dPrecisionMap, 5);
            List<LuFace> list = new LuFaceSelector(getConn()).listLufaceRefWkt(wkt, excludes,false);
            for (LuFace luface : list) {
                if (6 == luface.getKind() && face.pid() != luface.pid()) {
                    setCheckResult("", String.format("[%s,%d]", face.tableName().toUpperCase(), face.pid()),0);
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
