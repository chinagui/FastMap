package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkKind;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * Created by chaixin on 2017/1/13 0013.
 */
public class GLM50008 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof ZoneLinkKind) {
                ZoneLinkKind linkKind = (ZoneLinkKind) row;
                int kind = linkKind.getKind();
                if (linkKind.changedFields().containsKey("kind")) {
                    kind = (int) linkKind.changedFields().get("kind");
                }
                if (kind != 0)
                    continue;

                int form = linkKind.getForm();
                if (linkKind.changedFields().containsKey("form"))
                    form = (int) linkKind.changedFields().get("form");
                if (form != 1)
                    setCheckResult("", "", 0);
            }
        }
    }
}
