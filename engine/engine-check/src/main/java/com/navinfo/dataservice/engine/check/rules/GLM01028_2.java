package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.dao.glm.selector.rd.se.RdSeSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.List;

/**
 * Created by Crayeres on 2017/2/7.
 */
public class GLM01028_2 extends baseRule {

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

                if (kind == 11 || kind == 10) {
                    List<RdSe> rdSes = new RdSeSelector(getConn()).loadRdSesWithLinkPid(link.pid(), false);
                    if (!rdSes.isEmpty()) {
                        setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                    }
                }
            } else if (row instanceof RdLinkForm) {
                RdLinkForm form = (RdLinkForm) row;

                int formOfWay = form.getFormOfWay();
                if (form.changedFields().containsKey("formOfWay"))
                    formOfWay = Integer.valueOf(form.changedFields().get("formOfWay").toString());

                if (formOfWay == 20) {
                    List<RdSe> rdSes = new RdSeSelector(getConn()).loadRdSesWithLinkPid(form.getLinkPid(), false);
                    if (!rdSes.isEmpty()) {
                        setCheckResult("", "[RD_LINK," + form.getLinkPid() + "]", 0);
                    }
                }
            }
        }
    }
}
