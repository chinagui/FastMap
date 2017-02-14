package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * Created by Crayeres on 2017/2/9.
 */
public class GLM01088 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
                RdLink link = (RdLink) row;

                int paveStatus = link.getPaveStatus();
                if (link.changedFields().containsKey("paveStatus"))
                    paveStatus = Integer.valueOf(link.changedFields().get("paveStatus").toString());

                int functionClass = link.getFunctionClass();
                if (link.changedFields().containsKey("functionClass"))
                    functionClass = Integer.valueOf(link.changedFields().get("functionClass").toString());

                if (paveStatus == 1 && (functionClass == 1 || functionClass == 2)) {
                    setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                }
            }
        }
    }
}
