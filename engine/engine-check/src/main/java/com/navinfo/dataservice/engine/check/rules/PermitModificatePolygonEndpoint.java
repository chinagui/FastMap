package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.List;

/**
 * Created by Crayeres on 2017/1/19 0019.
 */
public class PermitModificatePolygonEndpoint extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof ZoneLink) {
                ZoneLink link = (ZoneLink) row;
                if (link.changedFields().containsKey("sNodePid") || link.changedFields().containsKey("eNodePid")) {
                    int sNodePid = 0;
                    if (link.changedFields().containsKey("sNodePid"))
                        sNodePid = Integer.valueOf(link.changedFields().get("sNodePid").toString());
                    int eNodePid = 0;
                    if (link.changedFields().containsKey("eNodePid"))
                        sNodePid = Integer.valueOf(link.changedFields().get("eNodePid").toString());
                    log.info("PermitModificatePolygonEndpoint:[pid=" + link.pid() + ",sNodePid=" + link.getsNodePid()
                            + ":" + sNodePid + "," + "eNodePid=" + link.geteNodePid() + ":" + eNodePid + "]");
                    ZoneFaceSelector selector = new ZoneFaceSelector(getConn());
                    List<ZoneFace> faces = selector.loadZoneFaceByLinkId(link.pid(), false);
                    if (faces.size() > 0)
                        setCheckResult("", "", 0);
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
