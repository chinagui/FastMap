package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

/**
 * 打断link维护可变限速的关系
 *
 * @author Zhang Xiaolong
 * @ClassName: OpRefRdVariableSpeed
 * @date 2016年8月17日 下午2:00:16
 * @Description: TODO
 */
public class OpRefRdVariableSpeed {

    private Connection conn;

    public OpRefRdVariableSpeed(Connection conn) {
        this.conn = conn;
    }

    public String run(Result result, RdLink oldLink, List<RdLink> newLinks) throws Exception {

        // 维护可变限速关系
        com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.update.Operation(
                this.conn);
        operation.breakLine(null, oldLink, newLinks, result);

        return null;
    }
}
