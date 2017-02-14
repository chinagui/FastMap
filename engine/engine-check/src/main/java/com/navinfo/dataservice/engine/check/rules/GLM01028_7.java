package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Crayeres on 2017/2/9.
 */
public class GLM01028_7 extends baseRule {

    private List<Integer> pedestrianLink = new ArrayList<>();

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        preparData(checkCommand);

        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
                RdLink link = (RdLink) row;

                int kind = link.getKind();
                if (link.changedFields().containsKey("kind"))
                    kind = Integer.valueOf(link.changedFields().get("kind").toString());
                if (kind == 11 || kind == 15 || pedestrianLink.contains(link.pid())) {
                    checkRdLaneConnexity(link.pid());
                }

            } else if (row instanceof RdLinkForm && row.status() != ObjStatus.DELETE) {
                RdLinkForm form = (RdLinkForm) row;

                int formOfWay = form.getFormOfWay();
                if (form.changedFields().containsKey("formOfWay"))
                    formOfWay = Integer.valueOf(form.changedFields().get("formOfWay").toString());

                if (formOfWay == 20) {
                    checkRdLaneConnexity(form.getLinkPid());
                }
            } else if (row instanceof RdLaneTopology && row.status() != ObjStatus.DELETE) {
                RdLaneTopology topology = (RdLaneTopology) row;

                int linkPid = topology.getOutLinkPid();
                if (topology.changedFields().containsKey("outLinkPid"))
                    linkPid = Integer.valueOf(topology.changedFields().get("outLinkPid").toString());

                if (checkLinkForm(linkPid))
                    break;
            } else if (row instanceof RdLaneVia && row.status() != ObjStatus.DELETE) {
                RdLaneVia via = (RdLaneVia) row;

                int linkPid = via.getLinkPid();
                if (via.changedFields().containsKey("linkPid"))
                    linkPid = Integer.valueOf(via.changedFields().get("linkPid").toString());

                if (checkLinkForm(linkPid))
                    break;
            }
        }
    }

    private void checkRdLaneConnexity(Integer linkPid) throws Exception {
        List<RdLaneConnexity> result = new ArrayList<>();
        List<RdLaneConnexity> list1 = new RdLaneConnexitySelector(getConn()).loadByLink(linkPid, 1, false);
        List<RdLaneConnexity> list2 = new RdLaneConnexitySelector(getConn()).loadByLink(linkPid, 2, false);
        List<RdLaneConnexity> list3 = new RdLaneConnexitySelector(getConn()).loadByLink(linkPid, 3, false);
        result.addAll(list1);
        result.addAll(list2);
        result.addAll(list3);

        if (!result.isEmpty()) {
            setCheckResult("", "[RD_LINK," + linkPid + "]", 0);
        }
    }

    private boolean checkLinkForm(int linkPid) throws Exception {
        RdLink link = (RdLink) new RdLinkSelector(getConn()).loadById(linkPid, false);
        List<IRow> forms = link.getForms();
        for (IRow f : forms) {
            if (((RdLinkForm) f).getFormOfWay() == 20) {
                setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                break;
            }
        }
        if (link.getKind() == 11 || link.getKind() == 15) {
            setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
            return true;
        }
        return false;
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

                if (formOfWays.containsValue(20)) {
                    pedestrianLink.add(link.pid());
                }
            }
        }
    }
}
