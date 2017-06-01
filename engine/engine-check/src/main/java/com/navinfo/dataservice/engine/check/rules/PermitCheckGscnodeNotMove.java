package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.model.utils.CheckGeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Rdlink	html	PERMIT_CHECK_GSCNODE_NOT_MOVE
 * 后台	创建或修改link，节点不能到已有的立交点处，请先删除立交关系
 */

public class PermitCheckGscnodeNotMove extends baseRule {

    /**
     * 日志记录
     */
    private static Logger logger = Logger.getLogger(PermitCheckGscnodeNotMove.class);

    private final static List<ObjType> OBJ_TYPES =
            Arrays.asList(ObjType.RDNODE, ObjType.LCNODE, ObjType.CMGBUILDNODE, ObjType.RWNODE);

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (CheckGeometryUtils.notContains(OBJ_TYPES, row.objType())) {
                continue;
            }
            if (row.status() == ObjStatus.DELETE) {
                continue;
            } else if (row.status() == ObjStatus.UPDATE && hasModifyGeo(row)) {
                continue;
            }

            Geometry geometry = CheckGeometryUtils.getGeometry(row);
            if (null == geometry) {
                logger.error(String.format("ObjType: %s, Pid: %d, [对象几何获取过程出错]", row.tableName(), row.parentPKValue()));
                continue;
            }

            logger.debug(String.format("CHECK003 {ObjType: %s, Pid: %d, Geometry: %s}", row.tableName(), row.parentPKValue(),
                        geometry.toString()));

            geometry = GeoTranslator.transform(geometry, GeoTranslator.dPrecisionMap, 5);

            StringBuffer sb = new StringBuffer();
            sb.append("SELECT RGL.LINK_PID FROM RD_GSC RG, RD_GSC_LINK RGL WHERE RG.U_RECORD <> 2 AND RG.PID = RGL.PID AND");
            for (Coordinate coor : geometry.getCoordinates()) {
                sb.append("(RG.GEOMETRY.SDO_POINT.X = ").append(coor.x + " ");
                sb.append("AND RG.GEOMETRY.SDO_POINT.Y = ").append(coor.y + " ").append(") ").append("OR ");
            }
            String sql = sb.substring(0, sb.lastIndexOf("OR")) + " AND RGL.LINK_PID != " + row.parentPKValue();
            DatabaseOperator getObj = new DatabaseOperator();
            List<Object> resultList = getObj.exeSelect(this.getConn(), sql);
            if (!resultList.isEmpty()) {
                this.setCheckResult("", String.format("[%s,%d]", row.tableName().toUpperCase(), row.parentPKValue()), 0);
                return;
            }
        }
    }

    /**
     * 是否对几何进行了修改
     * @param row 数据对象
     * @return true: 修改, false：未修改.
     */
    private boolean hasModifyGeo(IRow row) {
        boolean flag = false;
        if (row.changedFields().containsKey("geometry")) {
            flag = true;
        }
        return flag;
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
    }
}


