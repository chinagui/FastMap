package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.engine.check.CheckEngine;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author songdongyan
 * @ClassName: GLM01017
 * @date 下午3:28:00
 * @Description: 轮渡/人渡种别的Link不能作为交限的进入线、经过线或退出线。
 * Link种别编辑服务端前检查:RdLink
 * 新增交限/卡车交限：RdRestriction
 * 修改交限/卡车交限：RdRestrictionDetail(新增，修改outLinkPid),RdRestrictionVia(新增，修改LinkPid)
 */
public class GLM01017 extends baseRule {

    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow obj : checkCommand.getGlmList()) {
            //新增交限/卡车交限
            if (obj instanceof RdRestriction) {
                RdRestriction restriObj = (RdRestriction) obj;
                if (restriObj.status().equals(ObjStatus.INSERT)) {
                    checkRdRestriction(restriObj);
                }
            }
            //修改交限/卡车交限
            else if (obj instanceof RdRestrictionDetail) {
                RdRestrictionDetail rdRestrictionDetail = (RdRestrictionDetail) obj;
                checkRdRestrictionDetail(rdRestrictionDetail, checkCommand);
            }
            //修改交限/卡车交限
            else if (obj instanceof RdRestrictionVia) {
                RdRestrictionVia rdRestrictionVia = (RdRestrictionVia) obj;
                checkRdRestrictionVia(rdRestrictionVia, checkCommand);
            }
            //link种别编辑
            else if (obj instanceof RdLink) {
                RdLink rdLink = (RdLink) obj;
                checkRdLink(rdLink, checkCommand.getOperType());
            }
        }
    }

    /**
     * @param rdLink
     * @param operType
     * @throws Exception
     */
    private void checkRdLink(RdLink rdLink, OperType operType) throws Exception {
        //link种别编辑
        if (rdLink.changedFields().containsKey("kind")) {
            int kind = Integer.parseInt(rdLink.changedFields().get("kind").toString());
            //非轮渡/人渡种别的Link,不触发检查
            if (kind != 11 && kind != 13) {
                return;
            }

            StringBuilder sb = new StringBuilder();

            sb.append("SELECT 1 FROM RD_RESTRICTION RS WHERE RS.IN_LINK_PID = " + rdLink.getPid());
            sb.append(" AND RS.U_RECORD <> 2");
            sb.append(" UNION");
            sb.append(" SELECT 1 FROM RD_RESTRICTION_DETAIL RD WHERE RD.OUT_LINK_PID = " + rdLink.getPid());
            sb.append(" AND RD.U_RECORD <> 2");
            sb.append(" UNION");
            sb.append(" SELECT 1 FROM RD_RESTRICTION_VIA VIA WHERE VIA.LINK_PID = " + rdLink.getPid());
            sb.append(" AND VIA.U_RECORD <> 2");

            String sql = sb.toString();
            log.info("RdLink前检查GLM01017:" + sql);

            DatabaseOperator getObj = new DatabaseOperator();
            List<Object> resultList = new ArrayList<Object>();
            resultList = getObj.exeSelect(this.getConn(), sql);

            if (resultList.size() > 0) {
                this.setCheckResult("", "", 0);
            }
        }


    }

    /**
     * @param rdRestrictionVia
     * @param checkCommand
     * @throws Exception
     */
    private void checkRdRestrictionVia(RdRestrictionVia rdRestrictionVia, CheckCommand checkCommand) throws Exception {
        int linkPid = 0;
        //新增的经过线
        if (rdRestrictionVia.status().equals(ObjStatus.INSERT)) {
            linkPid = rdRestrictionVia.getLinkPid();
        }
        //修改linkPid的经过线
        else if (rdRestrictionVia.status().equals(ObjStatus.UPDATE)) {
            if (rdRestrictionVia.changedFields().containsKey("linkPid")) {
                linkPid = Integer.parseInt(rdRestrictionVia.changedFields().get("linkPid").toString());
            }
        }
        if (linkPid != 0) {
            StringBuilder sb = new StringBuilder();

            sb.append(" SELECT 1 FROM RD_LINK R WHERE R.KIND IN (13,11)");
            sb.append(" AND R.U_RECORD <> 2");
            sb.append(" AND R.LINK_PID =" + linkPid);

            String sql2 = sb.toString();
            log.info("RdRestrictionVia前检查GLM01017:" + sql2);

            DatabaseOperator getObj = new DatabaseOperator();
            List<Object> resultList = new ArrayList<Object>();
            resultList = getObj.exeSelect(this.getConn(), sql2);

            if (resultList.size() > 0) {
                this.setCheckResult("", "", 0);
            }
        }

    }

    /**
     * @param rdRestrictionDetail
     * @param checkCommand
     * @throws Exception
     */
    private void checkRdRestrictionDetail(RdRestrictionDetail rdRestrictionDetail, CheckCommand checkCommand) throws
            Exception {
        Set<Integer> linkPids = new HashSet<Integer>();
        if (rdRestrictionDetail.status().equals(ObjStatus.INSERT)) {
            linkPids.add(rdRestrictionDetail.getOutLinkPid());
            for (IRow rdRestrictionViaObj : rdRestrictionDetail.getVias()) {
                RdRestrictionVia rdRestrictionVia = (RdRestrictionVia) rdRestrictionViaObj;
                linkPids.add(rdRestrictionVia.getLinkPid());
            }
        } else if (rdRestrictionDetail.status().equals(ObjStatus.UPDATE)) {
            if (rdRestrictionDetail.changedFields().containsKey("outLinkPid")) {
                int outLinkPid = Integer.parseInt(rdRestrictionDetail.changedFields().get("outLinkPid").toString());
                linkPids.add(outLinkPid);
            }
        }

        if (!linkPids.isEmpty()) {
            StringBuilder sb = new StringBuilder();

            sb.append(" SELECT 1 FROM RD_LINK R WHERE R.KIND IN (13,11)");
            sb.append(" AND R.U_RECORD <> 2");
            sb.append(" AND R.LINK_PID IN (" + StringUtils.join(linkPids.toArray(), ",") + ")");

            String sql = sb.toString();
            log.info("RdRestrictionDetail前检查GLM01017:" + sql);

            DatabaseOperator getObj = new DatabaseOperator();
            List<Object> resultList = new ArrayList<Object>();
            resultList = getObj.exeSelect(this.getConn(), sql);

            if (resultList.size() > 0) {
                this.setCheckResult("", "", 0);
            }
        }
    }

    /**
     * @param restriObj
     * @throws Exception
     */
    private void checkRdRestriction(RdRestriction restriObj) throws Exception {
        //进入线与退出线与经过线
        Set<Integer> linkPids = new HashSet<Integer>();

        linkPids.add(restriObj.getInLinkPid());
        for (IRow objTmp : restriObj.getDetails()) {
            RdRestrictionDetail detailObj = (RdRestrictionDetail) objTmp;
            linkPids.add(detailObj.getOutLinkPid());
            for (IRow rdRestrictionViaObj : detailObj.getVias()) {
                RdRestrictionVia rdRestrictionVia = (RdRestrictionVia) rdRestrictionViaObj;
                linkPids.add(rdRestrictionVia.getLinkPid());

            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append(" SELECT 1 FROM RD_LINK R WHERE R.KIND IN (13,11)");
        sb.append(" AND R.U_RECORD <> 2");
        sb.append(" AND R.LINK_PID IN (" + StringUtils.join(linkPids.toArray(), ",") + ")");

        String sql = sb.toString();
        log.info("RdRestriction前检查GLM01017:" + sql);

        DatabaseOperator getObj = new DatabaseOperator();
        List<Object> resultList = new ArrayList<Object>();
        resultList = getObj.exeSelect(this.getConn(), sql);

        if (resultList.size() > 0) {
            this.setCheckResult("", "", 0);
        }

    }


    public void postCheck(CheckCommand checkCommand) throws Exception {

    }

    public static void main(String[] args) throws Exception {
        List<IRow> details = new ArrayList<IRow>();
        RdRestrictionDetail rdRestrictionDetail = new RdRestrictionDetail();
        rdRestrictionDetail.setOutLinkPid(197951);
        rdRestrictionDetail.setPid(14076);
        rdRestrictionDetail.setRestricPid(11883);
        details.add(rdRestrictionDetail);

        RdRestriction rdRestriction = new RdRestriction();
        rdRestriction.setInLinkPid(197954);
        rdRestriction.setDetails(details);
        rdRestriction.setNodePid(175447);
        rdRestriction.setPid(11883);

        List<IRow> objList = new ArrayList<IRow>();
        objList.add(rdRestriction);

        //检查调用
        CheckCommand checkCommand = new CheckCommand();
        checkCommand.setGlmList(objList);
        checkCommand.setOperType(OperType.CREATE);
        checkCommand.setObjType(ObjType.RDRESTRICTION);
        CheckEngine checkEngine = new CheckEngine(checkCommand);
        System.out.println(checkEngine.preCheck());
    }

}
