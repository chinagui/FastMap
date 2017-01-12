package com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgateName;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgatePassage;
import com.navinfo.dataservice.dao.glm.selector.rd.tollgate.RdTollgateSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author zhangyt
 * @Title: Operation.java
 * @Description: TODO
 * @date: 2016年8月10日 下午2:41:31
 * @version: v1.0
 */
public class Operation implements IOperation {

    private Command command;

    private Connection conn;

    public Operation(Command command) {
        this.command = command;
    }

    public Operation(Connection conn) {
        this.conn = conn;
    }

    public Operation(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        RdTollgate tollgate = this.command.getTollgate();
        JSONObject content = this.command.getContent();
        boolean isChange = tollgate.fillChangeFields(content);
        if (isChange) {
            result.insertObject(tollgate, ObjStatus.UPDATE, tollgate.pid());
        }
        result.setPrimaryPid(tollgate.pid());

        if (content.containsKey("passages")) {
            updatePassage(result, content.getJSONArray("passages"));
        }

        if (content.containsKey("names")) {
            updateName(result, content.getJSONArray("names"));
        }

        return null;
    }

    private void updatePassage(Result result, JSONArray array) throws Exception {
        Iterator<JSONObject> iterator = array.iterator();
        RdTollgatePassage passage = null;
        JSONObject jsonPassage = null;
        while (iterator.hasNext()) {
            jsonPassage = iterator.next();
            if (jsonPassage.containsKey("objStatus")) {
                String objStatus = jsonPassage.getString("objStatus");
                if (ObjStatus.UPDATE.toString().equals(objStatus)) {
                    passage = this.command.getTollgate().tollgatePassageMap.get(jsonPassage.getString("rowId"));
                    boolean isChange = passage.fillChangeFields(jsonPassage);
                    if (isChange) {
                        result.insertObject(passage, ObjStatus.UPDATE, passage.getPid());
                    }
                } else if (ObjStatus.DELETE.toString().equals(objStatus)) {
                    passage = this.command.getTollgate().tollgatePassageMap.get(jsonPassage.getString("rowId"));
                    result.insertObject(passage, ObjStatus.DELETE, passage.getPid());
                } else if (ObjStatus.INSERT.toString().equals(objStatus)) {
                    passage = new RdTollgatePassage();
                    passage.Unserialize(jsonPassage);
                    passage.setPid(this.command.getTollgate().getPid());
                    result.insertObject(passage, ObjStatus.INSERT, passage.getPid());
                }
            }
        }

    }

    private void updateName(Result result, JSONArray array) throws Exception {
        Iterator<JSONObject> iterator = array.iterator();
        RdTollgateName name = null;
        JSONObject jsonName = null;
        while (iterator.hasNext()) {
            jsonName = iterator.next();
            if (jsonName.containsKey("objStatus")) {
                String objStatus = jsonName.getString("objStatus");
                if (ObjStatus.UPDATE.toString().equals(objStatus)) {
                    name = this.command.getTollgate().tollgateNameMap.get(jsonName.getString("rowId"));
                    boolean isChange = name.fillChangeFields(jsonName);
                    if (isChange) {
                        result.insertObject(name, ObjStatus.UPDATE, name.getPid());
                    }
                } else if (ObjStatus.DELETE.toString().equals(objStatus)) {
                    name = this.command.getTollgate().tollgateNameMap.get(jsonName.getString("rowId"));
                    result.insertObject(name, ObjStatus.DELETE, name.getPid());
                } else if (ObjStatus.INSERT.toString().equals(objStatus)) {
                    name = new RdTollgateName();
                    name.Unserialize(jsonName);
                    name.setNameId(PidUtil.getInstance().applyRdTollgateNamePid());
                    name.setPid(this.command.getTollgate().getPid());
                    result.insertObject(name, ObjStatus.INSERT, name.getPid());
                }
            }
        }
    }

    /**
     * 根据被删除的RdLink的Pid、新生成的RdLink<br>
     * 维护原RdLink上关联的收费站
     *
     * @param result     待处理的结果集
     * @param oldLinkPid 被删除RdLink的Pid
     * @param newLinks   新生成的RdLink的集合
     * @return
     * @throws Exception
     */
    public String breakRdTollgate(Result result, int oldLinkPid, List<RdLink> newLinks) throws Exception {
        RdTollgateSelector selector = new RdTollgateSelector(this.conn);
        // 查询所有与被删除RdLink关联的收费站
        List<RdTollgate> rdTollgates = selector.loadRdTollgatesWithLinkPid(
                oldLinkPid, true);
        // 循环处理每一个收费站
        for (RdTollgate rdTollgate : rdTollgates) {
            // 收费站的进入点的Pid
            int nodePid = rdTollgate.getNodePid();
            // 处理进入线被打断的收费站
            if (oldLinkPid == rdTollgate.getInLinkPid()) {
                for (RdLink link : newLinks) {
                    if (nodePid == link.geteNodePid() || nodePid == link.getsNodePid()) {
                        // 如果新生成线的终点的Pid与收费站的nodePid相等
                        // 则该新生成线为进入线，修改进入线Pid
                        rdTollgate.changedFields().put("inLinkPid", link.pid());
                        break;
                    }
                }
            } else {
                for (RdLink link : newLinks) {
                    if (nodePid == link.getsNodePid() || nodePid == link.geteNodePid()) {
                        // 如果新生成线的起点的Pid与收费站的nodePid相等
                        // 则该新生成线为退出线，修改退出线Pid
                        rdTollgate.changedFields().put("outLinkPid", link.pid());
                        break;
                    }
                }
            }
            // 将需要修改的收费站放入结果集中
            result.insertObject(rdTollgate, ObjStatus.UPDATE, rdTollgate.pid());
        }
        return null;
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

        List<Integer> nodePids = new ArrayList<Integer>();

        nodePids.add(nodePid);

        departNode(link, nodePids, rdlinks, result);
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
    public void departNode(RdLink link, List<Integer> nodePids,
                           List<RdLink> rdlinks, Result result) throws Exception {

        int linkPid = link.getPid();

        // 跨图幅处理的link为进入线的RdTollgate
        Map<Integer, RdTollgate> tollgateInLink = null;

        // 跨图幅处理的link为退出线的RdTollgate
        Map<Integer, RdTollgate> tollgateOutLink = null;

        if (rdlinks != null && rdlinks.size() > 1) {

            tollgateInLink = new HashMap<Integer, RdTollgate>();

            tollgateOutLink = new HashMap<Integer, RdTollgate>();
        }

        RdTollgateSelector selector = new RdTollgateSelector(this.conn);

        // 在link上的RdTollgate
        List<RdTollgate> tollgates = selector.loadRdTollgatesWithLinkPid(
                linkPid, true);

        for (int nodePid : nodePids) {
            for (RdTollgate tollgate : tollgates) {
                if (tollgate.getNodePid() == nodePid) {
                    result.insertObject(tollgate, ObjStatus.DELETE,
                            tollgate.getPid());
                } else if (tollgateInLink != null
                        && tollgate.getInLinkPid() == linkPid) {
                    tollgateInLink.put(tollgate.getPid(), tollgate);
                } else if (tollgateOutLink != null
                        && tollgate.getOutLinkPid() == linkPid) {
                    tollgateOutLink.put(tollgate.getPid(), tollgate);
                }
            }

            if (tollgateOutLink == null || tollgateInLink == null) {

                return;
            }

            int connectNode = link.getsNodePid() == nodePid ? link
                    .geteNodePid() : link.getsNodePid();

            for (RdLink rdlink : rdlinks) {

                if (rdlink.getsNodePid() != connectNode
                        && rdlink.geteNodePid() != connectNode) {

                    continue;
                }

                for (RdTollgate tollgate : tollgateInLink.values()) {

                    tollgate.changedFields().put("inLinkPid", rdlink.getPid());

                    result.insertObject(tollgate, ObjStatus.UPDATE,
                            tollgate.pid());
                }

                for (RdTollgate tollgate : tollgateOutLink.values()) {

                    tollgate.changedFields().put("outLinkPid", rdlink.getPid());

                    result.insertObject(tollgate, ObjStatus.UPDATE,
                            tollgate.pid());
                }
            }
        }
    }
}
