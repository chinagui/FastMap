package com.navinfo.dataservice.engine.edit.operation.obj.rdcross.depart;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;

import java.sql.Connection;
import java.util.*;

/**
 * Created by chaixin on 2016/10/11 0011.
 */
public class Operation {
    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    /**
     * 维护上下线分离对RdCross的影响
     *
     * @param links      分离线
     * @param leftLinks  分离后左线
     * @param rightLinks 分离后右线
     * @param result     结果集
     * @return
     * @throws Exception
     */
    public String updownDepart(List<RdLink> links, Map<Integer, RdLink> leftLinks, Map<Integer, RdLink> rightLinks, Result result) throws Exception {
        RdCrossSelector selector = new RdCrossSelector(conn);
        // 1.路口点为目标link的经过点
        Set<Integer> tmpNodePids = new HashSet<Integer>();
        for (RdLink link : links) {
            tmpNodePids.add(link.getsNodePid());
            tmpNodePids.add(link.geteNodePid());
        }
        List<Integer> nodePids = new ArrayList<>(tmpNodePids).subList(1, tmpNodePids.size() - 1);
        if (nodePids.isEmpty())
            return "";
        List<RdCross> crosses = selector.loadRdCrossByNodeOrLink(nodePids, new ArrayList<Integer>(), true);
        for (RdCross cross : crosses) {
            result.insertObject(cross, ObjStatus.DELETE, cross.pid());
        }
        return "";
    }
}
