package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;

/**
 * @author zhaokk 创建行政区划点有关行政区划面具体操作类
 */
public class OpRefAdFace implements IOperation {

    private Command command;

    private Result result;

    private Connection conn;

    public OpRefAdFace(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;

    }

    @Override
    public String run(Result result) throws Exception {

        this.result = result;
        if (command.getFaces() != null && command.getFaces().size() > 0) {
            this.handleAdFaceTopo();
        }
        return null;
    }

    /*
     * @param List 修改AdFace 和AdLink topo 关系
     */
    private void handleAdFaceTopo() throws Exception {
        List<AdLink> links;
        // 1.获取打断点涉及的面信息
        // 2.删除打断线对应面的topo关系
        // 3.重新获取组成面的link关系，重新计算面的形状
        for (AdFace face : command.getFaces()) {
            links = new ArrayList<AdLink>();
            for (IRow iRow : face.getFaceTopos()) {
                AdFaceTopo obj = (AdFaceTopo) iRow;
                if (obj.getLinkPid() != command.getLinkPid()) {
                    links.add((AdLink) new AdLinkSelector(conn).loadById(obj.getLinkPid(), true));
                }
                result.insertObject(obj, ObjStatus.DELETE, face.getPid());
            }
            links.addAll(this.command.getNewLinks());
            com.navinfo.dataservice.engine.edit.operation.obj.adface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.adface.create.Operation(conn, result, face);
            opFace.reCaleFaceGeometry(links);
        }

    }

}