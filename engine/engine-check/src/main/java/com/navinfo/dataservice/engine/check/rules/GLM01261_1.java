package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Crayeres on 2017/2/9.
 */
public class GLM01261_1 extends baseRule {

    private List<Integer> speedLimitLink = new ArrayList<>();

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        preparData(checkCommand);

        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
                RdLink link = (RdLink) row;

                int kind = link.getKind();
                if (link.changedFields().containsKey("kind"))
                    kind = (int) link.changedFields().get("kind");

                if ((kind == 11 || kind == 13) && !speedLimitLink.contains(link.pid())) {
                    setCheckResult(link.getGeometry().toString(), "[RD_LINK, " + link.pid() + "]", link.mesh());
                }
            } else if (row instanceof RdLinkSpeedlimit && row.status() == ObjStatus.UPDATE) {
                RdLinkSpeedlimit speedlimit = (RdLinkSpeedlimit) row;

                int speedType = speedlimit.getSpeedType();
                if (speedlimit.changedFields().containsKey("speedType"))
                    speedType = (int) speedlimit.changedFields().get("speedType");

                int speedClass = speedlimit.getSpeedClass();
                if (speedlimit.changedFields().containsKey("speedClass"))
                    speedClass = (int) speedlimit.changedFields().get("speedClass");

                if (speedType == 0 && speedClass != 7) {
                    RdLink link = (RdLink) new RdLinkSelector(getConn()).loadByIdOnlyRdLink(speedlimit.getLinkPid(),
                            false);

                    if (link.getKind() == 11 || link.getKind() == 13) {
                        setCheckResult(link.getGeometry().toString(), "[RD_LINK, " + link.pid() + "]", link.mesh());
                    }
                }
            }
        }
    }

    private void preparData(CheckCommand checkCommand) {
        for (IRow obj : checkCommand.getGlmList()) {
            if (obj instanceof RdLink) {
                RdLink link = (RdLink) obj;
                if (link.status() == ObjStatus.DELETE)
                    continue;

                Map<String, String> speedLimits = new HashMap<>();
                for (IRow f : link.getSpeedlimits()) {
                    RdLinkSpeedlimit speedlimit = (RdLinkSpeedlimit) f;
                    speedLimits.put(speedlimit.getRowId(), speedlimit.getSpeedType() + "," + speedlimit.getSpeedClass
                            ());
                }

                for (IRow row : checkCommand.getGlmList()) {
                    if (row instanceof RdLinkSpeedlimit) {
                        RdLinkSpeedlimit speedlimit = (RdLinkSpeedlimit) row;
                        if (speedlimit.getLinkPid() == link.pid()) {
                            if (speedlimit.status() == ObjStatus.DELETE) {
                                speedLimits.remove(speedlimit.getRowId());
                            } else {
                                int speedType = speedlimit.getSpeedType();
                                if (speedlimit.changedFields().containsKey("speedType"))
                                    speedType = (int) speedlimit.changedFields().get("speedType");

                                int speedClass = speedlimit.getSpeedClass();
                                if (speedlimit.changedFields().containsKey("speedClass"))
                                    speedClass = (int) speedlimit.changedFields().get("speedClass");

                                speedLimits.put(speedlimit.getRowId(), speedType + "," + speedClass);
                            }
                        }
                    }
                }

                if (speedLimits.containsValue("0,7")) {
                    speedLimitLink.add(link.pid());
                }
            }
        }
    }
}
