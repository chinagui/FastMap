package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.selector.lc.LcFaceSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.model.utils.CheckGeometryUtils;
import com.navinfo.navicommons.geo.computation.GeometryTypeName;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: GLM51029
 * @Package: com.navinfo.dataservice.engine.check.rules
 * @Description: 土地覆盖Face中，相互不能存在重叠区域，否则报log 特殊说明：一个非岛屿面完全包含在岛屿面内的情况，不报log
 * @Author: Crayeres
 * @Date: 5/23/2017
 * @Version: V1.0
 */
public class GLM51029 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        List<Integer> excludes = new ArrayList<>();
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof LcFace && row.status() == ObjStatus.DELETE) {
                excludes.add(((LcFace) row).pid());
            }
        }

        for (IRow row : checkCommand.getGlmList()) {
            if (!(row instanceof LcFace) || row.status() == ObjStatus.DELETE) {
                continue;
            }

            LcFace face = (LcFace) row;
            int kind = face.getKind();
            if (face.changedFields().containsKey("kind")) {
                kind = Integer.parseInt(face.changedFields().get("kind").toString());
            }
            Geometry geometry = GeoTranslator.transform(face.getGeometry(), GeoTranslator.dPrecisionMap, 5);
            if (face.changedFields().containsKey("geometry")) {
                geometry = GeoTranslator.geojson2Jts(
                        (JSONObject) face.changedFields().get("geometry"), GeoTranslator.dPrecisionMap, 5);
            }
            String wkt = GeoTranslator.jts2Wkt(geometry);

            List<LcFace> list = new LcFaceSelector(getConn()).listLcface(wkt, excludes,false);
            for (LcFace lcFace : list) {
                if (face.pid() == lcFace.pid()) {
                    continue;
                }

                Geometry tmpGeo = GeoTranslator.transform(lcFace.getGeometry(), GeoTranslator.dPrecisionMap, 5);
                if (17 == kind && 17 != lcFace.getKind() && geometry.covers(tmpGeo)) {
                    continue;
                } else if (17 != kind && 17 == lcFace.getKind() && geometry.coveredBy(tmpGeo)) {
                    continue;
                }

                if (CheckGeometryUtils.isOnlyEdgeShared(geometry, tmpGeo)) {
                    continue;
                }

                setCheckResult("", String.format("[%s,%d]", face.tableName().toUpperCase(), face.pid()), 0);
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
    }
}
