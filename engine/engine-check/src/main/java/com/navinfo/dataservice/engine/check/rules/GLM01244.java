package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * Created by Crayeres on 2017/2/7.
 */
public class GLM01244 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
                RdLink link = (RdLink) row;

                int direct = link.getDirect();
                if (link.changedFields().containsKey("direct"))
                    direct = Integer.valueOf(link.changedFields().get("direct").toString());

                if (direct == 2 || direct == 3) {
                    RdLinkSpeedlimit speedlimit = null;
                    for (IRow sl : link.getSpeedlimits()) {
                        speedlimit = (RdLinkSpeedlimit) sl;
                        break;
                    }
                    for (IRow sl : checkCommand.getGlmList()) {
                        if (sl instanceof RdLinkSpeedlimit && sl.status() != ObjStatus.DELETE) {
                            RdLinkSpeedlimit ss = (RdLinkSpeedlimit) sl;
                            if (ss.getLinkPid() == link.pid()) {
                                speedlimit = ss;
                                break;
                            }
                        }
                    }
                    if (null == speedlimit)
                        continue;

                    int speedType = speedlimit.getSpeedType();
                    if (speedlimit.changedFields().containsKey("speedType"))
                        speedType = Integer.valueOf(speedlimit.changedFields().get("speedType").toString());
                    if (speedType != 0)
                        continue;

                    int toSpeedLimit = speedlimit.getToSpeedLimit();
                    if (speedlimit.changedFields().containsKey("toSpeedLimit"))
                        toSpeedLimit = Integer.valueOf(speedlimit.changedFields().get("toSpeedLimit").toString());

                    int fromSpeedLimit = speedlimit.getFromSpeedLimit();
                    if (speedlimit.changedFields().containsKey("fromSpeedLimit"))
                        fromSpeedLimit = Integer.valueOf(speedlimit.changedFields().get("fromSpeedLimit").toString());

                    log.info("GLM01244:[Direct=" + direct + ",toSpeedLimit=" + toSpeedLimit + ",fromSpeedLimit=" +
                            fromSpeedLimit + "]");

                    if (direct == 2 && toSpeedLimit != 0) {
                        setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                    } else if (direct == 3 && fromSpeedLimit != 0) {
                        setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                    }
                }
            } else if (row instanceof RdLinkSpeedlimit && row.status() == ObjStatus.UPDATE) {
                RdLinkSpeedlimit speedlimit = (RdLinkSpeedlimit) row;
                RdLink link = (RdLink) new RdLinkSelector(getConn()).loadByIdOnlyRdLink(speedlimit.getLinkPid(), false);
                if (link.getDirect() == 2 && speedlimit.getToSpeedLimit() != 0) {
                    setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                } else if (link.getDirect() == 3 && speedlimit.getFromSpeedLimit() != 0) {
                    setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                }
            }
        }
    }
}
