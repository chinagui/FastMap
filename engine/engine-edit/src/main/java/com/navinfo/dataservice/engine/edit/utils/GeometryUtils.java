package com.navinfo.dataservice.engine.edit.utils;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @Title: GeometryUtils
 * @Package: com.navinfo.dataservice.engine.edit.utils
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 6/23/2017
 * @Version: V1.0
 */
public class GeometryUtils {
    private GeometryUtils(){

    }

    /**
     * 获取对象几何
     *
     * @param row 对象
     * @return 对象几何
     */
    public static Geometry loadGeometry(IRow row) {
        Geometry geometry = null;
        switch (row.objType()) {
            case RDNODE: geometry = ((RdNode) row).getGeometry(); break;
            case RDLINK: geometry = ((RdLink) row).getGeometry(); break;
            case LUNODE: geometry = ((LuNode) row).getGeometry(); break;
            case LULINK: geometry = ((LuLink) row).getGeometry(); break;
            case LUFACE: geometry = ((LuFace) row).getGeometry(); break;
            case LCNODE: geometry = ((LcNode) row).getGeometry(); break;
            case LCLINK: geometry = ((LcLink) row).getGeometry(); break;
            case LCFACE: geometry = ((LcFace) row).getGeometry(); break;
            case RWNODE: geometry = ((RwNode) row).getGeometry(); break;
            case RWLINK: geometry = ((RwLink) row).getGeometry(); break;
            case ADNODE: geometry = ((AdNode) row).getGeometry(); break;
            case ADLINK: geometry = ((AdLink) row).getGeometry(); break;
            case ADFACE: geometry = ((AdFace) row).getGeometry(); break;
            case ZONENODE: geometry = ((ZoneNode) row).getGeometry(); break;
            case ZONELINK: geometry = ((ZoneLink) row).getGeometry(); break;
            case ZONEFACE: geometry = ((ZoneFace) row).getGeometry(); break;
            default: geometry = null;
        }
        return geometry;
    }
}
