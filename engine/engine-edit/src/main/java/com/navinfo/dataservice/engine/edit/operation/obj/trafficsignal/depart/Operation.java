package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.depart;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.dao.glm.selector.rd.trafficsignal.RdTrafficsignalSelector;

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
     * 维护上下线分离对信号灯的影响
     *
     * @param links      分离线
     * @param leftLinks  分离后左线
     * @param rightLinks 分离后右线
     * @param result     结果集
     * @return
     * @throws Exception
     */
    public String updownDepart(List<RdLink> links, Map<Integer, RdLink> leftLinks, Map<Integer, RdLink> rightLinks, Result result) throws Exception {
        RdTrafficsignalSelector selector = new RdTrafficsignalSelector(conn);
        Integer[] linkPids = leftLinks.keySet().toArray(new Integer[]{});
        int length = linkPids.length;
        // 1.信号灯进入点为分离线的经过点时删除信号灯
        Set<Integer> tmpNodePids = new HashSet<>();
        for (RdLink link : links) {
            tmpNodePids.add(link.getsNodePid());
            tmpNodePids.add(link.geteNodePid());
        }
        Integer[] tmpArr = tmpNodePids.toArray(new Integer[]{});
        List<Integer> nodePids = new ArrayList<>(tmpNodePids).subList(1, tmpNodePids.size() - 1);
        List<RdTrafficsignal> trafficsignals = selector.loadByNodePids(nodePids, true);
        for (RdTrafficsignal trafficsignal : trafficsignals) {
            result.insertObject(trafficsignal, ObjStatus.DELETE, trafficsignal.pid());
        }
        RdLink link = null;
        // 2.信号灯进入点为分离线的起点时更新信号灯进入线
        trafficsignals = selector.loadByNodeId(true, tmpArr[0]);
        if (!trafficsignals.isEmpty()) {
            for (RdTrafficsignal trafficsignal : trafficsignals) {
                for (int i = 0; i < links.size(); i++) {
                    if (trafficsignal.getLinkPid() == links.get(i).pid()) {
                        trafficsignal.changedFields().put("linkPid", leftLinks.get(i).pid());
                        result.insertObject(trafficsignal, ObjStatus.UPDATE, trafficsignal.pid());
                        break;
                    }
                }
            }
        }
        // 3.信号灯进入点为分离线的终点时更新信号灯进入线
        trafficsignals = selector.loadByNodeId(true, tmpArr[tmpArr.length - 1]);
        if (!trafficsignals.isEmpty()) {
            for (RdTrafficsignal trafficsignal : trafficsignals) {
                for (int i = 0; i < links.size(); i++) {
                    if (trafficsignal.getLinkPid() == links.get(i).pid()) {
                        trafficsignal.changedFields().put("linkPid", rightLinks.get(i).pid());
                        result.insertObject(trafficsignal, ObjStatus.UPDATE, trafficsignal.pid());
                        break;
                    }
                }
            }
        }
        return "";
    }
}
