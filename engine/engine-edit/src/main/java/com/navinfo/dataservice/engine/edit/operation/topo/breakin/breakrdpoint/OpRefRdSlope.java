/**
 *
 */
package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

/**
 * @author 赵凯凯
 * @ClassName: OpRefRdSlope
 * @date 2016年7月21日 下午5:03:21
 * @Description: TODO
 */
public class OpRefRdSlope {

    private Connection conn;

    public OpRefRdSlope(Connection conn) {
        this.conn = conn;
    }

    public String run(Result result, int linkPid, List<RdLink> newLinks) throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdslope.update.Operation rdSlopeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.update.Operation(conn);

        rdSlopeOperation.breakRdLink(null, linkPid, newLinks, result);

        return null;
    }

}
