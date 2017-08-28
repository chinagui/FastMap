package com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.rdlinkwarning.RdLinkWarning;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.rd.rdlinkwarning.RdLinkWarningSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.speedlimit.RdSpeedlimitSelector;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ly on 2017/8/18.
 */
public class Operation implements IOperation {

    private Command command;

    private Connection conn;

    private RdLinkWarning rdLinkWarning;

    public Operation(Command command) {

        this.command = command;

        this.rdLinkWarning = command.getRdLinkWarning();
    }
    public Operation(Connection conn) {
        this.conn = conn;

    }


    @Override
    public String run(Result result) throws Exception {

        JSONObject content = command.getContent();

        if (content.containsKey("objStatus")
                && ObjStatus.UPDATE.toString().equals(content.getString("objStatus"))) {

            boolean isChanged = rdLinkWarning.fillChangeFields(content);

            if (isChanged) {

                result.insertObject(rdLinkWarning, ObjStatus.UPDATE, rdLinkWarning.pid());
            }
        }

        result.setPrimaryPid(rdLinkWarning.getPid());

        return null;
    }

    /**
     * 删除link维护警示信息
     *
     * @param linkPids 被删linkPids
     */
    public void updateByLinks(List<Integer> linkPids, Result result) throws Exception {

        if (conn == null) {

            return;
        }

        RdLinkWarningSelector selector = new RdLinkWarningSelector(conn);

        List<RdLinkWarning> warnings = selector.loadByLinks(linkPids, true);

        // 更新关联线为零
        for (RdLinkWarning warning : warnings) {

            warning.changedFields().put("linkPid", 0);

            result.insertObject(warning, ObjStatus.UPDATE, warning.pid());
        }
    }

    /**
     * 打断link维护
     *
     * @param oldLink 被打断linkPid
     */
    public void breakRdLink(RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {

        if (conn == null) {

            return;
        }

        RdLinkWarningSelector selector = new RdLinkWarningSelector(conn);

        List<RdLinkWarning> warnings = selector.loadByLink(oldLink.getPid(), true);

        for (RdLinkWarning warning : warnings) {

            int inLinkPid = 0;

            double distanceFlag = Double.MAX_VALUE;

            for (RdLink link : newLinks) {

                double distance = warning.getGeometry().distance(link.getGeometry());

                if (distance < distanceFlag) {

                    distanceFlag = distance;

                    inLinkPid = link.getPid();
                }
            }

            warning.changedFields().put("linkPid", inLinkPid);

            result.insertObject(warning, ObjStatus.UPDATE, warning.pid());
        }
    }

}
