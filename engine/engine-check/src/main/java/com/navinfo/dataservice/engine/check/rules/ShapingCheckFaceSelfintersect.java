package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.navicommons.geo.computation.GeometryUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chaixin on 2017/1/19 0019.
 */
public class ShapingCheckFaceSelfintersect extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow nodeRow : checkCommand.getGlmList()) {
            if (nodeRow instanceof ZoneNode) {
                ZoneNode node = (ZoneNode) nodeRow;
                if (!node.changedFields().containsKey("geometry")) continue;

                ZoneFaceSelector selector = new ZoneFaceSelector(getConn());
                List<ZoneFace> faces = selector.loadZoneFaceByNodeId(node.pid(), false);
                Map<Integer, ZoneFace> faceMap = new HashMap<>();
                for (IRow faceRow : checkCommand.getGlmList()) {
                    if (faceRow instanceof ZoneFace) {
                        ZoneFace face = (ZoneFace) faceRow;
                        faceMap.put(face.pid(), face);
                    }
                }
                for (ZoneFace face : faces) {
                    ZoneFace zoneFace = faceMap.get(face.pid());
                    if (!GeometryUtils.getInterPointFromSelf(GeoTranslator.transform(zoneFace.getGeometry(), 0.00001,
                            5)).isEmpty())
                        setCheckResult("", "", 0);
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
