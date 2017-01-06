package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

public class CheckCodeKind extends baseRule {

    public CheckCodeKind() {

    }

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        List<Integer> linkPidList = new ArrayList<Integer>();
        for (IRow obj : checkCommand.getGlmList()) {
            if (obj instanceof RdLink) {
                RdLink rdLink = (RdLink) obj;

                if (linkPidList.contains(rdLink.getPid())) {
                    continue;
                } else {
                    linkPidList.add(rdLink.getPid());
                }

                // rd_link_name
                List<IRow> names = new AbstractSelector(RdLinkName.class, getConn()).loadRowsByParentId(rdLink.getPid
                        (), false);

                int kind = 0;

                Map<String, Object> changeFields = rdLink.changedFields();
                if (changeFields != null && changeFields.containsKey("kind")) {
                    kind = (int) changeFields.get("kind");
                } else {
                    kind = rdLink.getKind();
                }
                // LINK种别为高速、城市高速、国道时，主从CODE字段设为1，其他为0
                if (kind == 1 || kind == 2 || kind == 3) {
                    for (IRow name : names) {
                        RdLinkName rdLinkName = (RdLinkName) name;
                        if (rdLinkName.getCode() != 1) {
                            String target = "[RD_LINK," + rdLink.getPid() + "]";
                            this.setCheckResult(rdLink.getGeometry(), target, rdLink.getMeshId());
                        }
                    }
                } else {
                    for (IRow name : names) {
                        RdLinkName rdLinkName = (RdLinkName) name;
                        if (rdLinkName.getCode() != 0) {
                            String target = "[RD_LINK," + rdLink.getPid() + "]";
                            this.setCheckResult(rdLink.getGeometry(), target, rdLink.getMeshId());
                        }
                    }
                }
            } else if (obj instanceof RdLinkName) {
                RdLinkName rdLinkName = (RdLinkName) obj;
                int linkPid = rdLinkName.getLinkPid();
                if (linkPidList.contains(linkPid)) {
                    continue;
                } else {
                    linkPidList.add(linkPid);
                }

                RdLinkSelector rdSelector = new RdLinkSelector(getConn());
                RdLink rdLink = (RdLink) rdSelector.loadByIdOnlyRdLink(linkPid, false);

                int code = 0;
                Map<String, Object> changeFields = rdLinkName.changedFields();
                if (changeFields != null && changeFields.containsKey("kind")) {
                    code = (int) changeFields.get("code");
                } else {
                    code = rdLinkName.getCode();
                }

                int kind = rdLink.getKind();
                // LINK种别为高速、城市高速、国道时，主从CODE字段设为1，其他为0
                if (kind == 1 || kind == 2 || kind == 3) {
                    if (code != 1) {
                        String target = "[RD_LINK," + rdLink.getPid() + "]";
                        this.setCheckResult(rdLink.getGeometry(), target, rdLink.getMeshId());
                    }
                } else {
                    if (code != 0) {
                        String target = "[RD_LINK," + rdLink.getPid() + "]";
                        this.setCheckResult(rdLink.getGeometry(), target, rdLink.getMeshId());
                    }
                }
            }
        }
    }


}
