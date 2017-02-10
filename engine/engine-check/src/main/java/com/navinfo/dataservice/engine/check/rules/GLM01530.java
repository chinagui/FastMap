package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.List;

/**
 * Created by Crayeres on 2017/2/8.
 */
public class GLM01530 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLinkForm) {
                RdLinkForm form = (RdLinkForm) row;

                RdLinkSelector linkSelector = new RdLinkSelector(getConn());
                RdLink link = (RdLink) linkSelector.loadByIdOnlyRdLink(form.getLinkPid(), false);

                int sNodePid = link.getsNodePid();
                checkLinkForm(linkSelector, link, sNodePid);

                int eNodePid = link.geteNodePid();
                checkLinkForm(linkSelector, link, eNodePid);
            }
        }
    }

    private void checkLinkForm(RdLinkSelector linkSelector, RdLink link, int sNodePid) throws Exception {
        List<RdLink> links = linkSelector.loadByNodePid(sNodePid, false);
        boolean hasFlyover = false, hasTypical = false;
        for (RdLink l : links) {
            List<IRow> forms = l.getForms();
            for (IRow f : forms) {
                RdLinkForm ff = (RdLinkForm) f;
                if (ff.getFormOfWay() == 16) {
                    hasFlyover = true;
                }
                if (ff.getFormOfWay() == 17) {
                    hasTypical = true;
                }
            }
        }
        if (hasFlyover && hasTypical) {
            setCheckResult(link.getGeometry().toString(), "[RD_LINK, " + link.pid() + "]", link.mesh());
        }
    }
}
