package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Crayeres on 2017/2/7.
 */
public class GLM01094 extends baseRule {

    private List<Integer> saOrPaLink = new ArrayList<>();

    private List<Integer> icOrJctLink = new ArrayList<>();

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        preparData(checkCommand);

        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
                RdLink link = (RdLink) row;

                int functionClass = link.getFunctionClass();
                if (link.changedFields().containsKey("functionClass"))
                    functionClass = Integer.valueOf(link.changedFields().get("functionClass").toString());

                log.info("GLM01094:[saOrPaLink=" + Arrays.toString(saOrPaLink.toArray()) + ",icOrJctLink=" + Arrays
                        .toString(icOrJctLink.toArray()) + "]");

                if (saOrPaLink.contains(link.pid()) && !icOrJctLink.contains(link.pid()) && functionClass != 5) {
                    setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                }
            } else if (row instanceof RdLinkForm) {
                RdLinkForm form = (RdLinkForm) row;
                int formOfWay = form.getFormOfWay();
                if (form.changedFields().containsKey("formOfWay"))
                    formOfWay = Integer.valueOf(form.changedFields().get("formOfWay").toString());

                if (formOfWay == 12 || formOfWay == 13) {
                    List<IRow> forms = new AbstractSelector(RdLinkForm.class, getConn()).loadRowsByParentId(form
                            .getLinkPid(), false);
                    boolean formFlag = true;
                    for (IRow f : forms) {
                        RdLinkForm ff = (RdLinkForm) f;
                        if (ff.getFormOfWay() == 10 || ff.getFormOfWay() == 11) {
                            formFlag = false;
                            break;
                        }
                    }
                    if (formFlag) {
                        RdLink link = (RdLink) new RdLinkSelector(getConn()).loadByIdOnlyRdLink(form.getLinkPid(),
                                false);
                        if (link.getFunctionClass() != 5) {
                            setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                        }
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

                log.info("GLM01094:[formOfWays" + Arrays.toString(formOfWays.values().toArray()) + "]");

                if (formOfWays.containsValue(12) || formOfWays.containsValue(13)) {
                    saOrPaLink.add(link.pid());
                }
                if (formOfWays.containsValue(10) || formOfWays.containsValue(11)) {
                    icOrJctLink.add(link.pid());
                }
            }
        }
    }
}
