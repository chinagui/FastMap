package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Crayeres on 2017/2/9.
 */
public class GLM01282 extends baseRule {

    private List<Integer> singltonLimitLink = new ArrayList<>();

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        preparData(checkCommand);

        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
                RdLink link = (RdLink) row;

                int direct = link.getDirect();
                if (link.changedFields().containsKey("direct"))
                    direct = Integer.valueOf(link.changedFields().get("direct").toString());

                if (singltonLimitLink.contains(link.pid()) && (direct == 2 || direct == 3)) {
                    setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                }
            } else if (row instanceof RdLinkLimit && row.status() != ObjStatus.DELETE) {
                RdLinkLimit linkLimit = (RdLinkLimit) row;

                int type = linkLimit.getType();
                if (linkLimit.changedFields().containsKey("type"))
                    type = Integer.valueOf(linkLimit.changedFields().get("type").toString());

                if (type == 1) {
                    RdLink link = (RdLink) new RdLinkSelector(getConn()).loadByIdOnlyRdLink(linkLimit.getLinkPid(),
                            false);
                    if (link.getDirect() == 2 || link.getDirect() == 3) {
                        setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
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

                Map<String, Integer> linkLimits = new HashMap<>();
                for (IRow l : link.getLimits()) {
                    RdLinkLimit linkLimit = (RdLinkLimit) l;
                    linkLimits.put(linkLimit.getRowId(), linkLimit.getType());
                }

                for (IRow row : checkCommand.getGlmList()) {
                    if (row instanceof RdLinkLimit) {
                        RdLinkLimit linkLimit = (RdLinkLimit) row;
                        if (linkLimit.getLinkPid() == link.pid()) {
                            if (linkLimit.status() == ObjStatus.DELETE) {
                                linkLimits.remove(linkLimit.getRowId());
                            } else {
                                int type = linkLimit.getType();
                                if (linkLimit.changedFields().containsKey("type"))
                                    type = Integer.valueOf(linkLimit.changedFields().get("type").toString());
                                linkLimits.put(linkLimit.getRowId(), type);
                            }
                        }
                    }
                }

                if (linkLimits.containsValue(1)) {
                    singltonLimitLink.add(link.pid());
                }
            }
        }
    }
}
