package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

import java.util.List;

/**
 * Created by chaixin on 2016/12/27 0027.
 */
public class PermitCheckLinkNodeCrfi extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdNode) {
                RdNode node = (RdNode) row;
                if (!node.changedFields().containsKey("geometry"))
                    return;

                String sql = "SELECT RN.GEOMETRY, '[RD_NODE,' || RN.NODE_PID || ']' TARGET, RNM.MESH_ID FROM RD_NODE " +
                        "RN, RD_NODE_MESH RNM WHERE RN.NODE_PID = " + node.pid() + " AND RN.NODE_PID = RNM.NODE_PID " +
                        "AND EXISTS (SELECT * FROM RD_INTER_NODE RIN WHERE RIN.U_RECORD <> 2 AND RIN.NODE_PID = " +
                        node.pid() + ") AND ROWNUM = 1";

                DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
                List<Object> resultList = getObj.exeSelect(this.getConn(), sql);

                if (!resultList.isEmpty()) {
                    this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int) resultList
                            .get(2));
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
    }
}
