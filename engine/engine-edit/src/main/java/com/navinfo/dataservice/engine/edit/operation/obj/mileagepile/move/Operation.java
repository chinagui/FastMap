package com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.move;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.mileagepile.RdMileagepile;
import com.navinfo.dataservice.dao.glm.selector.rd.mileagepile.RdMileagepileSelector;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.util.List;

/**
 * Created by chaixin on 2016/11/9 0009.
 */
public class Operation implements IOperation {
    private Command command;

    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    public Operation(Command command) {
        this.command = command;
    }

    @Override
    public String run(Result result) throws Exception {
        JSONObject geoPoint = new JSONObject();
        geoPoint.put("type", "Point");
        geoPoint.put("coordinates", new double[]{command.getContent().getDouble("longitude"), command.getContent().getDouble("latitude")});
        command.getMileagepile().changedFields().put("geometry", geoPoint);
        String[] meshes = MeshUtils.point2Meshes(command.getContent().getDouble("longitude"), command.getContent().getDouble("latitude"));
        command.getMileagepile().changedFields().put("meshId", Integer.valueOf(meshes[0]));
        command.getMileagepile().changedFields().put("linkPid", command.getContent().getInt("linkPid"));
        result.insertObject(command.getMileagepile(), ObjStatus.UPDATE, command.getMileagepile().pid());
        return null;
    }

    /**
     * 移动RDNODE时维护里程桩的坐标以及关联Link
     *
     * @param oldLink  里程桩原关联rdlink
     * @param newLinks 新生成的rdlinks
     * @param result
     * @return
     * @throws Exception
     */
    public String moveMileagepile(RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {
        // 查询出所有会被影响的里程桩
        RdMileagepileSelector selector = new RdMileagepileSelector(this.conn);
        List<RdMileagepile> mileagepiles = selector.loadByLinkPid(oldLink.pid(), true);

        // 定义里程桩是否在新生成的线段上
        boolean isOnTheLine;
        // 循环处理所有里程桩
        for (RdMileagepile mileagepile : mileagepiles) {
            isOnTheLine = false;
            for (RdLink link : newLinks) {
                // 判断里程桩的坐标是否在任意一条新生成的线段上
                isOnTheLine = this.isOnTheLine(mileagepile.getGeometry(), link.getGeometry());
                // 当里程桩的坐标在新生成的线段时仅维护该里程桩的linkpid属性
                if (isOnTheLine) {
                    mileagepile.changedFields().put("linkPid", link.pid());
                    result.insertObject(mileagepile, ObjStatus.UPDATE, mileagepile.pid());
                    break;
                }
            }
            // 当里程桩坐标不再所有的新生成线段
            if (!isOnTheLine) {
                Coordinate minPoint = null;
                double minLength = 0;
                int minLinkPid = 0;
                // 计算里程桩原坐标与新生成线段最近的壹个点的坐标
                Coordinate hgwgLimitCoor = GeoTranslator.transform(mileagepile.getGeometry(), 0.00001, 5).getCoordinate();
                for (RdLink rdLink : newLinks) {
                    Coordinate tmpPoint = GeometryUtils.GetNearestPointOnLine(hgwgLimitCoor, GeoTranslator.transform(rdLink.getGeometry(), 0.00001, 5));
                    double tmpLength = GeometryUtils.getDistance(hgwgLimitCoor, tmpPoint);
                    if (minLength == 0 || tmpLength < minLength) {
                        minLength = tmpLength;
                        minLinkPid = rdLink.pid();
                        minPoint = tmpPoint;
                    }
                }
                JSONObject geoPoint = new JSONObject();
                geoPoint.put("type", "Point");
                geoPoint.put("coordinates", new double[]{minPoint.x, minPoint.y});
                Geometry tmpGeo = GeoTranslator.geojson2Jts(geoPoint);
                geoPoint = GeoTranslator.jts2Geojson(tmpGeo, 0.00001, 5);
                mileagepile.changedFields().put("geometry", geoPoint);
                mileagepile.changedFields().put("linkPid", minLinkPid);
                result.insertObject(mileagepile, ObjStatus.UPDATE, mileagepile.pid());
            }
        }
        return null;
    }

    /**
     * 判断点是否在线段上
     *
     * @param point 点
     * @param line  线段
     * @return true 是，false 否
     */
    private boolean isOnTheLine(Geometry point, Geometry line) {
        return line.intersects(point);
    }
}
