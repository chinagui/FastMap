package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * Created by Crayeres on 2017/2/8.
 */
public class GLM01103 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
                RdLink link = (RdLink) row;

                int specialTraffic = link.getSpecialTraffic();
                if (link.changedFields().containsKey("specialTraffic"))
                    specialTraffic = Integer.valueOf(link.changedFields().get("specialTraffic").toString());

                int paveStatus = link.getPaveStatus();
                if (link.changedFields().containsKey("paveStatus"))
                    paveStatus = Integer.valueOf(link.changedFields().get("paveStatus").toString());

                if (specialTraffic == 1 && paveStatus == 1) {
                    setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                }
            }
        }
    }
}
