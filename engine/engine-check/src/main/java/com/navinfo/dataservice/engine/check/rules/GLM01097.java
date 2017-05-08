package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Crayeres on 2017/2/6.
 */
public class GLM01097 extends baseRule {

    private List<Integer> busOnlyLink = new ArrayList<>();

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

                int functionClass = link.getFunctionClass();
                if (link.changedFields().containsKey("functionClass"))
                    functionClass = Integer.valueOf(link.changedFields().get("functionClass").toString());

                if (functionClass != 5 && busOnlyLink.contains(link.pid())) {
                    setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                }
            } else if (row instanceof RdLinkForm && row.status() != ObjStatus.DELETE) {
                RdLinkForm form = (RdLinkForm) row;

                int formOfWay = form.getFormOfWay();
                if (form.changedFields().containsKey("formOfWay"))
                    formOfWay = Integer.valueOf(form.changedFields().get("formOfWay").toString());

                if (formOfWay == 22) {
                    RdLink link = (RdLink) new RdLinkSelector(getConn()).loadByIdOnlyRdLink(form.getLinkPid(), false);
                    if (link.getFunctionClass() != 5) {
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

                if (formOfWays.containsValue(22))
                    busOnlyLink.add(link.pid());
            }
        }
    }
}
