package com.navinfo.dataservice.engine.edit.operation.obj.rdcross.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;

public class Operation implements IOperation {

    private Command command;

    private Connection conn;

    public Operation(Command command, Connection conn) {
        this.command = command;

        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {

        int meshId = new RdNodeSelector(conn).loadById(command.getNodePids().get(0), true).mesh();

        RdCross cross = new RdCross();

        cross.setMesh(meshId);

        cross.setPid(PidUtil.getInstance().applyRdCrossPid());

        result.setPrimaryPid(cross.getPid());

        if (command.getNodePids().size() > 1) {
            cross.setType(1);
        }

        List<IRow> nodes = new ArrayList<IRow>();

        List<Integer> nodePids = command.getNodePids();

        boolean flag = true;

        for (int i = 0; i < nodePids.size(); i++) {

            int nodePid = nodePids.get(i);

            RdCrossNode node = new RdCrossNode();

            node.setPid(cross.getPid());

            node.setNodePid(nodePid);

            if (nodePids.size() == 1) {
                node.setIsMain(1);
            } else {
                if (flag) {
                    flag = this.updateIsMain(node);
                }
            }

            node.setMesh(meshId);

            nodes.add(node);
        }

        if (!nodes.isEmpty() && flag) {
            RdCrossNode node = (RdCrossNode) nodes.get(0);
            node.setIsMain(1);
        }

        cross.setNodes(nodes);

        List<IRow> links = new ArrayList<IRow>();

        List<Integer> linkPids = command.getLinkPids();

        for (int i = 0; i < linkPids.size(); i++) {

            int linkPid = linkPids.get(i);

            RdCrossLink link = new RdCrossLink();

            link.setPid(cross.getPid());

            link.setLinkPid(linkPid);

            link.setMesh(meshId);

            links.add(link);

            //维护道路形态
            List<IRow> forms = new AbstractSelector(RdLinkForm.class, conn).loadRowsByParentId(linkPid, true);

            boolean needAdd = true;

            IRow editRow = null;

            for (IRow formrow : forms) {

                RdLinkForm form = (RdLinkForm) formrow;

                form.setMesh(meshId);

                if (form.getFormOfWay() == 33) {//环岛
                    needAdd = false;
                } else if (form.getFormOfWay() == 1) {
                    form.changedFields().put("formOfWay", 50);

                    editRow = form;
                }
            }

            if (needAdd) {
                if (editRow != null) {
                    result.insertObject(editRow, ObjStatus.UPDATE, linkPid);
                } else {
                    RdLinkForm form = new RdLinkForm();

                    form.setMesh(meshId);

                    form.setFormOfWay(50);

                    form.setLinkPid(linkPid);

                    result.insertObject(form, ObjStatus.INSERT, cross.pid());
                }
            }
        }

        cross.setLinks(links);

        result.insertObject(cross, ObjStatus.INSERT, cross.pid());

        return null;
    }

    /**
     * 判断路口点挂接线是否有辅路属性，无则设置为路口主点
     *
     * @param node 路口点
     * @return
     * @throws Exception
     */
    private boolean updateIsMain(RdCrossNode node) throws Exception {
        boolean flag = true;
        RdLinkSelector selector = new RdLinkSelector(conn);
        List<RdLink> links = selector.loadByNodePid(node.getNodePid(), true);
        for (RdLink link : links) {
            for (IRow row : link.getForms()) {
                RdLinkForm form = (RdLinkForm) row;
                if (form.getFormOfWay() == 34) {
                    flag = false;
                    break;
                }
            }
        }
        if (flag) {
            node.setIsMain(1);
            return false;
        }
        return true;
    }
}
