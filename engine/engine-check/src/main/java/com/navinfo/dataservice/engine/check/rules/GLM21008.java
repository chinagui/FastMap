package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLinkPart;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.List;

/**
 * Created by Crayeres on 2017/2/9.
 */
public class GLM21008 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLinkForm && row.status() != ObjStatus.DELETE) {
                RdLinkForm form = (RdLinkForm) row;

                int formOfWay = form.getFormOfWay();
                if (form.changedFields().containsKey("formOfWay"))
                    formOfWay = (int) form.changedFields().get("formOfWay");

                if (formOfWay == 12 || formOfWay == 13) {
                    RdSameLinkPart part = new RdSameLinkSelector(getConn()).loadLinkPartByLink(form.getLinkPid(),
                            "RD_LINK", false);
                    if (null != part) {
                        setCheckResult("", "", 0);
                    }
                }
            } else if (row instanceof RdSameLinkPart && row.status() != ObjStatus.DELETE) {
                RdSameLinkPart part = (RdSameLinkPart) row;

                int linkPid = part.getLinkPid();
                if (part.changedFields().containsKey("linkPid"))
                    linkPid = (int) part.changedFields().get("linkPid");

                RdLink link = (RdLink) new RdLinkSelector(getConn()).loadById(linkPid, false);
                List<IRow> forms = link.getForms();
                boolean flag = false;
                for (IRow f : forms) {
                    RdLinkForm ff = (RdLinkForm) f;
                    if (ff.getFormOfWay() == 12 || ff.getFormOfWay() == 13) {
                        flag = true;
                    }
                }
                if (flag) {
                    setCheckResult(link.getGeometry().toString(), "[RD_LINK, " + link.pid() + "]", link.mesh());
                }
            }
        }
    }
}
