package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.delete;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresGroup;
import com.navinfo.navicommons.database.QueryRunner;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;

public class Operation implements IOperation {

    private Connection conn=null;

    private Command command;

    public Operation(Command command,Connection conn) {

        this.command = command;

        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {

        delGroup(result);

        return null;
    }

    private void delGroup(Result result)throws Exception {

        for (ScPlateresGroup group:command.getGroups()) {

            result.insertObject(group, ObjStatus.DELETE, group.getGroupId());

            delRelationObj(group.getGroupId(),  result);
        }
    }

    private void delRelationObj(String groupId, Result result) throws Exception {

        delTempGeoObj( groupId);

        delScplateresGeometry(groupId, result);

        delScplateresManoeuvre(groupId, result);
    }

    private void delScplateresGeometry(String groupId, Result result) throws Exception {

        com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.delete.Operation Operation =
                new com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.delete.Operation(this.conn);

        Operation.delByGroupId(groupId, result);
    }

    private void delScplateresManoeuvre(String groupId, Result result) throws Exception {

        com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.delete.Operation Operation =
                new com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.delete.Operation(this.conn);

        Operation.delByGroupId(groupId, result);
    }

    /**
     * 删除Group对应的临时link、face几何
     */
    private String delTempGeoObj(String groupId)
    {
        Connection conn = null;
        try {
            conn = DBConnector.getInstance().getLimitConnection();
            QueryRunner runner = new QueryRunner();

            //删除Group对应的临时link几何
            String strSql = "DELETE SC_PLATERES_LINK WHERE GROUP_ID='" + groupId + "'";

            runner.execute(conn, strSql);

            //删除Group对应的临时link几何
            strSql = "DELETE SC_PLATERES_FACE WHERE GROUP_ID='" + groupId + "'";

            runner.execute(conn, strSql);

            conn.commit();

            return null;

        } catch (Exception e) {

            return " 删除Group=" + groupId + "对应的临时link、face几何失败";

        } finally {

            DbUtils.closeQuietly(conn);
        }
    }
}
