package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
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
public class GLM01269 extends baseRule {

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

                int specialTraffic = link.getSpecialTraffic();
                if (link.changedFields().containsKey("specialTraffic"))
                    specialTraffic = (int) link.changedFields().get("specialTraffic");

                if (specialTraffic == 1 && speedLimitLink.contains(link.pid())) {
                    setCheckResult(link.getGeometry().toString(), "[RD_LINK, " + link.pid() + "]", link.mesh());
                }
            } else if (row instanceof RdLinkSpeedlimit && row.status() != ObjStatus.DELETE) {
                RdLinkSpeedlimit speedlimit = (RdLinkSpeedlimit) row;

                int speedType = speedlimit.getSpeedType();
                if (speedlimit.changedFields().containsKey("speedType"))
                    speedType = (int) speedlimit.changedFields().get("speedType");

                int speedClass = speedlimit.getSpeedClass();
                if (speedlimit.changedFields().containsKey("speedClass"))
                    speedClass = (int) speedlimit.changedFields().get("speedClass");

                if (speedType == 0 && (speedClass == 1 || speedClass == 2 || speedClass == 3)) {
                    RdLink link = (RdLink) new RdLinkSelector(getConn()).loadByIdOnlyRdLink(speedlimit.getLinkPid(),
                            false);
                    if (link.getSpecialTraffic() == 1) {
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

                Map<String, String> speedlimitMap = new HashMap<>();
                for (IRow sl : link.getSpeedlimits()) {
                    RdLinkSpeedlimit speedlimit = (RdLinkSpeedlimit) sl;
                    speedlimitMap.put(speedlimit.getRowId(), speedlimit.getSpeedType() + "," + speedlimit
                            .getSpeedType());
                }

                for (IRow row : checkCommand.getGlmList()) {
                    if (row instanceof RdLinkSpeedlimit) {
                        RdLinkSpeedlimit speedlimit = (RdLinkSpeedlimit) row;
                        if (speedlimit.getLinkPid() == link.pid()) {
                            if (speedlimit.status() == ObjStatus.DELETE) {
                                speedlimitMap.remove(speedlimit.getRowId());
                            } else {
                                int speedType = speedlimit.getSpeedType();
                                if (speedlimit.changedFields().containsKey("speedType"))
                                    speedType = (int) speedlimit.changedFields().get("speedType");

                                int speedClass = speedlimit.getSpeedClass();
                                if (speedlimit.changedFields().containsKey("speedClass"))
                                    speedClass = (int) speedlimit.changedFields().get("speedClass");

                                speedlimitMap.put(speedlimit.getRowId(), speedType + "," + speedClass);
                            }
                        }
                    }
                }

                if (speedlimitMap.containsValue("0,1") || speedlimitMap.containsValue("0,2") || speedlimitMap
                        .containsValue("0,3")) {
                    speedLimitLink.add(link.pid());
                }
            }
        }
    }
}
