package com.navinfo.dataservice.engine.edit.operation.obj.rdcross.depart;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;

import java.sql.Connection;
import java.util.*;

/**
 * Created by chaixin on 2016/10/11 0011.
 */
public class Operation {
    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    /**
     * 维护上下线分离对RdCross的影响
     *
     * @param links      分离线
     * @param leftLinks  分离后左线
     * @param rightLinks 分离后右线
     * @param result     结果集
     * @return
     * @throws Exception
     */
    public String updownDepart(List<RdLink> links, Map<Integer, RdLink> leftLinks, Map<Integer, RdLink> rightLinks, Result result) throws Exception {
        RdCrossSelector selector = new RdCrossSelector(conn);
        // 1.路口点为目标link的经过点
        List<Integer> nodePids = CalLinkOperateUtils.calNodePids(links);
        if (!nodePids.isEmpty()) {
            List<RdCross> crosses = selector.loadRdCrossByNodeOrLink(nodePids, new ArrayList<Integer>(), true);
            for (RdCross cross : crosses) {
                result.insertObject(cross, ObjStatus.DELETE, cross.pid());
                for (IRow row : cross.getNodes()) {
                    nodePids.add(((RdCrossNode) row).getNodePid());
                }
            }
        }

        // 2.维护分离link的交叉口道路形态
        RdLinkSelector linkSelector = new RdLinkSelector(conn);
        List<RdLink> allDelLinks = linkSelector.loadByNodePids(nodePids, false);
        for (RdLink link : allDelLinks) {
            RdLink leftLink = leftLinks.get(link.pid());
            RdLink rightLink = rightLinks.get(link.pid());
            if (null != leftLink) {
                this.updateLinkForm(leftLink, result);
            }
            if (null != rightLink) {
                this.updateLinkForm(rightLink, result);
            }
            if (null == leftLink && null == rightLink) {
                link = (RdLink) linkSelector.loadById(link.pid(), true);
                if (isTargetLink(link, links)) continue;
                this.updateLinkForm(link, result);
            }
        }
        return "";
    }

    private void updateLinkForm(RdLink link, Result result) {
        for (IRow row : link.getForms()) {
            RdLinkForm form = (RdLinkForm) row;
            if (form.getFormOfWay() == 50) {
                if (link.getForms().size() == 1) {
                    form.changedFields().put("formOfWay", 1);
                    result.insertObject(form, ObjStatus.UPDATE, form.parentPKValue());
                } else {
                    result.insertObject(form, ObjStatus.DELETE, form.parentPKValue());
                }
            }
        }
    }

    private boolean isTargetLink(RdLink link, List<RdLink> links) {
        boolean result = false;
        for (RdLink l : links) {
            if (link.pid() == l.pid())
                return true;
        }
        return result;
    }
}
