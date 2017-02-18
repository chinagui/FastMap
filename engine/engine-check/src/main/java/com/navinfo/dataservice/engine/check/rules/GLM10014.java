package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.slope.RdSlopeSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Crayeres on 2017/2/17.
 */
public class GLM10014 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
                RdLink link = (RdLink) row;

                final int sNodePid = link.getsNodePid();
                if (link.changedFields().containsKey("sNodePid")) {
                    checkRdSlope(link, sNodePid);
                }

                int eNodePid = link.geteNodePid();
                if (link.changedFields().containsKey("eNodePid")) {
                    checkRdSlope(link, eNodePid);
                }
            }
        }
    }

    private void checkRdSlope(RdLink link, final int nodePid) throws Exception {
        RdSlopeSelector slopeSelector = new RdSlopeSelector(getConn());
        List<RdSlope> slopeList = slopeSelector.loadByNodePids(new ArrayList<Integer>() {{
            add(nodePid);
        }}, false);
        if (!slopeList.isEmpty()) {
            RdLinkSelector linkSelector = new RdLinkSelector(getConn());
            List<RdLink> links = linkSelector.loadByNodePid(nodePid, false);
            if (links.size() < 3) {
                setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
            }
        }
    }
}
