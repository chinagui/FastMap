package com.navinfo.dataservice.engine.edit.operation.topo.depart.departrdnode;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

import java.sql.Connection;
import java.util.List;

/**
 * 分离节点维护CRF Road
* @ClassName: OpRefRdRoad 
* @author Zhang Xiaolong
* @date 2016年11月30日 上午10:23:47 
* @Description: 维护原则：对构成此信息的Link及其任一端点Node进行分离，那么CRFRoad应该被删除；
* CRFObject中删除与其相关的单Link或Road，如果删除后无其他要素，CRFObj也被删除；原Node上的CRFInter不变
 */
public class OpRefRdRoad {
    private Connection conn;

    public OpRefRdRoad(Connection conn) {
        this.conn = conn;
    }

    public void updateRelation(Command command, List<RdLink> newLinks, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdroad.depart.Opeartion operation = new
                com.navinfo.dataservice.engine.edit.operation.obj.rdroad.depart.Opeartion(this.conn);
        operation.depart(command.getNodePid(), command.getRdLink(), newLinks, result);
    }
}
