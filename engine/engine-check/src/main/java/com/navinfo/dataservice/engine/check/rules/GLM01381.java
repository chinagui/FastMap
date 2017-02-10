package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Crayeres on 2017/2/6.
 */
public class GLM01381 extends baseRule {

    private List<Integer> landspaceLink = new ArrayList<>();

    private List<Integer> landspaceName = new ArrayList<>();

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        preparData(checkCommand);

        for (IRow row : checkCommand.getGlmList()) {
            //if (row instanceof RdLink) {
            //    RdLink link = (RdLink) row;
            //
            //    if (landspaceName.contains(link.pid()) && !landspaceLink.contains(link.pid())) {
            //        setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
            //    }
            //} else
                if (row instanceof RdLinkName && row.status() != ObjStatus.DELETE) {
                RdLinkName name = (RdLinkName) row;

                int nameType = name.getNameType();
                if (name.changedFields().containsKey("nameType"))
                    nameType = Integer.valueOf(name.changedFields().get("nameType").toString());

                if (nameType == 3) {
                    RdLink link = (RdLink) new RdLinkSelector(getConn()).loadById(name.getLinkPid(), false);
                    List<IRow> forms = link.getForms();
                    boolean formFlag = true;
                    for (IRow f : forms) {
                        RdLinkForm ff = (RdLinkForm) f;
                        if (ff.getFormOfWay() == 60) {
                            formFlag = false;
                            break;
                        }
                    }
                    if (formFlag) {
                        setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                    }
                }
            } else if (row instanceof RdLinkForm && row.status() != ObjStatus.DELETE) {
                RdLinkForm form = (RdLinkForm) row;

                int formOfWay = form.getFormOfWay();
                if (form.changedFields().containsKey("formOfWay"))
                    formOfWay = Integer.valueOf(form.changedFields().get("formOfWay").toString());

                if (formOfWay == 60) {
                    RdLink link = (RdLink) new RdLinkSelector(getConn()).loadById(form.getLinkPid(), false);
                    List<IRow> names = link.getNames();
                    boolean nameFlag = true;
                    for (IRow n : names) {
                        RdLinkName nn = (RdLinkName) n;
                        if (nn.getNameType() == 3) {
                            nameFlag = false;
                            break;
                        }
                    }
                    if (nameFlag) {
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

                Map<String, Integer> names = new HashMap<>();
                for (IRow n : link.getNames()) {
                    RdLinkName name = (RdLinkName) n;
                    names.put(name.getRowId(), name.getNameType());
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
                    } else if (row instanceof RdLinkName) {
                        RdLinkName name = (RdLinkName) row;
                        if (name.getLinkPid() == link.pid()) {
                            if (name.status() == ObjStatus.DELETE) {
                                formOfWays.remove(name.getRowId());
                            } else {
                                int nameType = name.getNameType();
                                if (name.changedFields().containsKey("nameType"))
                                    nameType = Integer.valueOf(name.changedFields().get("nameType").toString());
                                names.put(name.getRowId(), nameType);
                            }
                        }
                    }
                }

                if (formOfWays.containsValue(60))
                    landspaceLink.add(link.pid());

                if (names.containsValue(3))
                    landspaceName.add(link.pid());
            }
        }
    }
}
