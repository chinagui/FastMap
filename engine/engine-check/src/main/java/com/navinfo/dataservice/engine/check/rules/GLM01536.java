package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Crayeres on 2017/2/10.
 */
public class GLM01536 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

        Set<Integer> linkPids = new HashSet<>();

        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
                RdLink link = (RdLink) row;

                int kind = link.getKind();
                if (link.changedFields().containsKey("kind"))
                    kind = Integer.valueOf(link.changedFields().get("kind").toString());

                if (kind == 1) {
                    linkPids.add(link.pid());
                }
            } else if (row instanceof RdLinkForm) {
                RdLinkForm form = (RdLinkForm) row;

                linkPids.add(form.getLinkPid());

            } else if (row instanceof RdCross) {
                RdCross cross = (RdCross) row;

                for (IRow crossLinkRow : cross.getLinks()) {
                    linkPids.add(((RdCrossLink) crossLinkRow).getLinkPid());
                }
            } else if (row instanceof RdCrossLink) {
                linkPids.add(((RdCrossLink) row).getLinkPid());
            }
        }

        check(new ArrayList<>(linkPids));
    }


    private void check(List<Integer> linkPids) throws Exception {

        if(linkPids.size()<1)
        {
            return;
        }

        List<IRow> linkRows = new RdLinkSelector(getConn()).loadByIds(linkPids, false, true);

        for (IRow linkRow : linkRows) {

            RdLink link = (RdLink) linkRow;

            //不是高速（kind==1），continue
            if (link.getKind() != 1) {

                continue;
            }

            boolean have50 = false;

            boolean haveOther = false;

            List<IRow> formRows = link.getForms();

            for (IRow formRow : formRows) {

                RdLinkForm form = (RdLinkForm) formRow;

                if (form.getFormOfWay() == 50) {

                    have50 = true;

                } else {

                    haveOther = true;
                }
            }

            //不是交叉口内link，continue
            if (!have50) {
                continue;
            }

            //是交叉口内link且有其他形态，continue
            if (haveOther) {

                continue;
            }

            //是交叉口内link且无其他形态
            setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());

        }

    }
}
