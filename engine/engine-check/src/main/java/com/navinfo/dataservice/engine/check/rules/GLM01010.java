package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * Created by Crayeres on 2017/2/6.
 */
public class GLM01010 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink) {
                RdLink link = (RdLink) row;
                if (link.status() != ObjStatus.UPDATE)
                    continue;

                int specialTraffic = link.getSpecialTraffic();
                if (link.changedFields().containsKey("specialTraffic"))
                    specialTraffic = (int) link.changedFields().get("specialTraffic");
                if (specialTraffic == 0)
                    continue;

                int direct = link.getDirect();
                if (link.changedFields().containsKey("direct"))
                    direct = (int) link.changedFields().get("direct");

                if (direct == 0 || direct == 1)
                    setCheckResult(link.getGeometry().toString(), "[RD_LINK, " + link.pid() + "]", link.mesh());
            }
        }
    }
}
