package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.List;

/**
 * Created by Crayeres on 2017/2/9.
 */
public class GLM01446 extends baseRule {
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

                if (formOfWay == 54) {
                    RdLinkSelector selector = new RdLinkSelector(getConn());
                    RdLink link = (RdLink) selector.loadByIdOnlyRdLink(form.getLinkPid(), false);

                    int sNodePid = link.getsNodePid();
                    checkParkingForm(selector, link, sNodePid);

                    int eNodePid = link.geteNodePid();
                    checkParkingForm(selector, link, eNodePid);
                }
            }
        }
    }

    private void checkParkingForm(RdLinkSelector selector, RdLink link, int sNodePid) throws Exception {
        List<RdLink> links = selector.loadByNodePid(sNodePid, false);
        for (RdLink l : links) {
            boolean flag = true;
            List<IRow> forms = l.getForms();
            for (IRow f : forms) {
                RdLinkForm ff = (RdLinkForm) f;
                if (ff.getFormOfWay() == 53 || ff.getFormOfWay() == 54) {
                    flag = false;
                }
            }
            if (flag) {
                setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
            }
        }
    }
}
