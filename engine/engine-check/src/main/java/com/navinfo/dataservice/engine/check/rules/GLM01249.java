package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.List;

/**
 * Created by Crayeres on 2017/2/9.
 */
public class GLM01249 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLinkForm && row.status() == ObjStatus.UPDATE) {
                RdLinkForm form = (RdLinkForm) row;

                int oldFormOfWay = form.getFormOfWay();
                int formOfWay = form.getFormOfWay();
                if (form.changedFields().containsKey("formOfWay"))
                    formOfWay = (int) form.changedFields().get("formOfWay");

                if (oldFormOfWay == 15 && formOfWay != 15) {
                    RdLink link = (RdLink) new RdLinkSelector(getConn()).loadById(form.getLinkPid(), false);
                    boolean flag = false;
                    List<IRow> speedLimits = link.getSpeedlimits();
                    for (IRow sl : speedLimits) {
                        RdLinkSpeedlimit speedlimit = (RdLinkSpeedlimit) sl;
                        if (speedlimit.getSpeedType() == 0 && (speedlimit.getFromLimitSrc() == 7 || speedlimit
                                .getToLimitSrc() == 7)) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                    }
                }
            } else if (row instanceof RdLinkSpeedlimit && row.status() != ObjStatus.DELETE) {
                RdLinkSpeedlimit speedlimit = (RdLinkSpeedlimit) row;

                int speedType = speedlimit.getSpeedType();
                if (speedlimit.changedFields().containsKey("speedType"))
                    speedType = (int) speedlimit.changedFields().get("speedType");

                int fromLimitSrc = speedlimit.getFromLimitSrc();
                if (speedlimit.changedFields().containsKey("fromLimitSrc"))
                    fromLimitSrc = (int) speedlimit.changedFields().get("fromLimitSrc");

                int toLimitSrc = speedlimit.getToLimitSrc();
                if (speedlimit.changedFields().containsKey("toLimitSrc"))
                    toLimitSrc = (int) speedlimit.changedFields().get("toLimitSrc");

                if (speedType == 0 && (fromLimitSrc == 7 || toLimitSrc == 7)) {
                    RdLink link = (RdLink) new RdLinkSelector(getConn()).loadById(speedlimit.getLinkPid(), false);
                    boolean flag = false;
                    List<IRow> forms = link.getForms();
                    for (IRow f : forms) {
                        RdLinkForm ff = (RdLinkForm) f;
                        if (ff.getFormOfWay() == 15) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                    }
                }
            }
        }
    }
}
