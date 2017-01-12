package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * Created by chaixin on 2017/1/9 0009.
 */
public class PropertyCheckSheetnodeCornernode extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof ZoneNode) {
                ZoneNode node = (ZoneNode) row;
                if (node.changedFields().isEmpty())
                    continue;

                int form = node.getForm();
                if (node.changedFields().containsKey("form"))
                    form = (int) node.changedFields().get("form");
                if (form == 1 || form == 7) {
                    int kind = node.getKind();
                    if (node.changedFields().containsKey("kind"))
                        kind = (int) node.changedFields().get("kind");
                    if (kind != 1)
                        setCheckResult(node.getGeometry(), "[ZONE_NODE, " + node.pid() + "]", 0);
                }
            } else if (row instanceof AdNode) {
                AdNode node = (AdNode) row;
                if (node.changedFields().isEmpty())
                    continue;

                int form = node.getForm();
                if (node.changedFields().containsKey("form"))
                    form = (int) node.changedFields().get("form");
                if (form == 1 || form == 7) {
                    int kind = node.getKind();
                    if (node.changedFields().containsKey("kind"))
                        kind = (int) node.changedFields().get("kind");
                    if (kind != 1)
                        setCheckResult(node.getGeometry(), "[AD_NODE, " + node.pid() + "]", 0);
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
