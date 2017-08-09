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
 * @Title: GLM52017
 * @Package: com.navinfo.dataservice.engine.check.rules
 * @Description: BUA（kind=21）多边形中，相互不能存在重叠区域
 * @Author: Crayeres
 * @Date: 5/23/2017
 * @Version: V1.0
 */
public class GLM52017 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        List<Integer> excludes = new ArrayList<>();
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof LuFace && row.status() == ObjStatus.DELETE) {
                excludes.add(((LuFace) row).pid());
            }
        }

        for (IRow row : checkCommand.getGlmList()) {
            if (!(row instanceof LuFace) || row.status() != ObjStatus.UPDATE) {
                continue;
            }

            LuFace face = (LuFace) row;
            int kind = face.getKind();
            if (face.changedFields().containsKey("kind")) {
                kind = Integer.parseInt(face.changedFields().get("kind").toString());
            }

            if (21 != kind) {
                continue;
            }

            Geometry geometry = GeoTranslator.transform(face.getGeometry(), GeoTranslator.dPrecisionMap, 5);
            if (face.changedFields().containsKey("geometry")) {
                geometry = GeoTranslator.geojson2Jts((JSONObject) face.changedFields().get("geometry"), GeoTranslator.dPrecisionMap, 5);
            }
            String wkt = GeoTranslator.jts2Wkt(geometry);

            List<LuFace> list = new LuFaceSelector(getConn()).listLufaceRefWkt(wkt, excludes,false);
            for (LuFace luFace : list) {
                if (excludes.contains(luFace.pid())) {
                    continue;
                }

                if (21 == luFace.getKind() && face.pid() != luFace.pid()) {
                    setCheckResult("", String.format("[%s,%d]", row.tableName().toUpperCase(), face.pid()), 0);
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
