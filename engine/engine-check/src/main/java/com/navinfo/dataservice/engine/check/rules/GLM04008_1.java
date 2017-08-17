package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.dao.glm.selector.rd.gate.RdGateSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import org.apache.commons.lang.StringUtils;

import java.util.*;


/**
 * GLM04008_1
 * 大门方向编辑-服务端后检查
 * 交限时间段编辑-服务端后检查
 * 道路方向编辑-服务端后检查
 * 删除交限-服务后检查
 */
public class GLM04008_1 extends baseRule {

    public GLM04008_1() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

        //交限时间段编辑
        Set<Integer> detailPids = new HashSet<>();

        //交限删除,退出线、经过线变更
        Set<Integer> relationLinkPids = new HashSet<>();

        for (IRow obj : checkCommand.getGlmList()) {

            //大门方向编辑
            if (obj instanceof RdGate && obj.status().equals(ObjStatus.UPDATE)) {

                RdGate gate = (RdGate) obj;

                if (gate.changedFields().containsKey("dir")
                        && Integer.valueOf(gate.changedFields().get("dir").toString()) == 1
                        && gateCheck(gate) != 0) {

                    String target = "[RD_GATE," + gate.getPid() + "]";

                    this.setCheckResult("", target, 0);
                }
            }

            //道路方向编辑
            if (obj instanceof RdLink && obj.status().equals(ObjStatus.UPDATE)) {

                RdLink link = (RdLink) obj;

                if (link.changedFields().containsKey("direct")
                        && Integer.valueOf(link.changedFields().get("direct").toString()) == 1) {

                    linkCheck(link.getPid());
                }
            }

            //交限删除,退出线、经过线变更-删除交限
            if (obj instanceof RdRestriction && obj.status().equals(ObjStatus.DELETE)) {

                RdRestriction restriction = (RdRestriction) obj;

                relationLinkPids.add(restriction.getInLinkPid());

                for (IRow objDetail : restriction.getDetails()) {

                    RdRestrictionDetail detail = (RdRestrictionDetail) objDetail;

                    for (IRow viaRow : detail.getVias()) {

                        RdRestrictionVia via = (RdRestrictionVia) viaRow;

                        relationLinkPids.add(via.getLinkPid());
                    }

                    relationLinkPids.add(detail.getOutLinkPid());
                }
            }

            //交限删除,退出线、经过线变更-退出线变更
            if (obj instanceof RdRestrictionDetail) {

                RdRestrictionDetail detail = (RdRestrictionDetail) obj;

                if (detail.changedFields().containsKey("outLinkPid") || obj.status().equals(ObjStatus.DELETE)) {

                    relationLinkPids.add(detail.getOutLinkPid());

                    for (IRow viaRow : detail.getVias()) {

                        RdRestrictionVia via = (RdRestrictionVia) viaRow;

                        relationLinkPids.add(via.getLinkPid());
                    }
                }

                if (obj.status().equals(ObjStatus.UPDATE)) {

                    for (IRow rowCondition : detail.getConditions()) {

                        RdRestrictionCondition condition = (RdRestrictionCondition) rowCondition;

                        if (condition.getVehicle() != 0) {
                            continue;
                        }

                        String timeDomain = condition.getTimeDomain();

                        if (condition.changedFields().containsKey("timeDomain")) {

                            timeDomain = condition.changedFields().get("timeDomain").toString();
                        }

                        if (StringUtils.isNotEmpty(timeDomain)) {

                            detailPids.add(condition.getDetailId());
                        }
                    }
                }
            }

            //交限删除,退出线、经过线变更-经过线变更
            if (obj instanceof RdRestrictionVia) {

                RdRestrictionVia via = (RdRestrictionVia) obj;

                if (via.changedFields().containsKey("linkPid") || obj.status().equals(ObjStatus.DELETE)) {

                    relationLinkPids.add(via.getLinkPid());
                }
            }

            //交限时间段编辑
            if (obj instanceof RdRestrictionCondition) {

                RdRestrictionCondition condition = (RdRestrictionCondition) obj;

                if (condition.getVehicle() != 0)
                {
                    continue;
                }

                String timeDomain = condition.getTimeDomain();

                if (condition.changedFields().containsKey("timeDomain")) {

                    timeDomain = condition.changedFields().get("timeDomain").toString();
                }

                if (StringUtils.isNotEmpty(timeDomain)) {

                    detailPids.add(condition.getDetailId());
                }
            }
        }

        editRestrictionCheck(new ArrayList<>(relationLinkPids));

        editConditionCheck(detailPids);
    }

    /**
     * 大门方向编辑检查
     */
    private Integer gateCheck(RdGate gate) throws Exception {

        List<Integer> gateLinkPids = new ArrayList<>();

        gateLinkPids.add(gate.getInLinkPid());

        gateLinkPids.add(gate.getOutLinkPid());

        List<Integer> filterLinks = filterLink(gateLinkPids);

        if (filterLinks.size() != 2) {

            return 0;
        }

        RdRestrictionSelector restrictionSelector = new RdRestrictionSelector(this.getConn());

        List<RdRestriction> storageTmp = restrictionSelector.loadByLinks(filterLinks, 1, true);

        storageTmp.addAll(restrictionSelector.loadByLinks(filterLinks, 2, true));

        storageTmp.addAll(restrictionSelector.loadByLinks(filterLinks, 3, true));

        Set<Integer> handlePid = new HashSet<>();

        for (RdRestriction restriction : storageTmp) {

            if (handlePid.contains(restriction.getPid())) {

                continue;
            }

            handlePid.add(restriction.getPid());

            for (IRow objDetail : restriction.getDetails()) {

                RdRestrictionDetail detail = (RdRestrictionDetail) objDetail;

                //是否为永久普通交限
                if (!restrictionRule(detail)) {

                    continue;
                }

                TreeMap<Integer, Integer> sortLink = new TreeMap<>();

                sortLink.put(0, restriction.getInLinkPid());

                for (IRow viaRow : detail.getVias()) {

                    RdRestrictionVia via = (RdRestrictionVia) viaRow;

                    sortLink.put(via.getSeqNum(), via.getLinkPid());
                }

                sortLink.put(detail.getVias().size() + 1, detail.getOutLinkPid());

                String ids = org.apache.commons.lang.StringUtils.join(new ArrayList<>(sortLink.values()), ",");

                if (ids.contains(gate.getOutLinkPid() + "," + gate.getInLinkPid())) {

                    return 0;
                }
            }
        }
        return gate.getPid();
    }

    /**
     * 判断是否为永久普通交限
     */
    private boolean restrictionRule(RdRestrictionDetail detail) {

        for (IRow objCondition : detail.getConditions()) {

            RdRestrictionCondition condition = (RdRestrictionCondition) objCondition;

            if (StringUtils.isNotEmpty(condition.getTimeDomain()) || condition.getVehicle() != 0) {

                return false;
            }
        }
        return true;
    }

    /**
     * 道路方向编辑检查
     */
    private void linkCheck(int linkPid) throws Exception {

        RdGateSelector gateSelector = new RdGateSelector(this.getConn());

        List<RdGate> gates = gateSelector.loadByLink(linkPid, false);

        for (RdGate gate : gates) {

            if (gate.getDir() == 1 && gateCheck(gate) != 0) {

                String target = "[RD_LINK," + linkPid + "]";

                this.setCheckResult("", target, 0);

                break;
            }
        }
    }

    /**
     * 交限删除,退出线、经过线变更检查
     */
    private void editRestrictionCheck(List<Integer> relationLinkPids) throws Exception {

        if (relationLinkPids.size()<1)
        {
            return;
        }

        Set<Integer> gatePids = new HashSet<>();

        RdGateSelector gateSelector = new RdGateSelector(this.getConn());

        List<RdGate> gates = gateSelector.loadByLinks(relationLinkPids, false);

        for (RdGate gate : gates) {

            if (gate.getDir() == 1) {

                gatePids.add(gateCheck(gate));
            }
        }

        for (int pid : gatePids) {

            if (pid != 0) {

                String target = "[RD_GATE," + pid + "]";

                this.setCheckResult("", target, 0);
            }
        }
    }

    /**
     * 交限时间段编辑检查
     */
    private void editConditionCheck(Set<Integer> detailPids) throws Exception {

        Set<Integer> restrictionPids = new HashSet<>();

        for (int detailPid : detailPids) {

            int restrictionPid = editConditionCheck(detailPid);

            if (restrictionPid != 0) {

                restrictionPids.add(restrictionPid);
            }
        }

        for (int restrictionPid : restrictionPids) {
            String target = "[RD_RESTRICTION," + restrictionPid + "]";
            this.setCheckResult("", target, 0);
        }
    }

    /**
     * 交限时间段编辑检查
     */
    private int editConditionCheck(int detailPid) throws Exception {

        List<Integer> linkPids = new ArrayList<>();

        String strSql = "SELECT R.IN_LINK_PID FROM RD_RESTRICTION_DETAIL D, RD_RESTRICTION R  WHERE D.DETAIL_ID ="
                + detailPid
                + " AND D.RESTRIC_PID = R.PID AND D.U_RECORD <> 2  AND R.U_RECORD <> 2";

        DatabaseOperator getObj = new DatabaseOperator();

        List<Object> pidList = getObj.exeSelect(this.getConn(), strSql);

        if (pidList.size() > 0) {

            int linkPid = Integer.parseInt(String.valueOf(pidList.get(0)));

            linkPids.add(linkPid);
        }

        strSql = "SELECT V.LINK_PID FROM  RD_RESTRICTION_VIA V WHERE V.DETAIL_ID = "
                + detailPid
                + " AND  V.U_RECORD <> 2 ORDER BY V.SEQ_NUM ";

        pidList = getObj.exeSelect(this.getConn(), strSql);

        for (Object obj : pidList) {

            int linkPid = Integer.parseInt(String.valueOf(obj));

            linkPids.add(linkPid);
        }

        strSql = "SELECT D.OUT_LINK_PID FROM  RD_RESTRICTION_DETAIL D WHERE D.DETAIL_ID = "
                + detailPid
                + " AND  D.U_RECORD <> 2  ";

        pidList = getObj.exeSelect(this.getConn(), strSql);

        if (pidList.size() > 0) {

            int linkPid = Integer.parseInt(String.valueOf(pidList.get(0)));

            linkPids.add(linkPid);
        }

        RdGateSelector gateSelector = new RdGateSelector(this.getConn());

        for (int i = linkPids.size() - 1; i > 0; i--) {

            List<Integer> pids = new ArrayList<>();

            pids.add(linkPids.get(i));

            pids.add(linkPids.get(i - 1));

            List<Integer> filterLinks = filterLink(pids);

            if (filterLinks.size() != 2) {
                continue;
            }

            List<RdGate> gates = gateSelector.loadByLink(linkPids.get(i), false);

            for (RdGate gate : gates) {

                if (gate.getDir() == 1
                        && gate.getInLinkPid() == linkPids.get(i)
                        && gate.getOutLinkPid() == linkPids.get(i - 1)) {

                    strSql = "SELECT R.PID FROM RD_RESTRICTION R, RD_RESTRICTION_DETAIL D WHERE D.DETAIL_ID = "
                            + detailPid
                            + " AND R.PID = D.RESTRIC_PID AND D.U_RECORD <> 2 AND R.U_RECORD <> 2";

                    pidList = getObj.exeSelect(this.getConn(), strSql);

                    if (pidList.size() > 0) {

                        return Integer.parseInt(String.valueOf(pidList.get(0)));
                    }

                }
            }

        }

        return 0;
    }

    /**
     * 获取双方向道路
     */
    private List<Integer> filterLink(List<Integer> linkPids) throws Exception {

        List<Integer> pids = new ArrayList<>();

        if (linkPids.size() > 0) {

            String ids = org.apache.commons.lang.StringUtils.join(linkPids, ",");

            String strSql = "SELECT LINK_PID FROM RD_LINK D WHERE D.DIRECT=1 AND D.U_RECORD<>2 AND D.LINK_PID IN (" + ids + ")";

            DatabaseOperator getObj = new DatabaseOperator();

            List<Object> linkPidList = getObj.exeSelect(this.getConn(), strSql);

            for (Object obj : linkPidList) {

                pids.add(Integer.parseInt(String.valueOf(obj)));
            }
        }

        return pids;
    }

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {


    }


}
