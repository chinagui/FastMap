package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkKind;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.List;

/**
 * Created by chaixin on 2017/1/19 0019.
 */
public class ShapingCheckSheetlineNotMove extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow nodeRow : checkCommand.getGlmList()) {
            if (nodeRow instanceof ZoneNode) {
                ZoneNode node = (ZoneNode) nodeRow;
                if (!node.changedFields().containsKey("geometry")) continue;

                ZoneLinkSelector selector = new ZoneLinkSelector(getConn());
                List<ZoneLink> links = selector.loadByNodePid(node.pid(), false);
                for (ZoneLink link : links) {
                    for (IRow kindRow : link.getKinds()) {
                        ZoneLinkKind kind = (ZoneLinkKind) kindRow;
                        if (kind.getForm() == 0)
                            setCheckResult("", "", 0);
                    }
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
