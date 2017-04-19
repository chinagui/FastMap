package com.navinfo.dataservice.engine.edit.operation.topo.move.movezonenode;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLinkPart;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameLinkSelector;

import java.sql.Connection;
import java.util.List;

/**
 * Created by Crayeres on 2017/3/3.
 */
public class Check {

    public void checkSameLink(ZoneNode node, Connection conn) throws Exception {
        RdSameLinkSelector selector = new RdSameLinkSelector(conn);
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
                throw new Exception("此link不是该组同一关系中的主要素，不能进行修形操作");
        }
    }
}
