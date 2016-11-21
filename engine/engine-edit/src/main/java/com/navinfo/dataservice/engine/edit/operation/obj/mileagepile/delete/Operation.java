package com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.delete;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.mileagepile.RdMileagepile;
import com.navinfo.dataservice.dao.glm.selector.rd.mileagepile.RdMileagepileSelector;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaixin on 2016/11/9 0009.
 */
public class Operation implements IOperation {
    private Command command;

    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    public Operation(Command command) {
        this.command = command;
    }

    @Override
    public String run(Result result) throws Exception {
        result.insertObject(command.getMileagepile(), ObjStatus.DELETE, command.getObjId());
        return null;
    }

    /**
     * 用于删除RdLink时维护里程桩
     *
     * @param result   结果集
     * @param linkPids 待删除线
     * @return
     * @throws Exception
     */
    public String deleteRdMileagepile(Result result, List<Integer> linkPids) throws Exception {
        RdMileagepileSelector selector = new RdMileagepileSelector(conn);
        // 存储待更新里程桩信息
        List<RdMileagepile> mileagepiles = selector.loadByLinkPids(linkPids, true);
        // 更新里程桩关联线为零
        for (RdMileagepile mileagepile : mileagepiles) {
            mileagepile.changedFields().put("linkPid", 0);
            result.insertObject(mileagepile, ObjStatus.UPDATE, mileagepile.pid());
        }
        return null;
    }

    /**
     * 删除link对里程桩的删除影响
     *
     * @return
     * @throws Exception
     */
    public List<AlertObject> getDeleteRdMileagepileInfectData(int linkPid, Connection conn) throws Exception {
        RdMileagepileSelector selector = new RdMileagepileSelector(conn);
        List<RdMileagepile> mileagepiles = selector.loadByLinkPid(linkPid, true);
        List<AlertObject> alertList = new ArrayList<>();
        for (RdMileagepile mileagepile : mileagepiles) {
            AlertObject alertObj = new AlertObject();
            alertObj.setObjType(mileagepile.objType());
            alertObj.setPid(mileagepile.getPid());
            alertObj.setStatus(ObjStatus.UPDATE);
            if (!alertList.contains(alertObj)) {
                alertList.add(alertObj);
            }
        }
        return alertList;
    }
}
