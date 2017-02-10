package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * Created by Crayeres on 2017/2/9.
 */
public class GLM01470 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
                RdLink link = (RdLink) row;

                int kind = link.getKind();
                if (link.changedFields().containsKey("kind"))
                    kind = (int) link.changedFields().get("kind");

                int walkFlag = link.getWalkFlag();
                if (link.changedFields().containsKey("walkFlag"))
                    walkFlag = (int) link.changedFields().get("walkFlag");

                if (kind == 15 && walkFlag != 1) {
                    setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                }
            }
        }
    }
}
