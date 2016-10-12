package com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.depart;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeedVia;
import com.navinfo.dataservice.dao.glm.selector.rd.variablespeed.RdVariableSpeedSelector;

import java.sql.Connection;
import java.util.*;

/**
 * Created by chaixin on 2016/10/9 0009.
 */
public class Operation {
    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    /**
     * 维护上下线分离对可变限速的影响
     *
     * @param links      分离线
     * @param leftLinks  分离后左线
     * @param rightLinks 分离后右线
     * @param result     结果集
     * @return
     * @throws Exception
     */
    public String updownDepart(List<RdLink> links, Map<Integer, RdLink> leftLinks, Map<Integer, RdLink> rightLinks, Result result) throws Exception {
        RdVariableSpeedSelector selector = new RdVariableSpeedSelector(conn);
        for (RdLink link : links) {
            Set<RdVariableSpeed> variableSpeeds = new HashSet<>();
            // 1.当已经参与可变限速制作的单线link变为上下线分离时，将整组可变限速信息删除(进入线或退出线)
            variableSpeeds.addAll(selector.loadRdVariableSpeedByLinkPid(link.pid(), true));
            if (!variableSpeeds.isEmpty()) {
                for (RdVariableSpeed variableSpeed : variableSpeeds)
                    result.insertObject(variableSpeed, ObjStatus.DELETE, variableSpeed.pid());
                variableSpeeds.clear();
            }
            // 2.当目标link上的点已经参与制作可变限速
            Set<Integer> tmpNodePids = new HashSet<Integer>();
            tmpNodePids.add(link.getsNodePid());
            tmpNodePids.add(link.geteNodePid());
            List<Integer> nodePids = Arrays.asList(tmpNodePids.toArray(new Integer[]{})).subList(1, tmpNodePids.size() - 1);
            variableSpeeds.addAll(selector.loadRdVariableSpeedByNodePids(nodePids, true));
            if (!variableSpeeds.isEmpty()) {
                for (RdVariableSpeed variableSpeed : variableSpeeds) {
                    if (!result.getDelObjects().contains(variableSpeeds))
                        result.insertObject(variableSpeed, ObjStatus.DELETE, variableSpeed.pid());
                }
                variableSpeeds.clear();
            }
            // 3.经过线为目标link
            variableSpeeds.addAll(selector.loadRdVariableSpeedByViaLinkPid(link.pid(), true));
            // 4.经过线的起始点为目标link的经过点（未确认暂不放开）
//            variableSpeeds.addAll(selector.loadRdVariableSpeedByVianodeIds(nodePids,true));
            if (!variableSpeeds.isEmpty()) {
                for (RdVariableSpeed variableSpeed : variableSpeeds) {
                    for (IRow row : variableSpeed.getVias()) {
                        result.insertObject(row, ObjStatus.DELETE, row.parentPKValue());
                    }
                }
            }
        }
        return "";
    }
}
