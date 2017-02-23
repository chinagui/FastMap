package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFaceTopo;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkKind;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chaixin on 2017/1/19 0019.
 */
public class CheckNotCreateTooNarrowFace extends baseRule {

    private List<Integer> fictitiousLink = new ArrayList<>();

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        preparData(checkCommand);

        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof ZoneLink && row.status() != ObjStatus.DELETE) {
                ZoneLink link = (ZoneLink) row;

                if (fictitiousLink.contains(link.pid())) {
                    Geometry geometry = GeoTranslator.transform(link.getGeometry(), 0.00001, 5);
                    if (link.changedFields().containsKey("geometry"))
                        geometry = GeoTranslator.geojson2Jts((JSONObject) link.changedFields().get("geometry"),
                                0.00001, 5);

                    double length = GeometryUtils.getLinkLength(geometry);
                    log.info("CheckNotCreateTooNarrowFace:[" + length + "]");
                    if (length <= 1) {
                        setCheckResult(link.getGeometry(), "[ZONE_LINK," + link.pid() + "]", link.mesh());
                    }
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }

    private void preparData(CheckCommand checkCommand) {
        for (IRow obj : checkCommand.getGlmList()) {
            if (obj instanceof ZoneLink) {
                ZoneLink link = (ZoneLink) obj;
                if (link.status() == ObjStatus.DELETE)
                    continue;

                Map<String, Integer> kinds = new HashMap<>();
                for (IRow f : link.getKinds()) {
                    ZoneLinkKind kind = (ZoneLinkKind) f;
                    kinds.put(kind.getRowId(), kind.getKind());
                }

                for (IRow row : checkCommand.getGlmList()) {
                    if (row instanceof ZoneLinkKind) {
                        ZoneLinkKind kind = (ZoneLinkKind) row;
                        if (kind.getLinkPid() == link.pid()) {
                            if (kind.status() == ObjStatus.DELETE) {
                                kinds.remove(kind.getRowId());
                            } else {
                                int linkKind = kind.getKind();
                                if (kind.changedFields().containsKey("kind"))
                                    linkKind = Integer.valueOf(kind.changedFields().get("kind").toString());
                                kinds.put(kind.getRowId(), linkKind);
                            }
                        }
                    }
                }

                if (kinds.containsValue(0)) {
                    fictitiousLink.add(link.pid());
                }
            }
        }
    }
}
