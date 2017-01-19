package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * Created by chaixin on 2017/1/19 0019.
 */
public class ShapingCheckLinkRingobreak2 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow nodeRow : checkCommand.getGlmList()) {
            if (nodeRow instanceof ZoneNode) {
                ZoneNode node = (ZoneNode) nodeRow;
                if (!node.status().equals(OperType.CREATE)) continue;

                for (IRow linkRow : checkCommand.getGlmList()) {
                    if (linkRow instanceof ZoneLink) {
                        ZoneLink link = (ZoneLink) linkRow;
                        if (link.status().equals(OperType.CREATE) && (link.getsNodePid() == node.pid() || link
                                .geteNodePid() == node.pid())) {
                            double length = link.getLength();
                            if (length <= 2)
                                setCheckResult("", "", 0);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
