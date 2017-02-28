package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLinkPart;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.List;

/**
 * Created by chaixin on 2017/1/19 0019.
 */
public class PermitCheckNotModifyLinkAssamelink extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow nodeRow : checkCommand.getGlmList()) {
            if (nodeRow instanceof ZoneNode && checkCommand.getObjType() == ObjType.ZONENODE) {
                ZoneNode node = (ZoneNode) nodeRow;
                if (!node.changedFields().containsKey("geometry"))
                    continue;

                RdSameLinkSelector selector = new RdSameLinkSelector(getConn());
                List<RdSameLink> sameLinks = selector.loadSameLinkByNodeAndTableName(node.pid(), "ZONE_LINK", false);
                for (RdSameLink sameLink : sameLinks) {
                    boolean flag = true;
                    List<IRow> parts = sameLink.getParts();
                    for (IRow partRow : parts) {
                        RdSameLinkPart part = (RdSameLinkPart) partRow;
                        String name = part.getTableName();
                        if (name.equalsIgnoreCase("RD_LINK") || name.equalsIgnoreCase("AD_LINK")) {
                            flag = false;
                            break;
                        }
                    }
                    if (!flag)
                        setCheckResult("", "", 0);
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
