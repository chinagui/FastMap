package com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.depart;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.hgwg.RdHgwgLimit;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.dao.glm.selector.rd.tollgate.RdTollgateSelector;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chaixin on 2016/12/21 0021.
 */
public class Operation {

    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    public String updownDepart(RdNode sNode, List<RdLink> links, Map<Integer, RdLink> leftLinks, java.util.Map<Integer, RdLink> rightLinks, Map<Integer, RdLink> noTargetLinks, Result result) throws Exception {
        // 查找上下线分离对影响到的收费站
        Set<Integer> nodeSet = new HashSet<>();
        for (RdLink link : links) {
            nodeSet.add(link.getsNodePid());
            nodeSet.add(link.geteNodePid());
        }
        List<Integer> nodePids = new ArrayList<>();
        nodePids.addAll(nodeSet);
        RdTollgateSelector selector = new RdTollgateSelector(conn);
        List<RdTollgate> tollgates = selector.loadRdTollgatesWithNodePids(nodePids, true);
        for (RdTollgate tollgate : tollgates) {
            result.insertObject(tollgate, ObjStatus.DELETE, tollgate.pid());
        }
        return "";
    }
}
