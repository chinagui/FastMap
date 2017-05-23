package com.navinfo.dataservice.engine.edit.operation.obj.hgwg.delete;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.hgwg.RdHgwgLimit;
import com.navinfo.dataservice.dao.glm.selector.rd.hgwg.RdHgwgLimitSelector;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaixin on 2016/11/8 0008.
 */
public class Operation implements IOperation {

    private Command command;

    private Connection conn;

    public Operation(Command command) {
        this.command = command;
    }

    public Operation(Connection conn) {
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        RdHgwgLimit hgwgLimit = command.getHgwgLimit();
        result.insertObject(hgwgLimit, ObjStatus.DELETE, hgwgLimit.pid());
        return null;
    }

    /**
     * 用于删除RdLink时维护限高限重
     *
     * @param result   结果集
     * @param linkPids 待删除线
     * @return
     * @throws Exception
     */
    public String deleteHgwgLimit(Result result, List<Integer> linkPids) throws Exception {
        RdHgwgLimitSelector selector = new RdHgwgLimitSelector(conn);
        // 存储待删除限高限重信息
        List<RdHgwgLimit> hgwgLimits = new ArrayList<>();
        for (Integer linkPid : linkPids) {
            hgwgLimits.addAll(selector.loadByLinkPid(linkPid, true));
        }
        // 删除限高限重
        for (RdHgwgLimit hgwgLimit : hgwgLimits) {
            result.insertObject(hgwgLimit, ObjStatus.DELETE, hgwgLimit.pid());
        }
        return null;
    }

    /**
     * 删除link对限高限重的删除影响
     *
     * @return
     * @throws Exception
     */
    public List<AlertObject> getDeleteRdHgwgInfectData(int linkPid, Connection conn) throws Exception {
        RdHgwgLimitSelector selector = new RdHgwgLimitSelector(conn);
        List<RdHgwgLimit> hgwgLimits = selector.loadByLinkPid(linkPid, true);
        List<AlertObject> alertList = new ArrayList<>();
        for (RdHgwgLimit hgwgLimit : hgwgLimits) {
            AlertObject alertObj = new AlertObject();
            alertObj.setObjType(hgwgLimit.objType());
            alertObj.setPid(hgwgLimit.getPid());
            alertObj.setStatus(ObjStatus.DELETE);
            if (!alertList.contains(alertObj)) {
                alertList.add(alertObj);
            }
        }
        return alertList;
    }

    /**
     * 用于删除RdLink时维护限高限重
     *
     * @param result   结果集
     * @param linkPids 待删除线
     * @return
     * @throws Exception
     */
    public String deleteByLinks(Result result, List<Integer> linkPids) throws Exception {

        RdHgwgLimitSelector selector = new RdHgwgLimitSelector(conn);
        // 存储待删除限高限重信息
        List<RdHgwgLimit> hgwgLimits = selector.loadByLinkPids(linkPids, true);


        // 删除限高限重
        for (RdHgwgLimit hgwgLimit : hgwgLimits) {

            result.insertObject(hgwgLimit, ObjStatus.DELETE, hgwgLimit.pid());

        }
        return null;
    }
}
