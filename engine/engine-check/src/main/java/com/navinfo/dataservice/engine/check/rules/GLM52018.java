package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.selector.lc.LcFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: GLM520187
 * @Package: com.navinfo.dataservice.engine.check.rules
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 5/23/2017
 * @Version: V1.0
 */
public class GLM52018 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        List<Integer> excludesLu = new ArrayList<>();
        List<Integer> excludesLc = new ArrayList<>();
        for (IRow row : checkCommand.getGlmList()) {
            if (row.status() == ObjStatus.DELETE) {
                if (row instanceof LuFace) {
                    excludesLu.add(((LuFace) row).pid());
                }
                if (row instanceof LcFace) {
                    excludesLc.add(((LcFace) row).pid());
                }
            }
        }

        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof LcFace && row.status() != ObjStatus.DELETE) {
                LcFace face = (LcFace) row;
                int kind = face.getKind();
                if (face.changedFields().containsKey("kind")) {
                    kind = Integer.parseInt(face.changedFields().get("kind").toString());
                }
                if (17 == kind) {
                    continue;
                }

                Geometry geometry = face.getGeometry();
                if (face.changedFields().containsKey("geometry")) {
                    geometry = GeoTranslator.geojson2Jts((JSONObject) face.changedFields().get("geometry"));
                }
                geometry = GeoTranslator.transform(geometry, GeoTranslator.dPrecisionMap, 5);
                String wkt = GeoTranslator.jts2Wkt(geometry);

                List<LuFace> list = new LuFaceSelector(getConn()).listLufaceRefWkt(wkt, excludesLu,false);
                for (LuFace luFace : list) {
                    Geometry tmpGeo = GeoTranslator.transform(luFace.getGeometry(), GeoTranslator.dPrecisionMap, 5);

                    int luKind = luFace.getKind();
                    if (6 == luKind) {
                        setCheckResult("", String.format("[%s,%d]", face.tableName().toUpperCase(), face.pid()), 0);
                    } else if (11 == luKind && !geometry.coveredBy(tmpGeo)) {
                        setCheckResult("", String.format("[%s,%d]", face.tableName().toUpperCase(), face.pid()), 0);
                    }
                }

            } else if (row instanceof LuFace && row.status() != ObjStatus.DELETE) {
                LuFace face = (LuFace) row;

                int kind = face.getKind();
                if (face.changedFields().containsKey("kind")) {
                    kind = Integer.parseInt(face.changedFields().get("kind").toString());
                }

                Geometry geometry = face.getGeometry();
                if (face.changedFields().containsKey("geometry")) {
                    geometry = GeoTranslator.geojson2Jts((JSONObject) face.changedFields().get("geometry"));
                }
                geometry = GeoTranslator.transform(geometry, GeoTranslator.dPrecisionMap, 5);
                String wkt = GeoTranslator.jts2Wkt(geometry);
                List<LcFace> list = new LcFaceSelector(getConn()).listLcface(wkt, excludesLc,false);

                if (kind == 6) {
                    for (LcFace lcFace : list) {

                        if (17 != lcFace.getKind()) {
                            setCheckResult("", String.format("[%s,%d]", face.tableName().toUpperCase(), face.pid()), 0);
                        }
                    }
                } else if (kind == 11) {
                    for (LcFace lcFace : list) {
                        Geometry tmpGeo = GeoTranslator.transform(lcFace.getGeometry(), GeoTranslator.dPrecisionMap, 5);

                        if (17 != lcFace.getKind() && !geometry.covers(tmpGeo)) {
                            setCheckResult("", String.format("[%s,%d]", face.tableName().toUpperCase(), face.pid()), 0);
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
