package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.model.utils.CheckGeometryUtils;
import com.navinfo.navicommons.exception.ServiceException;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: GLM50029
 * @Package: com.navinfo.dataservice.engine.check.rules
 * @Description: AOIZone类型的Face，相互不能存在重叠区域；KDZone类型的Face，相互不能存在重叠区域；
 * @Author: Crayeres
 * @Date: 5/22/2017
 * @Version: V1.0
 */
public class GLM50029 extends baseRule {

    /**
     * 日志记录
     */
    private static Logger logger = Logger.getLogger(GLM50029.class);

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        List<Integer> excludes = new ArrayList<>();
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof ZoneFace && row.status() == ObjStatus.DELETE) {
                excludes.add(((ZoneFace) row).pid());
            }
        }

        for (IRow row : checkCommand.getGlmList()) {
           if (row instanceof ZoneFace && row.status() != ObjStatus.DELETE) {

               ZoneFace face = (ZoneFace) row;
               int regionId = face.getRegionId();
               if (face.changedFields().containsKey("regionId")) {
                   regionId = Integer.parseInt(face.changedFields().get("regionId").toString());
               }
               if (regionId == 0) {
                   continue;
               }


               AdAdminSelector adAdminSelector = new AdAdminSelector(getConn());
               double adminType;
               try {
                   AdAdmin adAdmin = (AdAdmin) adAdminSelector.loadById(regionId, false);
                   adminType = adAdmin.getAdminType();
               } catch (Exception e) {
                   logger.error(String.format("ZoneFace:%d, RegionId: %d, 查找对应行政区划代表点出错", face.pid(), face.getRegionId()));
                   continue;
               }

               this.checkAdminType(adminType, face, adAdminSelector, excludes);
           } else if (row instanceof AdAdmin && row.status() == ObjStatus.UPDATE) {
               AdAdmin adAdmin = (AdAdmin) row;

               double adminType = adAdmin.getAdminType();
               if (adAdmin.changedFields().containsKey("adminType")) {
                   adminType = Double.parseDouble(adAdmin.changedFields().get("adminType").toString());
               }

               if (8 != adminType && 9 != adminType) {
                   continue;
               }

               ZoneFaceSelector zoneFaceSelector = new ZoneFaceSelector(getConn());
               List<ZoneFace> list = zoneFaceSelector.loadZoneFaceByRegionId(adAdmin.getPid(), false);

               AdAdminSelector adminSelector = new AdAdminSelector(getConn());
               for (ZoneFace face : list) {
                   checkAdminType(adminType, face, adminSelector, excludes);
               }
           }
        }
    }

    /**
     * 检查重叠面的AdminType是否同时为8或9
     * @param adminType 待检查面AdminType
     * @param face 待检查面
     * @param adminSelector
     * @throws ServiceException
     * @throws JSONException
     */
    private void checkAdminType(double adminType, ZoneFace face, AdAdminSelector adminSelector, List<Integer> excludes) throws ServiceException, JSONException {
        if (8 != adminType && 9 != adminType) {
            return;
        }

        Geometry geometry = GeoTranslator.transform(face.getGeometry(), GeoTranslator.dPrecisionMap, 5);
        if (face.changedFields().containsKey("geometry")) {
            geometry = GeoTranslator.geojson2Jts(
                    (JSONObject) face.changedFields().get("geometry"), GeoTranslator.dPrecisionMap, 5);
        }
        String wkt = GeoTranslator.jts2Wkt(geometry);

        ZoneFaceSelector zoneFaceSelector = new ZoneFaceSelector(getConn());
        List<ZoneFace> list = zoneFaceSelector.listZoneface(wkt, excludes, false);
        for (ZoneFace zoneFace : list) {
            if (face.pid() == zoneFace.pid()) {
                continue;
            }
            if (CheckGeometryUtils.isOnlyEdgeShared(geometry, zoneFace.getGeometry())) {
                continue;
            }

            AdAdmin adAdmin;
            try {
                adAdmin = (AdAdmin) adminSelector.loadById(zoneFace.getRegionId(), false);
            } catch (Exception e) {
                logger.error(String.format("ZoneFace:%d, RegionId: %d, 查找对应行政区划代表点出错", zoneFace.pid(), zoneFace.getRegionId()));
                continue;
            }

            if (adminType == adAdmin.getAdminType()) {
                setCheckResult("", String.format("[%s,%d]", face.tableName().toUpperCase(), face.pid()), 0);
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
    }
}
