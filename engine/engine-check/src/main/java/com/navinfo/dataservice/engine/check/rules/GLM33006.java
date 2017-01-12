package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeedVia;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaixin on 2017/1/9 0009.
 */
public class GLM33006 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdVariableSpeed) {
                RdVariableSpeed speed = (RdVariableSpeed) row;
                List<Integer> pids = new ArrayList<>();
                pids.add(speed.getInLinkPid());
                int outLinkPid = speed.getOutLinkPid();
                if (speed.changedFields().containsKey("outLinkPid"))
                    outLinkPid = (int) speed.changedFields().get("outLinkPid");
                pids.add(outLinkPid);
                for (IRow via : speed.getVias()) {
                    RdVariableSpeedVia speedVia = (RdVariableSpeedVia) via;
                    pids.add(speedVia.getLinkPid());
                }

                String sql = "SELECT 1 FROM RD_LINK RL, RD_LINK_FORM RLF, RD_LINK_LIMIT RLL WHERE RL.LINK_PID IN (" +
                        StringUtils.getInteStr(pids) + ") AND RL.LINK_PID = RLF.LINK_PID AND RL.LINK_PID = RLL" +
                        ".LINK_PID AND RL.U_RECORD <> 2 AND RLF.U_RECORD <> 2 AND RLL.U_RECORD <> 2 AND (RL.KIND IN " +
                        "(0, 8, 9, 10, 11, 13) OR RLF.FORM_OF_WAY IN (18, 20, 33, 50) OR RL.IMI_CODE IN (1, 2) OR " +
                        "(RLL.TYPE = 2 AND RLL.TIME_DOMAIN IS NULL) OR RLL.TYPE = 3)";
                DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
                List<Object> resultList = getObj.exeSelect(getConn(), sql);
                if (!resultList.isEmpty())
                    setCheckResult("", "", 0);
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
