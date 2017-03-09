package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.selector.rd.slope.RdSlopeSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.List;

/**
 * Created by Crayeres on 2017/3/7.
 */
public class RelatingCheckNoRepeatSlope extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdSlope && row.status() == ObjStatus.INSERT) {
                RdSlope slope = (RdSlope) row;

                List<RdSlope> slopeList = new RdSlopeSelector(getConn()).loadByOutLink(slope.getLinkPid(), false);
                for (RdSlope s : slopeList) {
                    if (slope.getNodePid() == s.getNodePid()) {
                        setCheckResult("", "", 0);
                    }
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
