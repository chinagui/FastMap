package com.navinfo.dataservice.engine.edit.operation.topo.move.movecmgnode;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import java.sql.Connection;

/**
 * Created by crayeres on 2017/4/12.
 */
public class Operation implements IOperation {

    /**
     * 参数
     */
    private Command command;

    /**
     * 数据库链接
     */
    private Connection conn;

    public Operation(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        // 处理CMG-FACE

        // 处理CMG-NODE
        Geometry geometry = new GeometryFactory().createPoint(
                new Coordinate(command.getLongitude(), command.getLatitude()));
        command.getCmgnode().changedFields().put("geometry", geometry);
        return null;
    }
}
