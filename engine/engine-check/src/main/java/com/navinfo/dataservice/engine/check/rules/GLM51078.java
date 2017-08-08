package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildfaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lc.LcFaceSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: GLM51078
 * @Package: com.navinfo.dataservice.engine.check.rules
 * @Description: 建筑物面不能与土地覆盖中的Face相交，即：建筑物面只能在绿地面内或绿地面外
 * @Author: Crayeres
 * @Date: 5/23/2017
 * @Version: V1.0
 */
public class GLM51078 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        List<Integer> excludesCmg = new ArrayList<>();
        List<Integer> excludesLc = new ArrayList<>();
        for (IRow row : checkCommand.getGlmList()) {
            if (row.status() == ObjStatus.DELETE) {
                if (row instanceof CmgBuildface) {
                    excludesCmg.add(((CmgBuildface) row).pid());
                }
                if (row instanceof LcFace) {
                    excludesLc.add(((LcFace) row).pid());
                }
            }
        }

        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof LcFace && row.status() != ObjStatus.DELETE) {
                LcFace face = (LcFace) row;
                Geometry geometry = GeoTranslator.transform(face.getGeometry(), GeoTranslator.dPrecisionMap, 5);
                if (face.changedFields().containsKey("geometry")) {
                    geometry = GeoTranslator.geojson2Jts(
                            (JSONObject) face.changedFields().get("geometry"), GeoTranslator.dPrecisionMap, 5);
                }
                String wkt = GeoTranslator.jts2Wkt(geometry);

                List<CmgBuildface> list = new CmgBuildfaceSelector(getConn()).listCmgBuildface(wkt, excludesCmg, false);
                for (CmgBuildface cmgface : list) {
                    Geometry tmpGep = GeoTranslator.transform(cmgface.getGeometry(), GeoTranslator.dPrecisionMap, 5);
                    if (!geometry.covers(tmpGep) && !geometry.coveredBy(tmpGep)) {
                        setCheckResult("", String.format("[%s,%d]", face.tableName().toUpperCase(), face.pid()), 0);
                    }
                }
            } else if (row instanceof CmgBuildface && row.status() != ObjStatus.DELETE) {
                CmgBuildface face = (CmgBuildface) row;
                Geometry geometry = GeoTranslator.transform(face.getGeometry(), GeoTranslator.dPrecisionMap, 5);
                if (face.changedFields().containsKey("geometry")) {
                    geometry = GeoTranslator.geojson2Jts(
                            (JSONObject) face.changedFields().get("geometry"), GeoTranslator.dPrecisionMap, 5);
                }
                String wkt = GeoTranslator.jts2Wkt(geometry);

                List<LcFace> list = new LcFaceSelector(getConn()).listLcface(wkt, excludesLc,false);
                for (LcFace lcFace : list) {
                    Geometry tmpGep = GeoTranslator.transform(lcFace.getGeometry(), GeoTranslator.dPrecisionMap, 5);
                    if (!geometry.covers(tmpGep) && !geometry.coveredBy(tmpGep)) {
                        setCheckResult("", String.format("[%s,%d]", face.tableName().toUpperCase(), face.pid()), 0);
                    }
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
