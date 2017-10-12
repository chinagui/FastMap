package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.delete;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresGeometry;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresGeometrySearch;

import java.sql.Connection;
import java.util.List;

public class Operation implements IOperation {

    private Connection conn = null;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    private Command command;

    public Operation(Command command, Connection conn) {
        this.command = command;

        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {

        delGeometry(result);

        return null;
    }

    private void delGeometry(Result result) throws Exception {

        for (ScPlateresGeometry geometry : command.getGeometrys()) {

            result.insertObject(geometry, ObjStatus.DELETE, geometry.getGroupId());

            delRelationObj(geometry.getGeometryId(), result);
        }
    }

    public void delByGroupId(String groupId, Result result) throws Exception {

        if (this.conn == null) {
            return;
        }

        ScPlateresGeometrySearch search = new ScPlateresGeometrySearch(this.conn);

        List<ScPlateresGeometry> geometrys = search.loadByGroupId(groupId);

        for (ScPlateresGeometry geometry : geometrys) {

            result.insertObject(geometry, ObjStatus.DELETE, geometry.getGroupId());

            delRelationObj(geometry.getGeometryId(), result);
        }
    }

    private void delRelationObj(String geometryId, Result result) throws Exception {

        if (this.conn == null) {
            return;
        }

        com.navinfo.dataservice.engine.limit.operation.meta.scplateresrdlink.delete.Operation operation =
                new com.navinfo.dataservice.engine.limit.operation.meta.scplateresrdlink.delete.Operation(this.conn);

        operation.delByGeometryId(geometryId, result);
    }
}
