package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.model.utils.CheckGeometryUtils;
import com.navinfo.navicommons.geo.computation.GeometryTypeName;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * @Title: Check003
 * @Package: com.navinfo.dataservice.engine.check.rules
 * @Description: 不允许去除有立交关系的形状点、node
 * @Author: Crayeres
 * @Date: 05/09/17
 * @Version: V1.0
 */
public class Check003 extends baseRule {

    private final static List<ObjType> OBJ_TYPES =
            Arrays.asList(ObjType.LCNODE, ObjType.LCLINK, ObjType.LUNODE, ObjType.LULINK, ObjType.CMGBUILDNODE,
                    ObjType.CMGBUILDLINK,ObjType.RWNODE, ObjType.RWLINK);

    /**
     * 日志记录
     */
    private Logger logger = Logger.getLogger(Check003.class);

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (CheckGeometryUtils.notContains(OBJ_TYPES, row.objType())) {
                continue;
            }
            if (row.status() != ObjStatus.UPDATE || !hasModifyGeo(row)) {
                continue;
            }

            Geometry geometry = CheckGeometryUtils.getGeometry(row);

            if (null == geometry) {
                logger.error(String.format("ObjType: %s, Pid: %d, [对象几何获取过程出错]", row.tableName(), row.parentPKValue()));
            } else {
                logger.debug(String.format("CHECK003 {ObjType: %s, Pid: %d, Geometry: %s}", row.tableName(), row.parentPKValue(),
                        geometry.toString()));

                geometry = GeoTranslator.transform(geometry, GeoTranslator.dPrecisionMap, 5);
                DatabaseOperator databaseOperator = new DatabaseOperator();
                if (GeometryTypeName.POINT.equals(geometry.getGeometryType())) {
                    checkGscGeometry(row, geometry.getCoordinate(), databaseOperator);
                } else if (GeometryTypeName.LINESTRING.equals(geometry.getGeometryType())) {
                    for (Coordinate coor : geometry.getCoordinates()) {
                        checkGscGeometry(row, coor, databaseOperator);
                    }
                }
            }
        }
    }

    /**
     * 检查该点位是否为立交点
     * @param coor 点位
     * @param databaseOperator 数据库操作
     * @throws Exception 查询失败
     */
    private void checkGscGeometry(IRow row, Coordinate coor, DatabaseOperator databaseOperator) throws Exception {
        String sql = "SELECT * FROM RD_GSC T WHERE T.GEOMETRY.SDO_POINT.X = %.5f AND T.GEOMETRY.SDO_POINT.Y = %.5f AND T.U_RECORD <> 2";
        List<Object> resultList = databaseOperator.exeSelect(getConn(), String.format(sql, coor.x, coor.y));
        if (CollectionUtils.isNotEmpty(resultList)) {
            setCheckResult("不允许去除有立交关系的形状点、node", String.format("[%s,%d]", row.tableName().toUpperCase(),
                    row.parentPKValue()), 0);
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
