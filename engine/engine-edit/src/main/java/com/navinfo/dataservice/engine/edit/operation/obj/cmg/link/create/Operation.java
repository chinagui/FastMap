package com.navinfo.dataservice.engine.edit.operation.obj.cmg.link.create;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.engine.edit.utils.BasicServiceUtils;
import com.navinfo.dataservice.engine.edit.utils.CmgLinkOperateUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * @Title: Operation
 * @Package: com.navinfo.dataservice.engine.edit.operation.obj.cmg.link.create
 * @Description: 创建CMG-LINK具体操作
 * @Author: Crayeres
 * @Date: 2017/4/10
 * @Version: V1.0
 */
public class Operation implements IOperation {

    /**
     * 参数
     */
    private Command command;

    /**
     * 数据库连接
     */
    private Connection conn;

    /**
     * 初始化CMG-LINK新增操作类
     * @param command 参数
     */
    public Operation(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        Map<Geometry, JSONObject> map = new HashMap<>();

        if (!CollectionUtils.isEmpty(command.getCatchLinks())) {
            map = CmgLinkOperateUtils.splitLink(command.getGeometry(), command.getsNodePid(), command.geteNodePid(),
                    command.getCatchLinks(), result);
        } else if (CollectionUtils.isEmpty(map)) {
            JSONObject se = CmgLinkOperateUtils.createCmglinkEndpoint(command.getGeometry(), command.getsNodePid(),
                    command.geteNodePid(), result);
            map.put(command.getGeometry(), se);
        }

        // 创建CMG-LINK
        this.createCmglink(map, result);
        // 挂接的线被打断的操作
        this.breakLine(result);

        return null;
    }

    /**
     *
     * @param map Geometry-线的几何 JSONOBJECT-起始点JSON对象
     * @param result 结果集
     * @throws Exception 创建CMG-LINK出错
     */
    private void createCmglink(Map<Geometry, JSONObject> map, Result result) throws Exception {
        for (Map.Entry<Geometry, JSONObject> entry: map.entrySet()) {
            CmgLinkOperateUtils.createCmglink(entry.getKey(), entry.getValue().getInt("s"),
                    entry.getValue().getInt("e"), result, true);
        }
    }

    /**
     * CMG-LINK打断具体操作<br>
     * 1.循环挂接的线<br>
     * 2.如果有被打断操作执行打断功能
     * @param result 结果集
     * @exception Exception 打断CMG-LINK出错
     */
    private void breakLine(Result result) throws Exception {
        // 处理连续打断参数
        JSONArray resultArr = BasicServiceUtils.getBreakArray(command.getCatchLinks());
        // 组装打断操作流程
        for (int i = 0; i < resultArr.size(); i++) {
            JSONObject obj = resultArr.getJSONObject(i);
            JSONObject breakJson = BasicServiceUtils.getBreaksPara(obj, this.command.getDbId());
            // 组装打断线的参数
            // 保证是同一个连接
            com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint.Command breakCommand = new com
                    .navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint.Command(breakJson, breakJson.toString());
            com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint.Process breakProcess = new com
                    .navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint.Process(breakCommand, result, conn);
            breakProcess.innerRun();
        }
    }
}
