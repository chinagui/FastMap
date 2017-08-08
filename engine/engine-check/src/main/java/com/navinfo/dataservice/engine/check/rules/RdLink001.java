package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Rdlink word RDLINK001 后台 两条RDLink不能首尾点一致
 *
 * @author zhangxiaoyi
 */

public class RdLink001 extends baseRule {

    private static Logger logger = Logger.getLogger(RdLink001.class);

    public RdLink001() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

        Set<Integer> nodePids = new HashSet<>();

        Set<String> linkPointFlag = new HashSet<>();

        Set<Integer> handlePids = new HashSet<>();

        for (IRow obj : checkCommand.getGlmList()) {

            if ((obj instanceof RdLink) && (obj.status() != ObjStatus.DELETE)) {

                RdLink rdLink = (RdLink) obj;

                if (!linkPointFlag.add(rdLink.getsNodePid() + "_"
                        + rdLink.geteNodePid())
                        || !linkPointFlag.add(rdLink.geteNodePid() + "_"
                        + rdLink.getsNodePid())) {

                    this.setCheckResult("", "", 0);

                    return;
                }

                nodePids.add(rdLink.getsNodePid());

                nodePids.add(rdLink.geteNodePid());

                handlePids.add(rdLink.getPid());
            }
        }

        if (nodePids.size() < 1) {

            return;
        }

        List<RdLink> links = new RdLinkSelector(getConn()).loadByNodePids(
                new ArrayList<>(nodePids), false);

        for (RdLink link : links) {

            if (handlePids.contains(link.getPid())) {

                continue;
            }

            if (!linkPointFlag.add(link.getsNodePid() + "_"
                    + link.geteNodePid())
                    || !linkPointFlag.add(link.geteNodePid() + "_"
                    + link.getsNodePid())) {

                this.setCheckResult("", "", 0);

                return;
            }

            handlePids.add(link.getPid());
        }

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
    }
}
