package com.navinfo.dataservice.engine.edit.operation.obj.cmg.link.create;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildnodeSelector;
import com.navinfo.dataservice.engine.edit.utils.BasicServiceUtils;
import com.navinfo.dataservice.engine.edit.utils.CmgLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.jdbc.driver.Const;
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
            this.initCommandPara();
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

    /***
     * 新增link参数初始化 1.几何保留五位精度 2.捕捉node几何 重新替换link的形状点 ，为了保持精度
     *
     * @throws Exception
     */
    private void initCommandPara() throws Exception {
        CmgBuildnodeSelector nodeSelector = new CmgBuildnodeSelector(this.conn);
        JSONArray array = command.getCatchLinks();
        for (int i = 0; i < array.size(); i++) {
            JSONObject jo = array.getJSONObject(i);
            // 如果有挂接的node 用node的几何替换对应位置线的形状点
            if (jo.containsKey("nodePid")) {
                IRow row = nodeSelector.loadById(jo.getInt("nodePid"), true, true);
                int seqNum = jo.getInt("seqNum");
                CmgBuildnode node = (CmgBuildnode) row;
                Geometry geom = GeoTranslator.transform(node.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION);
                jo.put("lon", geom.getCoordinate().x);
                jo.put("lat", geom.getCoordinate().y);
                command.getGeometry().getCoordinates()[seqNum] = geom.getCoordinate();
            }
            // 挂接link精度处理
            if (jo.containsKey("linkPid")) {
                JSONObject geoPoint = new JSONObject();
                geoPoint.put("type", "Point");
                geoPoint.put("coordinates", new double[]{jo.getDouble("lon"), jo.getDouble("lat")});
                Geometry geometry = GeoTranslator.geojson2Jts(geoPoint, 1, 5);
                jo.put("lon", geometry.getCoordinate().x);
                jo.put("lat", geometry.getCoordinate().y);
            }
        }
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
