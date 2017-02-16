package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import org.apache.poi.hssf.record.formula.functions.False;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Crayeres on 2017/2/6.
 */
public class GLM01513 extends baseRule {

    private List<Integer> narrowLink = new ArrayList<>();

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        preparData(checkCommand);

        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink) {
                RdLink link = (RdLink) row;
                if (link.status() == ObjStatus.DELETE)
                    continue;

            } else if (row instanceof RdLinkForm && row.status() != ObjStatus.DELETE) {
                RdLinkForm form = (RdLinkForm) row;

                int formOfWay = form.getFormOfWay();
                if (form.changedFields().containsKey("formOfWay"))
                    formOfWay = Integer.valueOf(form.changedFields().get("formOfWay").toString());

                if (formOfWay == 43) {
                    RdLink link = (RdLink) new RdLinkSelector(getConn()).loadById(form.getLinkPid(), false);
                    List<IRow> speeds = link.getSpeedlimits();
                    boolean speedFlag = false;
                    for (IRow s : speeds) {
                        RdLinkSpeedlimit ss = (RdLinkSpeedlimit) s;
                        if (ss.getSpeedClass() != 7 && ss.getSpeedClass() != 8) {
                            speedFlag = true;
                            break;
                        }
                    }
                    if (speedFlag) {
                        setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                    }
                }
            } else if (row instanceof RdLinkSpeedlimit && row.status() != ObjStatus.DELETE) {
                RdLinkSpeedlimit speedlimit = (RdLinkSpeedlimit) row;

                int speedClass = speedlimit.getSpeedClass();
                if (speedlimit.changedFields().containsKey("speedClass"))
                    speedClass = Integer.valueOf(speedlimit.changedFields().get("speedClass").toString());

                if (speedClass != 7 && speedClass != 8) {
                    RdLink link = (RdLink) new RdLinkSelector(getConn()).loadById(speedlimit.getLinkPid(), false);
                    List<IRow> forms = link.getForms();
                    boolean formFlag = false;
                    for (IRow f : forms) {
                        RdLinkForm ff = (RdLinkForm) f;
                        if (ff.getFormOfWay() == 43) {
                            formFlag = true;
                            break;
                        }
                    }
                    if (formFlag) {
                        setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                    }
                }
            }
        }
    }

    private void preparData(CheckCommand checkCommand) {
        for (IRow obj : checkCommand.getGlmList()) {
            if (obj instanceof RdLink) {
                RdLink link = (RdLink) obj;
                if (link.status() == ObjStatus.DELETE)
                    continue;

                Map<String, Integer> formOfWays = new HashMap<>();
                for (IRow f : link.getForms()) {
                    RdLinkForm form = (RdLinkForm) f;
                    formOfWays.put(form.getRowId(), form.getFormOfWay());
                }

                for (IRow row : checkCommand.getGlmList()) {
                    if (row instanceof RdLinkForm) {
                        RdLinkForm form = (RdLinkForm) row;
                        if (form.getLinkPid() == link.pid()) {
                            if (form.status() == ObjStatus.DELETE) {
                                formOfWays.remove(form.getRowId());
                            } else {
                                int formOfWay = form.getFormOfWay();
                                if (form.changedFields().containsKey("formOfWay"))
                                    formOfWay = Integer.valueOf(form.changedFields().get("formOfWay").toString());
                                formOfWays.put(form.getRowId(), formOfWay);
                            }
                        }
                    }
                }

                if (formOfWays.containsValue(43))
                    narrowLink.add(link.pid());
            }
        }
    }
}
