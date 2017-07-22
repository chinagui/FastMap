package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkZone;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ly on 2017/7/4.
 */
public class Check008 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

        Set<Integer> linkPids = new HashSet<>();

        for (IRow row : checkCommand.getGlmList()) {

            if (row instanceof RdLinkZone && row.status() == ObjStatus.UPDATE) {

                RdLinkZone linkZone = (RdLinkZone) row;

                int type = linkZone.getType();

                if (linkZone.changedFields().containsKey("type"))

                    type = Integer.valueOf(linkZone.changedFields().get("type").toString());

                if (type == 0) {

                    linkPids.add(linkZone.getLinkPid());
                }
            }
        }

        setCheck(new ArrayList<>(linkPids));
    }


    /**
     * 批量获取出错link，并用link几何setCheckResult
     *
     * @param linkPids
     * @throws Exception
     */
    private void setCheck(List<Integer> linkPids) throws Exception {

        if (linkPids.size() < 1) {
            return;
        }
        List<IRow> linkRows = new RdLinkSelector(getConn()).loadByIds(linkPids, false, false);

        for (IRow linkRow : linkRows) {

            RdLink link = (RdLink) linkRow;

            setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
        }

    }
}
