package com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.update;

import java.sql.Connection;
import java.util.*;

import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.warninginfo.RdWarninginfo;
import com.navinfo.dataservice.dao.glm.selector.rd.warninginfo.RdWarninginfoSelector;

public class Operation implements IOperation {

    private Command command;

    private Connection conn = null;

    private RdWarninginfo rdWarninginfo;

    public Operation(Command command) {
        this.command = command;

        this.rdWarninginfo = this.command.getRdWarninginfo();

    }

    public Operation(Connection conn) {
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {

        String msg = update(result);

        return msg;
    }

    private String update(Result result) throws Exception {
        JSONObject content = command.getContent();

        if (!content.containsKey("objStatus")) {

            return null;
        }

        if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
            result.insertObject(rdWarninginfo, ObjStatus.DELETE,
                    rdWarninginfo.pid());
            return null;
        }

        boolean isChanged = rdWarninginfo.fillChangeFields(content);

        if (isChanged) {
            result.insertObject(rdWarninginfo, ObjStatus.UPDATE,
                    rdWarninginfo.pid());
        }

        return null;
    }

    /**
     * 打断link维护警示信息
     *
     * @param nodeLinkRelation 平滑修行分离点挂接线信息
     * @param linkPid          被打断的link
     * @param newLinks         新生成的link组
     * @param result
     * @throws Exception
     */
    public void breakRdLink(Map<RdNode, List<RdLink>> nodeLinkRelation, int linkPid, List<RdLink> newLinks, Result result)
            throws Exception {
        if (conn == null) {
            return;
        }
        RdWarninginfoSelector selector = new RdWarninginfoSelector(conn);

        List<RdWarninginfo> warninginfos = selector.loadByLink(linkPid, true);

        if (null != nodeLinkRelation && !nodeLinkRelation.isEmpty()) {
            List<Integer> catchIds = new ArrayList<>();
            for (Map.Entry<RdNode, List<RdLink>> entry : nodeLinkRelation.entrySet()) {
                if (entry.getValue().size() > 1) {
                    catchIds.add(entry.getKey().pid());
                }
            }
            List<RdWarninginfo> infos = selector.loadByNodePids(catchIds, true);
            catchIds = new ArrayList<>();
            for (RdWarninginfo info : infos) {
                catchIds.add(info.pid());
                result.insertObject(info, ObjStatus.DELETE, info.pid());
            }
            if (!catchIds.isEmpty()) {
                Iterator<RdWarninginfo> iterator = warninginfos.iterator();
                while (iterator.hasNext()) {
                    RdWarninginfo warninginfo = iterator.next();
                    if (catchIds.contains(warninginfo.pid())) {
                        iterator.remove();
                    }
                }
            }
        }

        for (RdWarninginfo warninginfo : warninginfos) {
            for (RdLink link : newLinks) {
                if (link.getsNodePid() == warninginfo.getNodePid()
                        || link.geteNodePid() == warninginfo.getNodePid()) {
                    warninginfo.changedFields().put("linkPid", link.getPid());
                    result.insertObject(warninginfo, ObjStatus.UPDATE,
                            warninginfo.pid());
                }
            }
        }
    }

    /**
     * 分离节点
     *
     * @param link
     * @param nodePid
     * @param rdlinks
     * @param result
     * @throws Exception
     */
    public void departNode(RdLink link, int nodePid, List<RdLink> rdlinks,
                           Result result) throws Exception {

        int linkPid = link.getPid();

        // 跨图幅处理的RdWarninginfo
        Map<Integer, RdWarninginfo> warninginfoMesh = null;

        if (rdlinks != null && rdlinks.size() > 1) {

            warninginfoMesh = new HashMap<Integer, RdWarninginfo>();
        }

        RdWarninginfoSelector selector = new RdWarninginfoSelector(conn);

        List<RdWarninginfo> warninginfos = selector.loadByLink(linkPid, true);

        for (RdWarninginfo warninginfo : warninginfos) {

            if (warninginfo.getNodePid() == nodePid) {

                result.insertObject(warninginfo, ObjStatus.DELETE,
                        warninginfo.getPid());

            } else if (warninginfoMesh != null) {

                warninginfoMesh.put(warninginfo.getPid(), warninginfo);
            }
        }

        if (warninginfoMesh == null) {

            return;
        }

        int connectNode = link.getsNodePid() == nodePid ? link.geteNodePid()
                : link.getsNodePid();

        for (RdLink rdlink : rdlinks) {

            if (rdlink.getsNodePid() != connectNode
                    && rdlink.geteNodePid() != connectNode) {

                continue;
            }
            for (RdWarninginfo warninginfo : warninginfoMesh.values()) {

                warninginfo.changedFields().put("linkPid", rdlink.getPid());

                result.insertObject(warninginfo, ObjStatus.UPDATE, warninginfo.pid());
            }
        }
    }
}
