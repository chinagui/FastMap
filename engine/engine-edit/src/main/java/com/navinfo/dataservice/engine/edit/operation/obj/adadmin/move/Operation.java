package com.navinfo.dataservice.engine.edit.operation.obj.adadmin.move;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.util.List;

/**
 * @author 张小龙
 * @version V1.0
 * @Title: Operation.java
 * @Description: 移动行政区划代表点操作类
 * @date 2016年4月18日 下午2:36:15
 */
public class Operation implements IOperation {

    private Command command;

    private AdAdmin moveAdmin;

    private Connection conn;

    public Operation(Command command, AdAdmin moveAdmin) {
        this.command = command;

        this.moveAdmin = moveAdmin;
    }

    public Operation(Connection conn) {
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {

        result.setPrimaryPid(moveAdmin.getPid());

        this.updateAdminGeometry(result);

        return null;
    }

    private void updateAdminGeometry(Result result) throws Exception {

        JSONObject geojson = new JSONObject();

        geojson.put("type", "Point");

        geojson.put("coordinates", new double[]{command.getLongitude(), command.getLatitude()});

        JSONObject updateContent = new JSONObject();

        // 根据经纬度计算图幅ID
        String meshIds[] = CompGeometryUtil.geo2MeshesWithoutBreak(GeoTranslator.geojson2Jts(geojson, 1, 5));

        if (meshIds.length > 1) {
            throw new Exception("不能在图幅线上创建行政区划代表点");
        }
        if (meshIds.length == 1) {
            updateContent.put("meshId", Integer.parseInt(meshIds[0]));
        }

        updateContent.put("geometry", geojson);

        updateContent.put("linkPid", command.getLinkPid());

        moveAdmin.fillChangeFields(updateContent);

        result.insertObject(moveAdmin, ObjStatus.UPDATE, moveAdmin.pid());
    }

    /**
     * 行政区划代表点引导link被打断时更新引导link信息为最近的link
     *
     * @param oldLink
     * @param newLinks
     * @param result
     * @throws Exception
     */
    public void moveAdAdmin(RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {
        AdAdminSelector selector = new AdAdminSelector(conn);
        List<AdAdmin> adAdmins = selector.loadRowsByLinkId(oldLink.pid(), true);
        for (AdAdmin adAdmin : adAdmins) {
            RdLink resultLink = breakAdminGuideLink(adAdmin, oldLink, newLinks);
            if (null != resultLink) {
                adAdmin.changedFields().put("linkPid", resultLink.pid());
                result.insertObject(adAdmin, ObjStatus.UPDATE, adAdmin.pid());
            }
        }
    }

    private RdLink breakAdminGuideLink(AdAdmin admin, RdLink oldLink, List<RdLink> newLinks) throws Exception {
        RdLink resultLink = null;
        if (admin != null && newLinks.size() > 1) {
            Geometry point = GeoTranslator.transform(admin.getGeometry(), 0.000001, 5);
            double minLength = 0;
            for (RdLink newLink : newLinks) {
                Coordinate cor = GeometryUtils.getLinkPedalPointOnLine(point.getCoordinate(), GeoTranslator.transform(newLink.getGeometry(), 0.000001, 5));
                if (cor != null) {
                    double length = GeometryUtils.getDistance(cor, point.getCoordinate());
                    if (minLength == 0 || minLength > length)
                        resultLink = newLink;
                }
            }
        }
        return resultLink;
    }
}
