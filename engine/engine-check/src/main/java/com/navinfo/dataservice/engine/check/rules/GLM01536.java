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
 * Created by Crayeres on 2017/2/10.
 */
public class GLM01536 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
                RdLink link = (RdLink) row;

                int kind = link.getKind();
                if (link.changedFields().containsKey("kind"))
                    kind = Integer.valueOf(link.changedFields().get("kind").toString());

                if (kind == 1) {
                    RdLink l = (RdLink) new RdLinkSelector(getConn()).loadById(link.pid(), false);
                    if (l.getForms().size() == 1) {
                        RdLinkForm form = (RdLinkForm) l.getForms().get(0);

                        if (form.getFormOfWay() == 50) {
                            setCheckResult(l.getGeometry().toString(), "[RD_LINK," + l.pid() + "]", l.mesh());
                        }
                    }
                }
            } else if (row instanceof RdLinkForm) {
                RdLink link = (RdLink) new RdLinkSelector(getConn()).loadById(((RdLinkForm) row).getLinkPid(), false);
                List<IRow> forms = link.getForms();
                if (forms.size() != 1) {
                    RdLinkForm form = (RdLinkForm) forms.get(0);
                    if (form.getFormOfWay() == 50) {
                        setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                    }
                }
            }
        }
    }
}
