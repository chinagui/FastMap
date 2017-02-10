package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Crayeres on 2017/2/8.
 */
public class GLM01454 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLinkForm) {
                RdLinkForm form = (RdLinkForm) row;

                RdLinkSelector linkSelector = new RdLinkSelector(getConn());
                RdLink link = (RdLink) linkSelector.loadByIdOnlyRdLink(form.getLinkPid(), false);

                int sNodePid = link.getsNodePid();
                checkCrossNode(linkSelector, link, sNodePid);

                int eNodePid = link.geteNodePid();
                checkCrossNode(linkSelector, link, eNodePid);
            }
        }
    }

    private void checkCrossNode(RdLinkSelector linkSelector, RdLink link, int sNodePid) throws Exception {
        RdCrossSelector crossSelector = new RdCrossSelector(getConn());
        RdCross cross = crossSelector.loadCrossByNodePid(sNodePid, false);
        List<Integer> nodes = new ArrayList<>();
        for (IRow n : cross.getNodes()) {
            nodes.add(((RdCrossNode) n).getNodePid());
        }
        List<Integer> innerLinks = new ArrayList<>();
        for (IRow l : cross.getLinks()) {
            innerLinks.add(((RdCrossLink) l).getLinkPid());
        }
        List<RdLink> links = linkSelector.loadLinkPidByNodePids(StringUtils.getInteStr(nodes), false);
        boolean flag = true;
        for (RdLink l : links) {
            if (!innerLinks.contains(l.pid())) {
                List<IRow> forms = l.getForms();
                for (IRow f : forms) {
                    RdLinkForm ff = (RdLinkForm) f;
                    if (ff.getFormOfWay() != 52) {
                        flag = false;
                        break;
                    }
                }
            }
            if (!flag) {
                break;
            }
        }
        if (flag) {
            for (RdLink l : links) {
                if (innerLinks.contains(l.pid())) {
                    List<IRow> forms = l.getForms();
                    flag = true;
                    for (IRow f : forms) {
                        RdLinkForm ff = (RdLinkForm) f;
                        if (ff.getFormOfWay() == 52) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        setCheckResult(link.getGeometry().toString(), "[RD_LINK, " + link.pid() + "]", link.mesh());
                        break;
                    }
                }
            }
        }
    }
}
