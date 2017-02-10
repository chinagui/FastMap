package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * Created by Crayeres on 2017/2/8.
 */
public class GLM01254 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLinkSpeedlimit && row.status() != ObjStatus.DELETE) {
                RdLinkSpeedlimit speedlimit = (RdLinkSpeedlimit) row;

                int speedType = speedlimit.getSpeedType();
                if (speedlimit.changedFields().containsKey("speedType"))
                    speedType = Integer.valueOf(speedlimit.changedFields().get("speedType").toString());

                if (speedType == 0) {
                    int speedClass = speedlimit.getSpeedClass();
                    if (speedlimit.changedFields().containsKey("speedClass"))
                        speedClass = Integer.valueOf(speedlimit.changedFields().get("speedClass").toString());

                    if (speedClass == 0) {
                        setCheckResult("", "[RD_LINK," + speedlimit.getLinkPid() + "]", 0);
                    }
                }
            }
        }
    }
}
