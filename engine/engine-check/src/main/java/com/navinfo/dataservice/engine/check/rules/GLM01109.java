package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * Created by Crayeres on 2017/2/7.
 */
public class GLM01109 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
                RdLink link = (RdLink) row;

                int laneNum = link.getLaneNum();
                if (link.changedFields().containsKey("laneNum"))
                    laneNum = (int) link.changedFields().get("laneNum");

                int laneLeft = link.getLaneLeft();
                if (link.changedFields().containsKey("laneLeft"))
                    laneLeft = (int) link.changedFields().get("laneLeft");

                int laneRight = link.getLaneRight();
                if (link.changedFields().containsKey("laneRight"))
                    laneRight = (int) link.changedFields().get("laneRight");

                if (laneNum != 0 && (laneLeft != 0 || laneRight != 0)) {
                    setCheckResult(link.getGeometry().toString(), "[RD_LINK, " + link.pid() + "]", link.mesh());
                }
            }
        }
    }
}
