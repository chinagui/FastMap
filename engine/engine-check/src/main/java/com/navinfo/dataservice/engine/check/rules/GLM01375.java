package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * Created by Crayeres on 2017/2/7.
 */
public class GLM01375 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink) {
                RdLink link = (RdLink) row;

                int multiDigitized = link.getMultiDigitized();
                if (link.changedFields().containsKey("multiDigitized"))
                    multiDigitized = Integer.valueOf(link.changedFields().get("multiDigitized").toString());

                int imiCode = link.getImiCode();
                if (link.changedFields().containsKey("imiCode"))
                    imiCode = Integer.valueOf(link.changedFields().get("imiCode").toString());

                if (multiDigitized == 1 && imiCode == 2) {
                    setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                }
            }
        }
    }
}
