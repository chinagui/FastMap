package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceTopo;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;

/**
 * 创建土地利用点相关土地利用面具体相关操作类
 */
public class OpRefLuFace implements IOperation {

    private Command command;

    private Result result;

    private Connection conn;

    public OpRefLuFace(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;

    }

    @Override
    public String run(Result result) throws Exception {

        this.result = result;
        if (command.getLuFaceTopos() != null && command.getLuFaceTopos().size() > 0) {
            this.handleLuFaceTopo();
        }
        return null;
    }

    /*
     * 修改LuFace 和LuLink 的 topo 关系
     */
    private void handleLuFaceTopo() throws Exception {
        List<LuLink> links;
        // 1.获取打断点涉及的面信息
        // 2.删除打断线对应面的topo关系
        // 3.重新获取组成面的link关系，重新计算面的形状
        for (LuFace face : command.getFaces()) {
            links = new ArrayList<LuLink>();
            for (IRow iRow : face.getFaceTopos()) {
                LuFaceTopo obj = (LuFaceTopo) iRow;
                if (obj.getLinkPid() != command.getLinkPid()) {
                    links.add((LuLink) new LuLinkSelector(conn).loadById(obj.getLinkPid(), true));
                }
                result.insertObject(obj, ObjStatus.DELETE, face.getPid());
            }
            links.addAll(command.getNewLinks());
            com.navinfo.dataservice.engine.edit.operation.obj.luface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.luface.create.Operation(conn, result, face);
            opFace.reCaleFaceGeometry(links);
        }

    }

}