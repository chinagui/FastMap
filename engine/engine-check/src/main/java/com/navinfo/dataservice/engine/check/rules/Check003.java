package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.model.utils.CheckGeometryUtils;
import com.navinfo.navicommons.geo.computation.GeometryTypeName;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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

    private final static List<ObjType> NODE_TYPES = Arrays.asList(ObjType.LCNODE, ObjType.LUNODE, ObjType.CMGBUILDNODE, ObjType.RWNODE);

    private final static List<ObjType> LINK_TYPES = Arrays.asList(ObjType.LCLINK, ObjType.LULINK, ObjType.CMGBUILDLINK, ObjType.RWLINK);

    /**
     * 日志记录
     */
    private Logger logger = Logger.getLogger(Check003.class);

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        if (checkCommand.getOperType().equals(OperType.MOVE) || checkCommand.getOperType().equals(OperType.REPAIR)) {
            DatabaseOperator databaseOperator = new DatabaseOperator();
            for (IRow iRow : checkCommand.getGlmList()) {
                if (NODE_TYPES.contains(iRow.objType()) && hasModifyGeo(iRow)) {
                    // 检查点位是否包含立交
                    Coordinate coordinate = GeoTranslator.transform
                            (CheckGeometryUtils.getGeometry(iRow), GeoTranslator.dPrecisionMap, 5).getCoordinate();
                    checkGscGeometry(iRow, databaseOperator, coordinate);
                }
                if (LINK_TYPES.contains(iRow.objType()) && hasModifyGeo(iRow)) {
                    Coordinate[] oldCoors = GeoTranslator.transform
                            (CheckGeometryUtils.getGeometry(iRow), GeoTranslator.dPrecisionMap, 5).getCoordinates();
                    ArrayList<Coordinate> oldCoordinates = new ArrayList(Arrays.asList(oldCoors));
                    Iterator<Coordinate> iterator = oldCoordinates.iterator();

                    Coordinate[] newCoors = GeoTranslator.geojson2Jts((JSONObject) iRow.changedFields().get("geometry"), 1, 5)
                            .getCoordinates();
                    while (iterator.hasNext()) {
                        Coordinate next = iterator.next();
                        for (Coordinate coor : newCoors) {
                            if (coor.x == next.x && coor.y == next.y) {
                                iterator.remove();
                                break;
                            }
                        }
                    }
                    checkGscGeometry(iRow, databaseOperator, oldCoordinates.toArray(new Coordinate[]{}));
                }
            }
        }
    }

    /**
     * 检查该点位是否为立交点
     * @param coors 点位
     * @param databaseOperator 数据库操作
     * @throws Exception 查询失败
     */
    private void checkGscGeometry(IRow row, DatabaseOperator databaseOperator, Coordinate... coors) throws Exception {
        if (null == coors || coors.length == 0) {
            return;
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM RD_GSC T WHERE T.U_RECORD <> 2 AND (");
        for (int index = 0; index < coors.length; index++) {
            if (0 != index) {
                sql.append(" OR ");
            }

            Coordinate coordinate = coors[index];
            sql.append("(");
            sql.append("T.GEOMETRY.SDO_POINT.X = ").append(coordinate.x);
            sql.append(" AND ");
            sql.append("T.GEOMETRY.SDO_POINT.Y = ").append(coordinate.y);
            sql.append(")");
        }
        sql.append(")");
        List<Object> resultList = databaseOperator.exeSelect(getConn(), sql.toString());
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
