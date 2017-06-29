package com.navinfo.dataservice.engine.edit.operation.topo.batch.delete.rdlink;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.engine.edit.utils.RelationshipUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Operation implements IOperation {

    private Connection conn;

    private List<Integer> delLinkPids = new ArrayList<>();

    private List<Integer> delNodePids = new ArrayList<>();

    private List<RdLink> delLinks = new ArrayList<>();

    public Operation(Command command, Connection conn) {

        this.delLinkPids = command.getLinkPids();

        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {

        batchDel(result);

        return null;
    }

    private void batchDel(Result result) throws Exception {

        pretreatment(result);

        handleRelationObj(result);
    }

    private void pretreatment(Result result) throws Exception {

        handleRdLink(result);

        handleRdNode(result);
    }

    private void handleRdLink(Result result) throws Exception {

        RdLinkSelector linkSelector = new RdLinkSelector(conn);

        List<IRow> linkRows = linkSelector.loadByIds(delLinkPids, true, true);

        delLinkPids.clear();

        for (IRow row : linkRows) {

            RdLink link = (RdLink) row;

            delLinks.add(link);

            delLinkPids.add(link.getPid());

            result.insertObject(link, ObjStatus.DELETE, link.getPid());
        }
    }

    private void handleRdNode(Result result) throws Exception {

        RdLinkSelector linkSelector = new RdLinkSelector(conn);

        List<Integer> nodeTmp = new ArrayList<>();

        // 被删link的所有端点node
        for (RdLink link : delLinks) {

            if (!nodeTmp.contains(link.geteNodePid())) {

                nodeTmp.add(link.geteNodePid());
            }
            if (!nodeTmp.contains(link.getsNodePid())) {

                nodeTmp.add(link.getsNodePid());
            }

        }

        // 所有端点node挂接的全部link
        List<RdLink> linkTotal = linkSelector.loadByNodePids(nodeTmp, true);

        // 不删的node
        Set<Integer> noDelNodes = new HashSet<>();

        for (RdLink link : linkTotal)
            if (!delLinkPids.contains(link.getPid())) {

                noDelNodes.add(link.getsNodePid());

                noDelNodes.add(link.geteNodePid());
            }

        // 获取需要删除的node
        nodeTmp.removeAll(noDelNodes);

        RdNodeSelector nodeSelector = new RdNodeSelector(conn);

        List<IRow> nodeRows = nodeSelector.loadByIds(nodeTmp, true, true);

        for (IRow row : nodeRows) {

            RdNode node = (RdNode) row;

            delNodePids.add(node.getPid());

            result.insertObject(node, ObjStatus.DELETE, node.getPid());
        }
    }

    private void handleRelationObj(Result result) throws Exception {

        RelationshipUtils relationshipUtils = new RelationshipUtils(this.conn, result);

        relationshipUtils.handleRelationObj(this.delLinkPids, this.delNodePids, this.delLinks);
    }
}