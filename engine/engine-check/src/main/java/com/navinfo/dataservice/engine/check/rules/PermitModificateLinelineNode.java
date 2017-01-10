package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

import java.util.List;
import java.util.Map;

/**
 * Created by chaixin on 2016/12/27 0027.
 */
public class PermitModificateLinelineNode extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink) {
                RdLink rdLink = (RdLink) row;
                Map<String, Object> changedFields = rdLink.changedFields();
                if (changedFields == null) {
                    continue;
                }
                String linkPid = "";
                if (changedFields.containsKey("sNodePid") || changedFields.containsKey("eNodePid")) {
                    linkPid = rdLink.pid() + "";
                }
                if (StringUtils.isEmpty(linkPid)) {
                    continue;
                }

                checkLaneConnexity(linkPid);
                checkRestriction(linkPid);
                checkVoiceguide(linkPid);
                checkBranch(linkPid);
                checkDirectroute(linkPid);
            }
        }
    }

    private void checkLaneConnexity(String linkPid) throws Exception {
        String sql = "SELECT RL.GEOMETRY, RL.LINK_PID, RL.MESH_ID FROM RD_LINK RL WHERE RL.LINK_PID = " + linkPid +
                "" + " AND EXISTS (SELECT * FROM RD_LANE_VIA RLV RLV.LINK_PID = " + linkPid + " AND RLV.U_RECORD <> 2)";
        DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
        List<Object> resultList = getObj.exeSelect(this.getConn(), sql);

        if (!resultList.isEmpty()) {
            this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int) resultList.get(2),
                    "该线是经过线，移动该线造成线线关系（车信）从inLin到outlink的不连续");
        }
    }

    private void checkRestriction(String linkPid) throws Exception {
        String sql = "SELECT RL.GEOMETRY, RL.LINK_PID, RL.MESH_ID FROM RD_LINK RL WHERE RL.LINK_PID = " + linkPid +
                "AND EXISTS (SELECT * FROM RD_RESTRICTION_VIA RRV RRV.LINK_PID = " + linkPid + " AND RRV.U_RECORD <> " +
                "2)";
        DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
        List<Object> resultList = getObj.exeSelect(this.getConn(), sql);

        if (!resultList.isEmpty()) {
            this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int) resultList.get(2),
                    "该线是经过线，移动该线造成线线关系（线线交限）从inLin到outlink的不连续");
        }
    }

    private void checkVoiceguide(String linkPid) throws Exception {
        String sql = "SELECT RL.GEOMETRY, RL.LINK_PID, RL.MESH_ID FROM RD_LINK RL WHERE RL.LINK_PID = " + linkPid +
                " AND EXISTS (SELECT * FROM RD_VOICEGUIDE_VIA ROV ROV.LINK_PID = " + linkPid + " AND ROV.U_RECORD <> " +
                "2)";
        DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
        List<Object> resultList = getObj.exeSelect(this.getConn(), sql);

        if (!resultList.isEmpty()) {
            this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int) resultList.get(2),
                    "该线是经过线，移动该线造成线线关系（线线语音引导）从inLin到outlink的不连续");
        }
    }

    private void checkBranch(String linkPid) throws Exception {
        String sql = "SELECT RL.GEOMETRY, RL.LINK_PID, RL.MESH_ID FROM RD_LINK RL WHERE RL.LINK_PID = " + linkPid +
                " AND EXISTS (SELECT * FROM RD_BRANCH_VIA ROV RBV.LINK_PID = " + linkPid + " AND RBV.U_RECORD <> 2)";
        DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
        List<Object> resultList = getObj.exeSelect(this.getConn(), sql);

        if (!resultList.isEmpty()) {
            this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int) resultList.get(2),
                    "该线是经过线，移动该线造成线线关系（线线分歧）从inLin到outlink的不连续");
        }
    }

    private void checkDirectroute(String linkPid) throws Exception {
        String sql = "SELECT RL.GEOMETRY, RL.LINK_PID, RL.MESH_ID FROM RD_LINK RL WHERE RL.LINK_PID = " + linkPid +
                " AND EXISTS (SELECT * FROM RD_DIRECTROUTE_VIA ROV RDV.LINK_PID = " + linkPid + " AND RDV.U_RECORD <>" +
                " 2)";
        DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
        List<Object> resultList = getObj.exeSelect(this.getConn(), sql);

        if (!resultList.isEmpty()) {
            this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int) resultList.get(2),
                    "该线是经过线，移动该线造成线线关系（线线顺行）从inLin到outlink的不连续");
        }
    }


    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
    }
}
