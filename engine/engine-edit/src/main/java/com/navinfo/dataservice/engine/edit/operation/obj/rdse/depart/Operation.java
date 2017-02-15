package com.navinfo.dataservice.engine.edit.operation.obj.rdse.depart;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.dao.glm.selector.rd.se.RdSeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.trafficsignal.RdTrafficsignalSelector;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Crayeres on 2017/2/15.
 */
public class Operation {
    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    public String updownDepart(RdNode sNode, List<RdLink> links, Map<Integer, RdLink> leftLinks, Map<Integer, RdLink>
            rightLinks, Map<Integer, RdLink> noTargetLinks, Result result) throws Exception {
        RdSeSelector selector = new RdSeSelector(conn);
        Integer[] leftLinkPids = leftLinks.keySet().toArray(new Integer[]{});
        Integer[] rightLinkPids = rightLinks.keySet().toArray(new Integer[]{});
        int length = leftLinkPids.length;
        // 1.分叉口进入点为分离线的经过点时删除分叉口
        List<Integer> nodePids = CalLinkOperateUtils.calNodePids(links);
        List<RdSe> rdSes = null;
        if (!nodePids.isEmpty()) {
            rdSes = selector.loadRdSesWithNodePids(nodePids, true);
            for (RdSe rdSe : rdSes) {
                result.insertObject(rdSe, ObjStatus.DELETE, rdSe.pid());
            }
        }
        if (links.size() > 0) {
            RdLink firstLink = links.get(0);
            RdLink endLink = links.get(length - 1);
            // 2.分叉口进入线为分离线的分叉口
            rdSes = selector.loadRdSesWithLinkPid(firstLink.getPid(), false);
            for (RdSe rdSe : rdSes) {
                if (firstLink.pid() == rdSe.getInLinkPid()) {
                    rdSe.changedFields().put("inLinkPid", leftLinkPids[0]);
                } else if (firstLink.pid() == rdSe.getOutLinkPid()) {
                    rdSe.changedFields().put("outLinkPid", rightLinkPids[0]);
                }
                result.insertObject(rdSe, ObjStatus.UPDATE, rdSe.pid());
            }

            if (firstLink.pid() == endLink.pid())
                return "";

            // 3.分叉口退出线为分离线的分叉口
            rdSes = selector.loadRdSesWithLinkPid(endLink.getPid(), false);
            for (RdSe rdSe : rdSes) {
                if (firstLink.pid() == rdSe.getInLinkPid()) {
                    rdSe.changedFields().put("inLinkPid", leftLinkPids[length - 1]);
                } else if (firstLink.pid() == rdSe.getOutLinkPid()) {
                    rdSe.changedFields().put("outLinkPid", rightLinkPids[length - 1]);
                }
                result.insertObject(rdSe, ObjStatus.UPDATE, rdSe.pid());
            }
        }
        return "";
    }
}
