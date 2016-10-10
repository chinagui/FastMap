package com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.depart;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.speedbump.RdSpeedbump;
import com.navinfo.dataservice.dao.glm.selector.rd.speedbump.RdSpeedbumpSelector;

import java.sql.Connection;
import java.util.*;

/**
 * Created by chaixin on 2016/10/9 0009.
 */
public class Operation {
    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    /**
     * 维护上下线分离时对减速带的影响
     *
     * @param links      分离线
     * @param leftLinks  分离后左线
     * @param rightLinks 分离后右线
     * @param result     结果集
     * @return
     * @throws Exception
     */
    public String updownDepart(List<RdLink> links, Map<Integer, RdLink> leftLinks, Map<Integer, RdLink> rightLinks, Result result) throws Exception {
        RdSpeedbumpSelector selector = new RdSpeedbumpSelector(conn);
        Set<Integer> linkPids = leftLinks.keySet();
        // 1.当减速带的进入link参与上下线分离时删除减速带
        List<RdSpeedbump> rdSpeedbumps = selector.loadByLinkPids(linkPids, true);
        for (RdSpeedbump speedbump : rdSpeedbumps)
            result.insertObject(speedbump, ObjStatus.DELETE, speedbump.pid());
        rdSpeedbumps.clear();
        // 2.当目标link上的点已经参与制作减速带
        Set<Integer> tmpNodePids = new HashSet<Integer>();
        for (RdLink link : links) {
            tmpNodePids.add(link.getsNodePid());
            tmpNodePids.add(link.geteNodePid());
        }
        List<Integer> nodePids = new ArrayList<>(tmpNodePids).subList(1, tmpNodePids.size() - 1);
        rdSpeedbumps = selector.loadByNodePids(nodePids, true);
        for (RdSpeedbump speedbump : rdSpeedbumps) {
            if (!result.getDelObjects().contains(speedbump))
                result.insertObject(speedbump, ObjStatus.DELETE, speedbump.pid());
        }
        rdSpeedbumps.clear();

        return "";
    }

}
