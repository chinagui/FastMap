package com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;

public class Operation implements IOperation {

    private Command command;

    private RdRestriction restrict;

    private Connection conn;

    public Operation(Connection conn) {

        this.conn = conn;
    }

    public Operation(Command command, Connection conn, RdRestriction restrict) {
        this.command = command;

        this.conn = conn;

        this.restrict = restrict;

    }

    @Override
    public String run(Result result) throws Exception {

        JSONObject content = command.getContent();

        // 判断是否存在交限进入线
        if (content.containsKey("objStatus")) {
            //修改主表属性
            boolean isChanged = restrict.fillChangeFields(content);

            if (isChanged) {
                result.insertObject(restrict, ObjStatus.UPDATE, restrict.pid());
            }
        }

        //修改拓补结构数据
        if (content.containsKey("details")) {
            JSONArray details = content.getJSONArray("details");

            for (int i = 0; i < details.size(); i++) {

                JSONObject detailJson = details.getJSONObject(i);

                //定义交限详细信息PID，新增经过线，交限详细信息也新增的话取该pid赋值给经过线对象
                int detailId = 0;

                if (detailJson.containsKey("objStatus")) {

                    if (!ObjStatus.INSERT.toString().equals(detailJson.getString("objStatus"))) {

                        RdRestrictionDetail detail = restrict.detailMap.get(detailJson.getInt("pid"));

                        if (detail == null) {
                            throw new Exception("detailId=" + detailJson.getInt("pid") + "的交限detail不存在");
                        }
                        //删除拓补结构
                        if (ObjStatus.DELETE.toString().equals(detailJson.getString("objStatus"))) {
                            result.insertObject(detail, ObjStatus.DELETE, restrict.pid());
                            continue;
                        } else if (ObjStatus.UPDATE.toString().equals(detailJson.getString("objStatus"))) {
                            detail.fillChangeFields(detailJson);
                            //修改拓补结构，修改退出线，需要重新计算关系类型
                            if (detail.changedFields().containsKey("outLinkPid")) {
                                int inNodePid = restrict.getNodePid();

                                int outLinkPid = detailJson.getInt("outLinkPid");

                                CalLinkOperateUtils calLinkOperateUtils = new CalLinkOperateUtils();

                                int relationShipType = calLinkOperateUtils.getRelationShipType(conn, inNodePid,
                                        outLinkPid);

                                detail.changedFields().put("relationshipType", relationShipType);
                            }
                            if (!detail.changedFields().isEmpty()) {
                                result.insertObject(detail, ObjStatus.UPDATE, restrict.pid());
                            }
                        }
                    } else {
                        //新增拓补结构
                        RdRestrictionDetail detail = new RdRestrictionDetail();

                        detail.Unserialize(detailJson);

                        detail.setPid(PidUtil.getInstance().applyRestrictionDetailPid());

                        detailId = detail.getPid();

                        detail.setRestricPid(restrict.getPid());

                        CalLinkOperateUtils calLinkOperateUtils = new CalLinkOperateUtils();

                        int relationShipType = calLinkOperateUtils.getRelationShipType(conn, restrict.getNodePid(),
                                detail.getOutLinkPid());
                        //线线关系类型的，记录经过线
                        if (relationShipType == 2) {

                            List<IRow> vias = new ArrayList<IRow>();

                            List<Integer> viaLinkPids = calLinkOperateUtils.calViaLinks(conn, restrict.getInLinkPid(),
                                    restrict.getNodePid(), detail.getOutLinkPid());

                            if (CollectionUtils.isNotEmpty(viaLinkPids)) {
                                for (int j = 0; j < viaLinkPids.size(); j++) {
                                    RdRestrictionVia via = new RdRestrictionVia();

                                    via.setDetailId(detail.getPid());

                                    via.setSeqNum(j + 1);

                                    via.setLinkPid(viaLinkPids.get(j));

                                    vias.add(via);
                                }
                            }

                            detail.setVias(vias);
                        }

                        detail.setRelationshipType(relationShipType);

                        detail.setMesh(restrict.mesh());

                        result.insertObject(detail, ObjStatus.INSERT, restrict.getPid());

//						continue;
                    }
                }
                //修改拓补结构，新增或者修改经过线
                if (detailJson.containsKey("vias")) {

                    RdRestrictionDetail detail = restrict.detailMap.get(detailJson.getInt("pid"));

                    JSONArray vias = detailJson.getJSONArray("vias");

                    for (int j = 0; j < vias.size(); j++) {

                        JSONObject viajson = vias.getJSONObject(j);

                        if (viajson.containsKey("objStatus")) {

                            if (!ObjStatus.INSERT.toString().equals(viajson.getString("objStatus"))) {

                                RdRestrictionVia via = detail.viaMap.get(viajson.getString("rowId"));

                                if (via == null) {
                                    throw new Exception(
                                            "rowId=" + viajson.getString("rowId") + "的rd_restriction_via不存在");
                                }

                                if (ObjStatus.DELETE.toString().equals(viajson.getString("objStatus"))) {
                                    result.insertObject(via, ObjStatus.DELETE, restrict.pid());

                                    continue;
                                } else if (ObjStatus.UPDATE.toString().equals(viajson.getString("objStatus"))) {

                                    boolean isChanged = via.fillChangeFields(viajson);

                                    if (isChanged) {
                                        result.insertObject(via, ObjStatus.UPDATE, restrict.pid());
                                    }
                                }
                            } else {
                                RdRestrictionVia via = new RdRestrictionVia();

                                via.setSeqNum(viajson.getInt("seqNum"));

                                via.setGroupId(viajson.getInt("groupId"));

                                via.setLinkPid(viajson.getInt("linkPid"));

                                //赋值经过线的detailId,分三种情况：1.前台传递 2.后台从拓补信息中获取3.新增拓补后生成的detialId
                                if (viajson.containsKey("detailId") && viajson.getInt("detailId") != 0) {
                                    via.setDetailId(viajson.getInt("detailId"));
                                } else if (detailJson.containsKey("pid") && detailJson.getInt("pid") != 0) {
                                    via.setDetailId(detailJson.getInt("pid"));
                                } else {
                                    via.setDetailId(detailId);
                                }
                                result.insertObject(via, ObjStatus.INSERT, restrict.pid());
                                continue;
                            }
                        }

                    }
                }

                //卡车交限
                if (detailJson.containsKey("conditions")) {

                    int conditionDetailId = detailJson.getInt("pid");

                    if (conditionDetailId == 0)
                        conditionDetailId = detailId;

                    JSONArray conds = detailJson.getJSONArray("conditions");

                    for (int j = 0; j < conds.size(); j++) {
                        JSONObject cond = conds.getJSONObject(j);

                        if (!cond.containsKey("objStatus")) {
                            throw new Exception("传入请求内容格式错误，conditions不存在操作类型objType");
                        }

                        if (!ObjStatus.INSERT.toString().equals(cond.getString("objStatus"))) {

                            RdRestrictionCondition condition = restrict.conditionMap.get(cond.getString("rowId"));

                            if (condition == null) {
                                throw new Exception("rowId=" + cond.getString("rowId") + "的交限condition不存在");
                            }

                            if (ObjStatus.DELETE.toString().equals(cond.getString("objStatus"))) {
                                result.insertObject(condition, ObjStatus.DELETE, restrict.pid());

                            } else if (ObjStatus.UPDATE.toString().equals(cond.getString("objStatus"))) {

                                boolean isChanged = condition.fillChangeFields(cond);

                                if (isChanged) {
                                    result.insertObject(condition, ObjStatus.UPDATE, restrict.pid());
                                }
                            }
                        } else {
                            RdRestrictionCondition condition = new RdRestrictionCondition();

                            condition.Unserialize(cond);

                            condition.setDetailId(conditionDetailId);

                            result.insertObject(condition, ObjStatus.INSERT, restrict.pid());
                        }
                    }
                }
            }
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
    public void departNode(RdLink link, int nodePid, List<RdLink> rdlinks, Result result) throws Exception {

        int linkPid = link.getPid();

        // 需要分离节点处理的RdRestriction
        Map<Integer, RdRestriction> restrictionDepart = new HashMap<Integer, RdRestriction>();

        // 需要分离节点处理的RdRestrictionDetail
        Map<Integer, RdRestrictionDetail> detailDepart = new HashMap<Integer, RdRestrictionDetail>();

        // 分离节点不处理，跨图幅打断需要处理的RdRestriction
        Map<Integer, RdRestriction> restrictionMesh = null;

        // 分离节点不处理，跨图幅打断需要处理的RdRestrictionDetail
        Map<Integer, RdRestrictionDetail> detailMesh = null;

        if (rdlinks != null && rdlinks.size() > 1) {

            restrictionMesh = new HashMap<Integer, RdRestriction>();

            detailMesh = new HashMap<Integer, RdRestrictionDetail>();
        }

        RdRestrictionSelector selector = new RdRestrictionSelector(this.conn);

        // link作为进入线的RdRestriction
        List<RdRestriction> restrictions = selector.loadByLink(linkPid, 1, true);
        getInLinkDepartInfo(nodePid, restrictions, restrictionDepart, restrictionMesh);

        // link作为退出线的RdRestriction
        restrictions = selector.loadByLink(linkPid, 2, true);

        Map<Integer, RdRestrictionDetail> detailTmp = new HashMap<Integer, RdRestrictionDetail>();

        getOutLinkDepartInfo(nodePid, linkPid, restrictions, detailTmp, detailMesh);

        for (RdRestriction restriction : restrictions) {

            if (!detailTmp.containsKey(restriction.getPid())) {

                continue;
            }

            if (restriction.getDetails().size() > 1) {

                RdRestrictionDetail delDetail = detailTmp.get(restriction.getPid());

                detailDepart.put(delDetail.getPid(), delDetail);

            } else {

                restrictionDepart.put(restriction.getPid(), restriction);
            }
        }

        for (RdRestrictionDetail delDetail : detailDepart.values()) {

            result.insertObject(delDetail, ObjStatus.DELETE, delDetail.pid());
        }

        for (RdRestriction restriction : restrictionDepart.values()) {

            result.insertObject(restriction, ObjStatus.DELETE, restriction.pid());
        }

        if (restrictionMesh == null || detailMesh == null) {

            return;
        }

        int connectNode = link.getsNodePid() == nodePid ? link.geteNodePid() : link.getsNodePid();

        for (RdLink rdlink : rdlinks) {

            if (rdlink.getsNodePid() != connectNode && rdlink.geteNodePid() != connectNode) {

                continue;
            }

            for (RdRestriction restriction : restrictionMesh.values()) {

                restriction.changedFields().put("inLinkPid", rdlink.getPid());

                result.insertObject(restriction, ObjStatus.UPDATE, restriction.pid());
            }

            for (RdRestrictionDetail detail : detailMesh.values()) {

                detail.changedFields().put("outLinkPid", rdlink.getPid());

                result.insertObject(detail, ObjStatus.UPDATE, detail.getRestricPid());
            }
        }
    }

    /**
     * 获取link作为进入线时交限的信息
     *
     * @param nodePid           分离点
     * @param restrictions      link作为进入线的所有RdRestriction
     * @param restrictionDepart 分离点为进入点的交限
     * @param restrictionMesh   分离点不是进入点的交限
     * @throws Exception
     */
    private void getInLinkDepartInfo(int nodePid, List<RdRestriction> restrictions,
                                     Map<Integer, RdRestriction> restrictionDepart, Map<Integer, RdRestriction> restrictionMesh)
            throws Exception {

        for (RdRestriction restriction : restrictions) {

            if (restriction.getNodePid() == nodePid) {
                restrictionDepart.put(restriction.getPid(), restriction);

            } else if (restrictionMesh != null) {

                restrictionMesh.put(restriction.getPid(), restriction);
            }
        }
    }

    /**
     * 获取link作为退出线时交限的信息
     *
     * @param nodePid      分离点
     * @param linkPid      分离线
     * @param restrictions link作为退出线的所有RdRestriction
     * @param detailTmp    分离点为退出线的进入点的交限
     * @param detailMesh   分离点不是退出线的进入点的交限
     * @throws Exception
     */
    private void getOutLinkDepartInfo(int nodePid, int linkPid, List<RdRestriction> restrictions,
                                      Map<Integer, RdRestrictionDetail> detailTmp, Map<Integer, RdRestrictionDetail> detailMesh)
            throws Exception {

        RdLinkSelector rdLinkSelector = new RdLinkSelector(this.conn);

        for (RdRestriction restriction : restrictions) {

            for (IRow rowDetail : restriction.getDetails()) {

                RdRestrictionDetail detail = (RdRestrictionDetail) rowDetail;

                // 排除其他退出线
                if (detail.getOutLinkPid() != linkPid) {

                    continue;
                }

                // 分离node为交限进入点
                if (restriction.getNodePid() == nodePid) {

                    detailTmp.put(detail.getRestricPid(), detail);

                    continue;
                }

                // 无经过线
                if (detail.getVias().size() == 0) {

                    if (detailMesh != null) {

                        detailMesh.put(detail.getRestricPid(), detail);
                    }

                    continue;
                }

                List<Integer> linkPids = new ArrayList<Integer>();

                for (IRow rowVia : detail.getVias()) {

                    RdRestrictionVia via = (RdRestrictionVia) rowVia;

                    if (!linkPids.contains(via.getLinkPid())) {

                        linkPids.add(via.getLinkPid());
                    }
                }

                List<IRow> linkViaRows = rdLinkSelector.loadByIds(linkPids, true, false);

                boolean isConnect = false;

                for (IRow rowLink : linkViaRows) {

                    RdLink rdLink = (RdLink) rowLink;

                    // 经过线挂接与退出线的分离node挂接
                    if (rdLink.geteNodePid() == nodePid || rdLink.getsNodePid() == nodePid) {

                        isConnect = true;

                        break;
                    }
                }

                if (isConnect) {

                    detailTmp.put(detail.getRestricPid(), detail);

                } else if (detailMesh != null) {

                    detailMesh.put(detail.getRestricPid(), detail);
                }
            }
        }
    }


    public void breakRdLink(RdLink deleteLink, List<RdLink> newLinks,
                            Result result) throws Exception {
        if (this.command != null || conn == null) {

            return;
        }

        RdRestrictionSelector selector = new RdRestrictionSelector(conn);

        // link为进入线
        List<RdRestriction> restrictions = selector.loadByLink(
                deleteLink.getPid(), 1, true);

        for (RdRestriction restriction : restrictions) {

            breakInLink(restriction, newLinks, result);
        }

        // link为退出线
        restrictions = selector.loadByLink(deleteLink.getPid(),
                2, true);

        for (RdRestriction restriction : restrictions) {

            breakOutLink(deleteLink, restriction, newLinks, result);
        }

        // link为经过线
        restrictions = selector.loadByLink(deleteLink.getPid(),
                3, true);

        for (RdRestriction restriction : restrictions) {

            breakPassLink(deleteLink, restriction, newLinks, result);
        }

    }

    private void breakInLink(RdRestriction restriction, List<RdLink> newLinks,
                             Result result) {

        for (RdLink link : newLinks) {

            if (link.getsNodePid() == restriction.getNodePid()
                    || link.geteNodePid() == restriction.getNodePid()) {

                restriction.changedFields().put("inLinkPid", link.getPid());

                result.insertObject(restriction, ObjStatus.UPDATE,
                        restriction.pid());

                break;
            }

        }
    }

    private void breakOutLink(RdLink deleteLink, RdRestriction restriction,
                              List<RdLink> newLinks, Result result) throws Exception {

        for (IRow rowDetail : restriction.getDetails()) {

            RdRestrictionDetail detail = (RdRestrictionDetail) rowDetail;

            // 排除其他详细信息
            if (detail.getOutLinkPid() != deleteLink.getPid()) {

                continue;
            }

            int connectionNodePid = 0;

            if (detail.getVias().size() == 0) {

                connectionNodePid = restriction.getNodePid();

            } else {

                RdRestrictionVia lastVia = (RdRestrictionVia) detail.getVias()
                        .get(0);

                for (IRow rowVia : detail.getVias()) {

                    RdRestrictionVia via = (RdRestrictionVia) rowVia;

                    if (lastVia.getGroupId() == via.getGroupId()
                            && lastVia.getSeqNum() < via.getSeqNum()) {

                        lastVia = via;
                    }
                }

                RdLinkSelector rdLinkSelector = new RdLinkSelector(this.conn);

                List<Integer> linkPids = rdLinkSelector.loadLinkPidByNodePid(
                        deleteLink.getsNodePid(), false);

                connectionNodePid = linkPids.contains(lastVia.getLinkPid()) ? deleteLink
                        .getsNodePid() : deleteLink.geteNodePid();
            }

            if (connectionNodePid == 0) {

                continue;
            }

            for (RdLink link : newLinks) {

                if (link.getsNodePid() == connectionNodePid
                        || link.geteNodePid() == connectionNodePid) {

                    detail.changedFields().put("outLinkPid", link.getPid());

                    result.insertObject(detail, ObjStatus.UPDATE,
                            restriction.pid());

                    break;
                }
            }

        }
    }

    private void breakPassLink(RdLink deleteLink, RdRestriction restriction,
                               List<RdLink> newLinks, Result result) throws Exception {

        for (IRow rowDetail : restriction.getDetails()) {

            RdRestrictionDetail detail = (RdRestrictionDetail) rowDetail;

            // 对经过线分组
            Map<Integer, List<RdRestrictionVia>> viaGroupId = new HashMap<Integer, List<RdRestrictionVia>>();

            for (IRow row : detail.getVias()) {

                RdRestrictionVia via = (RdRestrictionVia) row;

                if (viaGroupId.get(via.getGroupId()) == null) {
                    viaGroupId.put(via.getGroupId(),
                            new ArrayList<RdRestrictionVia>());
                }

                viaGroupId.get(via.getGroupId()).add(via);
            }

            // 分组处理经过线
            for (int key : viaGroupId.keySet()) {

                // 经过线组
                List<RdRestrictionVia> viaGroup = viaGroupId.get(key);

                RdRestrictionVia breakVia = null;

                for (RdRestrictionVia via : viaGroup) {

                    if (via.getLinkPid() == deleteLink.getPid()) {

                        breakVia = via;

                        break;
                    }
                }

                // 经过线组的link未被打断，不处理
                if (breakVia == null) {

                    continue;
                }

                // 与进入线或前一个经过线的连接点
                int connectionNodePid = 0;

                // 打断的是第一个经过线link
                if (breakVia.getSeqNum() == 1) {

                    connectionNodePid = restriction.getNodePid();

                } else {

                    int preLinkPid = 0;

                    for (RdRestrictionVia via : viaGroup) {

                        if (via.getSeqNum() == breakVia.getSeqNum() - 1) {

                            preLinkPid = via.getLinkPid();

                            break;
                        }
                    }

                    RdLinkSelector rdLinkSelector = new RdLinkSelector(
                            this.conn);

                    List<Integer> linkPids = rdLinkSelector
                            .loadLinkPidByNodePid(deleteLink.getsNodePid(),
                                    false);

                    connectionNodePid = linkPids.contains(preLinkPid) ? deleteLink
                            .getsNodePid() : deleteLink.geteNodePid();
                }

                if (newLinks.get(0).getsNodePid() == connectionNodePid
                        || newLinks.get(0).geteNodePid() == connectionNodePid) {

                    for (int i = 0; i < newLinks.size(); i++) {

                        RdRestrictionVia newVia = new RdRestrictionVia();

                        newVia.setDetailId(breakVia.getDetailId());

                        newVia.setGroupId(breakVia.getGroupId());

                        newVia.setLinkPid(newLinks.get(i).getPid());

                        newVia.setSeqNum(breakVia.getSeqNum() + i);

                        result.insertObject(newVia, ObjStatus.INSERT,
                                detail.parentPKValue());
                    }

                } else {

                    for (int i = newLinks.size(); i > 0; i--) {

                        RdRestrictionVia newVia = new RdRestrictionVia();

                        newVia.setDetailId(breakVia.getDetailId());

                        newVia.setGroupId(breakVia.getGroupId());

                        newVia.setLinkPid(newLinks.get(i - 1).getPid());

                        newVia.setSeqNum(breakVia.getSeqNum() + newLinks.size()
                                - i);

                        result.insertObject(newVia, ObjStatus.INSERT,
                                detail.parentPKValue());
                    }
                }

                result.insertObject(breakVia, ObjStatus.DELETE,
                        detail.parentPKValue());

                // 维护后续经过线序号
                for (RdRestrictionVia via : viaGroup) {

                    if (via.getSeqNum() > breakVia.getSeqNum()) {

                        via.changedFields().put("seqNum",
                                via.getSeqNum() + newLinks.size() - 1);

                        result.insertObject(via, ObjStatus.UPDATE,
                                detail.parentPKValue());
                    }
                }
            }
        }
    }


}
