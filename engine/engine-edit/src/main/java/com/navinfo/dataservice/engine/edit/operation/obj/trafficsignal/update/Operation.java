package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.trafficsignal.RdTrafficsignalSelector;

import net.sf.json.JSONObject;

/**
 * @author Zhang Xiaolong
 * @ClassName: Operation
 * @date 2016年7月20日 下午7:39:27
 * @Description: TODO
 */
public class Operation implements IOperation {

    private Command command;

    private Connection conn = null;

    public Operation(Command command) {
        this.command = command;
    }

    public Operation(Connection conn) {
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {

        RdTrafficsignal rdTrafficsignal = this.command.getRdTrafficsignal();

        JSONObject content = command.getContent();

        if (content.containsKey("objStatus")) {

            if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
                result.insertObject(rdTrafficsignal, ObjStatus.DELETE, rdTrafficsignal.pid());

                return null;
            } else {
                boolean isChanged = rdTrafficsignal.fillChangeFields(content);

                if (isChanged) {
                    result.insertObject(rdTrafficsignal, ObjStatus.UPDATE, rdTrafficsignal.pid());
                }
            }
        }

        return null;
    }

    /**
     * 打断link维护信号灯
     *
     * @param nodeLinkRelation 平滑修行分离点挂接线信息
     * @param linkPid          被打断的link
     * @param newLinks         新生成的link组
     * @param result
     * @throws Exception
     */
    public void breakRdLink(Map<RdNode, List<RdLink>> nodeLinkRelation, int linkPid, List<RdLink> newLinks, Result result) throws Exception {
        if (conn == null) {
            return;
        }
        RdTrafficsignalSelector selector = new RdTrafficsignalSelector(conn);
        List<RdTrafficsignal> rdTrafficsignals = selector.loadByLinkPid(true, linkPid);

        if (null != nodeLinkRelation && !nodeLinkRelation.isEmpty()) {
            List<Integer> catchIds = new ArrayList<>();
            for (Map.Entry<RdNode, List<RdLink>> entry : nodeLinkRelation.entrySet()) {
                if (entry.getValue().size() > 1) {
                    catchIds.add(entry.getKey().pid());
                }
            }
            List<RdTrafficsignal> trafficsignals = selector.loadByNodePids(catchIds, true);
            catchIds = new ArrayList<>();
            for (RdTrafficsignal trafficsignal : trafficsignals) {
                catchIds.add(trafficsignal.pid());
                result.insertObject(trafficsignal, ObjStatus.DELETE, trafficsignal.pid());
            }

            if (!catchIds.isEmpty()) {
                Iterator<RdTrafficsignal> iterator = rdTrafficsignals.iterator();
                while (iterator.hasNext()) {
                    if (catchIds.contains(iterator.next().pid()))
                        iterator.remove();
                }
            }
        }

        if (CollectionUtils.isNotEmpty(rdTrafficsignals)) {
            for (RdTrafficsignal rdTrafficsignal : rdTrafficsignals) {
                for (RdLink link : newLinks) {

                    if (link.getsNodePid() == rdTrafficsignal.getNodePid()
                            || link.geteNodePid() == rdTrafficsignal.getNodePid()) {

                        rdTrafficsignal.changedFields().put("linkPid", link.getPid());

                        result.insertObject(rdTrafficsignal, ObjStatus.UPDATE, rdTrafficsignal.pid());
                    }
                }
            }
        }
    }

    /**
     * 修改道路方向维护信号灯关系
     *
     * @param updateLink
     * @throws Exception
     */
    public List<RdTrafficsignal> updateRdCrossByModifyLinkDirect(RdLink updateLink) throws Exception {

        List<RdTrafficsignal> deleteTrafficList = new ArrayList<>();

        RdTrafficsignalSelector selector = new RdTrafficsignalSelector(conn);

        int direct = (int) updateLink.changedFields().get("direct");

        List<RdTrafficsignal> trafficsignals = selector.loadByLinkPid(true, updateLink.getPid());

        if (CollectionUtils.isNotEmpty(trafficsignals)) {
            for (RdTrafficsignal rdTrafficsignal : trafficsignals) {
                int nodePid = rdTrafficsignal.getNodePid();
                // 道路由双方向修改为单方向（且修改的方向为路口的退出方向）时，该link上的信号灯应删除。
                if (updateLink.getDirect() == 1 && direct == 2 && updateLink.getsNodePid() == nodePid) {
                    // return "请注意，修改道路方向，可能需要对下列路口维护信号灯信息：" +
                    // rdTrafficsignal.getPid();
                    deleteTrafficList.add(rdTrafficsignal);
                } else if (updateLink.getDirect() == 1 && direct == 3 && updateLink.geteNodePid() == nodePid) {
                    deleteTrafficList.add(rdTrafficsignal);
                }
                // 关联道路的方向为路口的进入方向，修改为路口的退出方向时，应删除该link上的信号灯。
                else if (updateLink.getDirect() == 2 && direct == 3) {
                    // return "请注意，修改道路方向，可能需要对下列路口维护信号灯信息：" +
                    // rdTrafficsignal.getPid();
                    deleteTrafficList.add(rdTrafficsignal);
                }
            }
        }
        return deleteTrafficList;
    }

    /**
     * 更新link方向需要添加信号灯的路口
     *
     * @param updateLink
     * @return
     * @throws Exception
     */
    public List<RdCross> getUpdateCross(RdLink updateLink) throws Exception {
        List<RdCross> crossList = new ArrayList<>();

        int direct = (int) updateLink.changedFields().get("direct");

        RdTrafficsignalSelector selector = new RdTrafficsignalSelector(conn);

        RdCrossSelector crossSelector = new RdCrossSelector(conn);

        int sNodePid = updateLink.getsNodePid();

        // 关联道路的方向为路口的退出方向，修改为路口的进入方向时，应创建该link上的信号灯。
        List<RdTrafficsignal> rdTrafficsignals = selector.loadByNodeId(true, sNodePid);

        if (CollectionUtils.isNotEmpty(rdTrafficsignals)) {
            if (updateLink.getDirect() == 2 && (direct == 3 || direct == 1)) {
                // return "请注意，修改道路方向，可能需要对下列路口LINK维护信号灯信息（LINK_PID）：" +
                // updateLink.getPid();

                RdCross cross = crossSelector.loadCrossByNodePid(sNodePid, false);

                crossList.add(cross);
            }
        }

        int eNodePid = updateLink.geteNodePid();

        // 关联道路的方向为路口的退出方向，修改为路口的进入方向时，应创建该link上的信号灯。
        List<RdTrafficsignal> rdTrafficsignals2 = selector.loadByNodeId(true, eNodePid);

        if (CollectionUtils.isNotEmpty(rdTrafficsignals2)) {
            if (updateLink.getDirect() == 2 && (direct == 3 || direct == 1)) {
                // return "请注意，修改道路方向，可能需要对下列路口LINK维护信号灯信息（LINK_PID）：" +
                // updateLink.getPid();
                RdCross cross = crossSelector.loadCrossByNodePid(eNodePid, false);

                crossList.add(cross);
            }
        }

        return crossList;
    }

    /**
     * 修改link方向对信号灯影响
     *
     * @return
     * @throws Exception
     */
    public List<AlertObject> getUpdateLinkDirectInfectData(RdLink updateLink, JSONObject jsonObj) throws Exception {

        boolean flag = updateLink.fillChangeFields(jsonObj);

        List<AlertObject> alertList = new ArrayList<>();

        if (flag) {
            List<RdTrafficsignal> trafficSignalList = updateRdCrossByModifyLinkDirect(updateLink);

            for (RdTrafficsignal rdTrafficsignal : trafficSignalList) {

                AlertObject alertObj = new AlertObject();

                alertObj.setObjType(rdTrafficsignal.objType());

                alertObj.setPid(rdTrafficsignal.getPid());

                alertObj.setStatus(ObjStatus.DELETE);

                if (!alertList.contains(alertObj)) {
                    alertList.add(alertObj);
                }
            }
        }

        return alertList;
    }

    /**
     * 修改link方向对信号灯影响，提示需要在路口创建信号灯
     *
     * @return
     * @throws Exception
     */
    public List<AlertObject> getUpdateLinkDirectInfectCross(RdLink updateLink, JSONObject jsonObj) throws Exception {

        boolean flag = updateLink.fillChangeFields(jsonObj);

        List<AlertObject> alertList = new ArrayList<>();

        if (flag) {

            List<RdCross> crossList = getUpdateCross(updateLink);

            for (RdCross cross : crossList) {

                AlertObject alertObj = new AlertObject();

                alertObj.setObjType(cross.objType());

                alertObj.setPid(cross.getPid());

                alertObj.setStatus(ObjStatus.UPDATE);

                if (!alertList.contains(alertObj)) {
                    alertList.add(alertObj);
                }
            }
        }

        return alertList;
    }
}
