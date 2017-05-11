package com.navinfo.dataservice.engine.edit.operation.topo.batch.delete.rdnode;

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

public class Operation implements IOperation {

    private Connection conn;

    private List<Integer> delLinkPids = new ArrayList<>();

    private List<Integer> delNodePids = new ArrayList<>();

    private List<RdLink> delLinks = new ArrayList<>();

    public Operation(Command command, Connection conn) {

        HashSet<Integer> tmp = new HashSet<>(command.getNodePids());

        this.delNodePids.addAll(tmp);

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

        handleRdNode(result);

        handleRdLink(result);
    }

    private void handleRdNode(Result result) throws Exception {

        RdNodeSelector nodeSelector = new RdNodeSelector(conn);

        List<IRow> nodeRows = nodeSelector.loadByIds(delNodePids, true, true);

        for (IRow row : nodeRows) {

            RdNode node = (RdNode) row;

            result.insertObject(node, ObjStatus.DELETE, node.getPid());
        }
    }

    private void handleRdLink(Result result) throws Exception {

        RdLinkSelector linkSelector = new RdLinkSelector(conn);

        HashSet<Integer> tmp = new HashSet<>();

        for (int nodePid : this.delNodePids) {

            List<Integer> linkPids = linkSelector.loadLinkPidByNodePid(nodePid, false);

            tmp.addAll(linkPids);
        }

        delLinkPids.clear();

        delLinkPids.addAll(tmp);

        List<IRow> linkRows = linkSelector.loadByIds(delLinkPids, true, true);

        for (IRow row : linkRows) {

            RdLink link = (RdLink) row;

            delLinks.add(link);

            result.insertObject(link, ObjStatus.DELETE, link.getPid());
        }
    }

    private void handleRelationObj(Result result) throws Exception {

        RelationshipUtils relationshipUtils = new RelationshipUtils(this.conn, result);

        relationshipUtils.handleRelationObj(this.delLinkPids, this.delNodePids, this.delLinks);
    }

}