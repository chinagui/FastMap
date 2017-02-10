package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.List;

/**
 * Created by Crayeres on 2017/2/9.
 */
public class GLM01257 extends baseRule {
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

                if (formOfWay == 20) {
                    RdLink link = (RdLink) new RdLinkSelector(getConn()).loadById(form.getLinkPid(), false);
                    List<IRow> speedLimits = link.getSpeedlimits();
                    boolean flag = false;
                    for (IRow sl : speedLimits) {
                        RdLinkSpeedlimit speedlimit = (RdLinkSpeedlimit) sl;
                        if (speedlimit.getSpeedType() == 0 && speedlimit.getSpeedClass() != 8) {
                            flag = true;
                        }
                    }
                    if (flag) {
                        setCheckResult(link.getGeometry().toString(), "[RD_LINK, " + link.pid() + "]", link.mesh());
                    }
                }

            } else if (row instanceof RdLinkSpeedlimit && row.status() != ObjStatus.DELETE) {
                RdLinkSpeedlimit speedlimit = (RdLinkSpeedlimit) row;

                int speedType = speedlimit.getSpeedType();
                if (speedlimit.changedFields().containsKey("speedType"))
                    speedType = (int) speedlimit.changedFields().get("speedType");

                int speedClass = speedlimit.getSpeedClass();
                if (speedlimit.changedFields().containsKey("speedClass"))
                    speedClass = (int) speedlimit.changedFields().get("speedClass");

                if (speedType == 0 && speedClass != 8) {
                    RdLink link = (RdLink) new RdLinkSelector(getConn()).loadById(speedlimit.getLinkPid(), false);
                    boolean flag = false;
                    List<IRow> forms = link.getForms();
                    for (IRow f : forms) {
                        RdLinkForm ff = (RdLinkForm) f;
                        if (ff.getFormOfWay() == 20) {
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
}
