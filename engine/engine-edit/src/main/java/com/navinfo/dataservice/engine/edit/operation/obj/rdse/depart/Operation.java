package com.navinfo.dataservice.engine.edit.operation.obj.rdse.depart;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.dao.glm.selector.rd.se.RdSeSelector;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Crayeres on 2017/2/15.
 */
public class Operation {
    private Logger logger = Logger.getLogger(Operation.class);

    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    public String updownDepart(RdNode sNode, List<RdLink> links, Map<Integer, RdLink> leftLinks, Map<Integer, RdLink>
            rightLinks, Map<Integer, RdLink> noTargetLinks, Result result) throws Exception {
        if (links.isEmpty())
            return "";

        int sNodePid = sNode.pid();
        Map<Integer, Integer> maps = new HashMap<>();
        for (RdLink link : links) {
            if (sNodePid == link.getsNodePid()) {
                maps.put(link.pid(), 2);
                sNodePid = link.geteNodePid();
            } else if (sNodePid == link.geteNodePid()) {
                maps.put(link.pid(), 3);
                sNodePid = link.getsNodePid();
            }
        }

        // logger.info("UPDOWNDEPART:关联维护分叉口提示");
        RdSeSelector selector = new RdSeSelector(conn);
        Integer[] leftLinkPids = leftLinks.keySet().toArray(new Integer[]{});
        int length = leftLinkPids.length;
        // 1.分叉口进入点为分离线的经过点时删除分叉口
        List<Integer> nodePids = CalLinkOperateUtils.calNodePids(links);
        List<RdSe> rdSes = null;
        if (!nodePids.isEmpty()) {
            rdSes = selector.loadRdSesWithNodePids(nodePids, true);
            // logger.info("需要删除分叉口数量：" + rdSes.size());
            for (RdSe rdSe : rdSes) {
                result.insertObject(rdSe, ObjStatus.DELETE, rdSe.pid());
            }
        }
        if (links.size() > 0) {
            RdLink firstLink = links.get(0);
            RdLink endLink = links.get(length - 1);
            // 2.第一条分离Link相关的分叉口提示
            rdSes = selector.loadRdSesWithLinkPid(firstLink.getPid(), false);
            for (RdSe rdSe : rdSes) {
                if (nodePids.contains(rdSe.getNodePid()))
                    continue;

                int opDirect = maps.get(firstLink.pid());
                if (firstLink.pid() == rdSe.getInLinkPid()) {
                    if (opDirect == 2)
                        rdSe.changedFields().put("inLinkPid", rightLinks.get(leftLinkPids[0]).pid());
                    else if (opDirect == 3)
                        rdSe.changedFields().put("inLinkPid", leftLinks.get(leftLinkPids[0]).pid());
                } else if (firstLink.pid() == rdSe.getOutLinkPid()) {
                    if (opDirect == 2)
                        rdSe.changedFields().put("outLinkPid", leftLinks.get(leftLinkPids[0]).pid());
                    else if (opDirect == 3)
                        rdSe.changedFields().put("outLinkPid", rightLinks.get(leftLinkPids[0]).pid());
                }
                result.insertObject(rdSe, ObjStatus.UPDATE, rdSe.pid());
            }

            if (firstLink.pid() == endLink.pid())
                return "";

            // 3.最后一条分离Link相关的分叉口提示
            rdSes = selector.loadRdSesWithLinkPid(endLink.getPid(), false);
            for (RdSe rdSe : rdSes) {
                if (nodePids.contains(rdSe.getNodePid()))
                    continue;

                int opDirect = maps.get(firstLink.pid());
                if (firstLink.pid() == rdSe.getInLinkPid()) {
                    if (opDirect == 2)
                        rdSe.changedFields().put("inLinkPid", rightLinks.get(leftLinkPids[length - 1]).pid());
                    else if (opDirect == 3)
                        rdSe.changedFields().put("inLinkPid", leftLinks.get(leftLinkPids[length - 1]).pid());
                } else if (firstLink.pid() == rdSe.getOutLinkPid()) {
                    if (opDirect == 2)
                        rdSe.changedFields().put("outLinkPid", leftLinks.get(leftLinkPids[length - 1]).pid());
                    else if (opDirect == 3)
                        rdSe.changedFields().put("outLinkPid", rightLinks.get(leftLinkPids[length - 1]).pid());
                }
                result.insertObject(rdSe, ObjStatus.UPDATE, rdSe.pid());
            }
        }
        return "";
    }
}
