package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

import java.util.List;

/**
 * Created by chaixin on 2017/1/9 0009.
 */
public class GLM33007 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink) {
                RdLink link = (RdLink) row;
                if (!link.changedFields().containsKey("direct"))
                    continue;
                String sql = "SELECT RN.GEOMETRY, 'RD_VARIABLE_SPEED, ' || RVS.VSPEED_PID || ']', RL.MESH_ID FROM " +
                        "RD_VARIABLE_SPEED RVS, RD_LINK RL, RD_NODE RN WHERE RVS.IN_LINK_PID = RL.LINK_PID AND RVS" +
                        ".NODE_PID <> RL.E_NODE_PID AND RVS.NODE_PID = RN.NODE_PID AND RL.DIRECT = 2 AND RVS.U_RECORD" +
                        " <> 2 AND RL.U_RECORD <> 2 AND RN.U_RECORD <> 2 AND RL.LINK_PID = " + link.pid() + " UNION " +
                        "ALL SELECT RN" +
                        ".GEOMETRY, 'RD_VARIABLE_SPEED, ' || RVS.VSPEED_PID || ']', RL.MESH_ID FROM RD_VARIABLE_SPEED" +
                        " RVS, RD_LINK RL, RD_NODE RN WHERE RVS.OUT_LINK_PID = RL.LINK_PID AND RVS.NODE_PID <> RL" +
                        ".S_NODE_PID AND RVS.NODE_PID = RN.NODE_PID AND RL.DIRECT = 2 AND RVS.U_RECORD <> 2 AND RL" +
                        ".U_RECORD <> 2 AND RN.U_RECORD <> 2 AND RL.LINK_PID = " + link.pid();
                DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
                List<Object> resultList = getObj.exeSelect(getConn(), sql);
                if (resultList.isEmpty())
                    continue;
                setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int) resultList.get(2));
            } else if (row instanceof RdVariableSpeed) {
                RdVariableSpeed speed = (RdVariableSpeed) row;
                int inLinkPid = speed.getInLinkPid();
                int nodePid = speed.getNodePid();
                int outLinkPid = speed.getInLinkPid();
                if (speed.changedFields().containsKey("outLinkPid"))
                    outLinkPid = (int) speed.changedFields().get("outLinkPid");
                String sql = "SELECT RN.GEOMETRY, '[RD_VARIABLE_SPEED, ' || " + speed.pid() + " || ']', 0 FROM " +
                        "RD_LINK RL1, RD_LINK RL2, RD_NODE " +
                        "RN WHERE ((RL1.E_NODE_PID <> " + nodePid + " AND RL1.DIRECT = 2 AND RL1.U_RECORD <> 2) OR " +
                        "(RL2.S_NODE_PID <> " + nodePid + " AND RL2.DIRECT = 2 AND RL2.U_RECORD <> 2)) AND " +
                        "RL1.LINK_PID = " + inLinkPid + "AND RL2.LINK_PID = " + outLinkPid + "AND RN.NODE_PID = " +
                        nodePid + " UNION ALL SELECT RN.GEOMETRY, '[RD_VARIABLE_SPEED, ' || " + speed.pid() + " || " +
                        "']', 0 FROM RD_LINK RL1, " +
                        "RD_LINK RL2, RD_NODE RN WHERE ((RL1.S_NODE_PID <> " + nodePid + " AND RL1.DIRECT = 3 AND " +
                        "RL1.U_RECORD <> 2) OR (RL2.E_NODE_PID <> " + nodePid + " AND RL2.DIRECT = 3 AND RL2.U_RECORD" +
                        " <> 2)) AND RL1.LINK_PID = " + inLinkPid + "AND RL2.LINK_PID = " + outLinkPid;
                DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
                List<Object> resultList = getObj.exeSelect(getConn(), sql);
                if (resultList.isEmpty())
                    continue;
                setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int) resultList.get(2));
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
