package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.update;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;
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
                for (RdLink link : newLinks) {
                    // 判断电子眼的坐标存在于哪条新生成的线段上并更新电子眼信息
                    if (this.isOnTheLine(eleceye.getGeometry(), link.getGeometry())) {
                        eleceye.changedFields().put("linkPid", link.pid());
                        result.insertObject(eleceye, ObjStatus.UPDATE, eleceye.pid());
                    }
                }
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
        return line.distance(point) <= 1;
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
