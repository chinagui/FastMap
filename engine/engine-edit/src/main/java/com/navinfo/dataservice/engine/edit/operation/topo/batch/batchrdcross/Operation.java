package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdcross;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;

import java.sql.Connection;
import java.util.*;

/**
 * Created by chaixin on 2016/10/12 0012.
 */
public class Operation implements IOperation {

    private Command command;

    private Connection conn;

    public Operation(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        RdCross cross = command.getRdCross();
        this.updateRdCrossLink(cross, result);
        this.updateRdCrossNode(cross, result);
        this.updateRdCross(cross, result);
        result.setPrimaryPid(cross.pid());
        return null;
    }

    /**
     * 维护RdCrossNode信息
     *
     * @param cross  目标RdCross
     * @param result 结果集
     */
    private void updateRdCrossNode(RdCross cross, Result result) {
        // 组成node点集合
        List<Integer> newPids = this.command.getNodePids();
        // 待删除RdCrossNode集合
        List<RdCrossNode> delNodes = new ArrayList<>();

        // 是否主点被删除
        boolean isMain = false;
        // 生成待删除集合并将已存在node点从node点集合中去除
        for (IRow row : cross.getNodes()) {
            RdCrossNode node = (RdCrossNode) row;
            if (newPids.contains(node.getNodePid())) {
                newPids.remove((Object) node.getNodePid());
            } else {
                if (!isMain && 1 == node.getIsMain())
                    isMain = true;
                delNodes.add(node);
            }
        }

        // 删除RdCrossNode
        for (RdCrossNode node : delNodes) {
            result.insertObject(node, ObjStatus.DELETE, node.getPid());
        }

        // 生成新的RdCrossNode
        for (Integer nodePid : newPids) {
            RdCrossNode node = new RdCrossNode();
            node.setPid(cross.pid());
            node.setNodePid(nodePid);
            if (isMain) {
                node.setIsMain(1);
                isMain = false;
            }
            result.insertObject(node, ObjStatus.INSERT, node.getPid());
        }

    }

    /**
     * 维护RdCrossLink信息
     *
     * @param cross  目标RdCross
     * @param result 结果集
     * @throws Exception
     */
    private void updateRdCrossLink(RdCross cross, Result result) throws Exception {
        // 组成node点集合
        List<Integer> nodePids = this.command.getNodePids();
        // 查找node点关联的link集合
        RdLinkSelector selector = new RdLinkSelector(this.conn);
        Map<Integer, RdLink> map = new HashMap<>();
        List<RdLink> newLinks = selector.loadByNodePids(nodePids, true);
        // 去除首尾点不同时包含在node点集合的link
        // 组成待处理link的集合
        Iterator<RdLink> it = newLinks.iterator();
        while (it.hasNext()) {
            RdLink link = it.next();
            if (!nodePids.contains(link.getsNodePid()) || !nodePids.contains(link.geteNodePid())) {
                it.remove();
            } else {
                map.put(link.pid(), link);
            }
        }

        // 生成待删除RdCrossLink集合
        // 待生成集合中去除已经存在的link
        List<RdCrossLink> delLinks = new ArrayList<>();
        for (IRow row : cross.getLinks()) {
            RdCrossLink crossLink = (RdCrossLink) row;
            if (map.containsKey(crossLink.getLinkPid())) {
                map.remove(crossLink.getLinkPid());
            } else {
                delLinks.add(crossLink);
            }
        }

        // 删除RdCrossLink
        for (RdCrossLink crossLink : delLinks) {
            result.insertObject(crossLink, ObjStatus.DELETE, cross.pid());
        }

        // 创建RdCrossLink
        for (Integer linkPid : map.keySet()) {
            RdLink link = map.get(linkPid);
            RdCrossLink crossLink = new RdCrossLink();
            crossLink.setLinkPid(linkPid);
            crossLink.setPid(cross.pid());
            result.insertObject(crossLink, ObjStatus.INSERT, cross.pid());

            // 维护link的linkForm信息
            this.updateLinkForm(cross, linkPid, result);
        }
    }

    private void updateRdCross(RdCross cross, Result result) {
        if (command.getNodePids().size() > 1) {
            if (cross.getType() == 0) {
                cross.changedFields().put("type", 1);
                result.insertObject(cross, ObjStatus.UPDATE, cross.pid());
            }
        } else {
            if (cross.getType() == 1) {
                cross.changedFields().put("type", 0);
                result.insertObject(cross, ObjStatus.UPDATE, cross.pid());
            }
        }
    }

    /**
     * 维护link的LinkFrom子表信息
     *
     * @param cross   目标对象
     * @param linkPid 待维护link
     * @param result  结果集
     * @throws Exception
     */
    private void updateLinkForm(RdCross cross, int linkPid, Result result) throws Exception {
        //维护道路形态
        List<IRow> forms = new AbstractSelector(RdLinkForm.class, conn).loadRowsByParentId(linkPid, true);
        boolean needAdd = true;
        IRow editRow = null;
        for (IRow formrow : forms) {
            RdLinkForm form = (RdLinkForm) formrow;
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
                form.setFormOfWay(50);
                form.setLinkPid(linkPid);
                result.insertObject(form, ObjStatus.INSERT, cross.pid());
            }
        }
    }
}
