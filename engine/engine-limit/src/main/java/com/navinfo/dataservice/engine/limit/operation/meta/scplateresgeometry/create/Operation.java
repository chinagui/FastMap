package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.create;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.Utils.PidApply;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresFace;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresLink;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresGeometry;
import com.navinfo.dataservice.engine.limit.search.limit.ScPlateresFaceSearch;
import com.navinfo.dataservice.engine.limit.search.limit.ScPlateresLinkSearch;
import com.navinfo.navicommons.database.QueryRunner;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.List;

public class Operation implements IOperation {

    private static Logger logger = Logger.getLogger(Operation.class);
    private Command command;
    private Connection conn;

    private Connection limitConn;

    public Operation(Command command, Connection conn,Connection limitConn) {

        this.command = command;

        this.conn = conn;

        this.limitConn = limitConn;
    }

    @Override
    public String run(Result result) throws Exception {

        createByTmpGeo(result);

        return null;
    }

    private void createByTmpGeo(Result result) throws Exception {

        String groupId = this.command.getGroupId();

        ScPlateresFaceSearch faceSearch = new ScPlateresFaceSearch(limitConn);

        List<ScPlateresFace> faces = faceSearch.loadByGroupId(this.command.getGroupId());

        for (int i = 0; i < faces.size(); i++) {

            ScPlateresGeometry geometry = new ScPlateresGeometry();

            String geomId = PidApply.getInstance(this.conn).pidForInsertGeometry(groupId,
                    LimitObjType.SCPLATERESGEOMETRY, i);

            geometry.setGeometryId(geomId);

            geometry.setGroupId(groupId);

            Geometry geom = faces.get(i).getGeometry();

            if (geom.getGeometryType().equals("LineString")) {
                throw new Exception("临时面图层包含线几何：" + geomId);
            }

            geometry.setGeometry(geom);

            geometry.setBoundaryLink(faces.get(i).getBoundaryLink());

            result.insertObject(geometry, ObjStatus.INSERT, geometry.getGeometryId());
        }

        ScPlateresLinkSearch linkSearch = new ScPlateresLinkSearch(limitConn);

        List<ScPlateresLink> links = linkSearch.loadByGroupId(this.command.getGroupId());

        for (int i = 0; i < links.size(); i++) {

            ScPlateresGeometry geometry = new ScPlateresGeometry();

            String geomId = PidApply.getInstance(this.conn).pidForInsertGeometry(groupId,
                    LimitObjType.SCPLATERESGEOMETRY, i + faces.size());

            geometry.setGeometryId(geomId);

            geometry.setGroupId(groupId);

            Geometry geom = links.get(i).getGeometry();

            geometry.setGeometry(geom);

            geometry.setBoundaryLink(links.get(i).getBoundaryLink());

            result.insertObject(geometry, ObjStatus.INSERT, geometry.getGeometryId());
        }

        delTempGeoObj(groupId);
    }

    /**
     * 删除Group对应的临时link、face几何
     */
    private void delTempGeoObj(String groupId) throws Exception {
        try {

            QueryRunner runner = new QueryRunner();

            //删除Group对应的临时link几何
            String strSql = "DELETE SC_PLATERES_LINK WHERE GROUP_ID='" + groupId + "'";

            runner.execute(limitConn, strSql);

            //删除Group对应的临时link几何
            strSql = "DELETE SC_PLATERES_FACE WHERE GROUP_ID='" + groupId + "'";

            runner.execute(limitConn, strSql);

            logger.info(" 删除Group=" + groupId + "对应的临时link、face几何成功");

        } catch (Exception e) {

            String errStr = " 删除Group=" + groupId + "对应的临时link、face几何失败";

            logger.error(errStr);

            throw new Exception(errStr);
        }
    }

}
