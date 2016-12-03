package com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.mileagepile.RdMileagepile;
import com.navinfo.dataservice.dao.glm.selector.rd.mileagepile.RdMileagepileSelector;
import com.vividsolutions.jts.geom.Geometry;

import java.sql.Connection;
import java.util.List;

/**
 * Created by chaixin on 2016/11/9 0009.
 */
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
        boolean isChanged = command.getMileagepile().fillChangeFields(command.getContent());
        if (isChanged) {
            result.insertObject(command.getMileagepile(), ObjStatus.UPDATE, command.getMileagepile().pid());
        }
        return null;
    }

    /**
     * 用于打断时维护里程桩
     *
     * @param linkPid  原始RdLink
     * @param newLinks 打断后RdLink
     * @param result   结果集
     * @return
     * @throws Exception
     */
    public String breakRdLink(int linkPid, List<RdLink> newLinks, Result result) throws Exception {
        RdMileagepileSelector selector = new RdMileagepileSelector(this.conn);
        try {
            // 取出被打断线段上的所有里程桩
            List<RdMileagepile> mileagepiles = selector.loadByLinkPid(linkPid, true);
            for (RdMileagepile mileagepile : mileagepiles) {
                for (RdLink link : newLinks) {
                    // 判断里程桩的坐标存在于哪条新生成的线段上并更新里程桩信息
                    if (this.isOnTheLine(mileagepile.getGeometry(), link.getGeometry())) {
                        mileagepile.changedFields().put("linkPid", link.pid());
                        result.insertObject(mileagepile, ObjStatus.UPDATE, mileagepile.pid());
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
        return line.intersects(point);
    }

}
