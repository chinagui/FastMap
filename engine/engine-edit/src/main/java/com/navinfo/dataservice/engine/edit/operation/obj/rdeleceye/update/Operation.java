package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.update;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

public class Operation implements IOperation {

    private Command command;

    private Connection conn;

    public Operation(Command command) {
        this.command = command;
    }

    public Operation(Connection conn) {
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        updateRdElectroniceye(result);

        return null;
    }

    public void updateRdElectroniceye(Result result) throws Exception {
        JSONObject content = command.getContent();

        RdElectroniceye eleceye = command.getEleceye();

        // 修改电子眼
        boolean isChanged = eleceye.fillChangeFields(content);

        if (isChanged) {
            result.insertObject(eleceye, ObjStatus.UPDATE, eleceye.pid());
            result.setPrimaryPid(eleceye.pid());
        }
    }

    public String breakRdLink(Result result, int oldLinkPid, List<RdLink> newLinks) {
        RdElectroniceyeSelector selector = new RdElectroniceyeSelector(this.conn);
        try {
            // 取出被打断线段上的所有电子眼
            List<RdElectroniceye> eleceyes = selector.loadListByRdLinkId(oldLinkPid, true);
            for (RdElectroniceye eleceye : eleceyes) {
                double minLength = -1;
                int minPid = 0;
                for (RdLink link : newLinks) {
                    Geometry point = GeoTranslator.transform(eleceye.getGeometry(), 0.00001, 5);
                    Geometry linkG = GeoTranslator.transform(link.getGeometry(), 0.00001, 5);
                    Coordinate pedal = GeometryUtils.getLinkPedalPointOnLine(point.getCoordinate(), linkG);
                    if (pedal == null) {
                        continue;
                    }
                    double curLength = GeometryUtils.getLinkLength(GeometryUtils.getLineFromPoint(new double[]{point
                            .getCoordinate().x, point.getCoordinate().y}, new double[]{pedal.x, pedal.y}));
                    // 判断电子眼的坐标存在于哪条新生成的线段上并更新电子眼信息
                    if (minLength == -1 || curLength < minLength) {
                        minLength = curLength;
                        minPid = link.pid();
                    }
                }
                eleceye.changedFields().put("linkPid", minPid);
                result.insertObject(eleceye, ObjStatus.UPDATE, eleceye.pid());
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public void updateRdElectroniceyeWithDirect(RdLink link, Result result) throws Exception {
        RdElectroniceyeSelector selector = new RdElectroniceyeSelector(conn);
        List<RdElectroniceye> eleceyes = selector.loadListByRdLinkId(link.pid(), true);
        Integer direct = (Integer) link.changedFields().get("direct");
        for (RdElectroniceye eleceye : eleceyes) {
            if (2 == direct || 3 == direct) {
                eleceye.changedFields().put("direct", direct);
                result.insertObject(eleceye, ObjStatus.UPDATE, eleceye.pid());
            }
        }
    }

}
