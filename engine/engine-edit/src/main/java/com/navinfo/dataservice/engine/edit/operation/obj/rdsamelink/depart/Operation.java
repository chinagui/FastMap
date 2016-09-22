package com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.depart;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLinkPart;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameLinkSelector;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.util.List;

/**
 * 用于维护节点分离时对同一关系的影响
 * Created by chaixin on 2016/9/21 0021.
 */
public class Operation {

    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    public void depart(int nodePid, RdLink oldLink, List<RdLink> newLinks, Result result, String requester) throws Exception {
        RdSameLinkSelector rdSameLinkSelector = new RdSameLinkSelector(this.conn);
        RdSameLinkPart part = rdSameLinkSelector.loadLinkPartByLink(oldLink.pid(), "RD_LINK", true);

        RdLinkSelector rdLinkSelector = new RdLinkSelector(this.conn);
        List<RdLink> links = rdLinkSelector.loadByNodePid(nodePid, true);
        if (null != links && links.size() > 1 && null != links && !links.isEmpty()) {
            throw new Exception("此RDLink为主要素，并且被分离的node挂接了至少两根link，则不允许分离节点");
        }
        AbstractSelector abstractSelector = new AbstractSelector(RdSameLinkPart.class, this.conn);
        List<IRow> parts = abstractSelector.loadRowsByParentId(part.getGroupId(), true);
        if (newLinks.size() == 1) {
            for (IRow row : parts) {
                part = (RdSameLinkPart) row;
                if ("AD_LINK".equals(part.getTableName())) {
                    this.breakAd(requester);
                } else if ("ZONE_LINK".equals(part.getTableName())) {
                    this.breakZone(requester);
                } else if ("LU_LINK".equals(part.getTableName())) {
                    this.breakLu(requester);
                }
            }
        } else if (newLinks.size() > 1) {

        }
    }

    private void breakAd(String requester) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Command command =
                new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Command(JSONObject.fromObject(requester), requester);
        com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.OpTopo operation =
                new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.OpTopo(command, null, this.conn);
    }

    private void breakZone(String requester) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Command command =
                new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Command(JSONObject.fromObject(requester), requester);
        com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.OpTopo operation =
                new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.OpTopo(command, null, this.conn);
    }

    private void breakLu(String requester) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Command command =
                new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Command(JSONObject.fromObject(requester), requester);
        com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.OpTopo operation =
                new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.OpTopo(command, null, this.conn);
    }
}
