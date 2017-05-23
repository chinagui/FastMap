package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.model.utils.CheckGeometryUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * @Title: CheckNotCreateTooNarrowFace
 * @Package: com.navinfo.dataservice.engine.check.rules
 * @Description: 如果创建的面跨图幅，并与图框线重叠的部分长度小于一个精度格，则不允许创建面
 * @Author: Crayeres
 * @Date: 05/09/17
 * @Version: V1.1
 */
public class CheckNotCreateTooNarrowFace extends baseRule {

    private final static List<ObjType> OBJ_TYPES =
            Arrays.asList(ObjType.LCLINK, ObjType.LULINK, ObjType.CMGBUILDLINK, ObjType.ADLINK, ObjType.ZONELINK);

    /**
     * 日志记录
     */
    private Logger logger = Logger.getLogger(CheckNotCreateTooNarrowFace.class);

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (CheckGeometryUtils.notContains(OBJ_TYPES, row.objType()) || row.status() != ObjStatus.INSERT) {
                continue;
            }

            Geometry geometry = CheckGeometryUtils.getGeometry(row);

            if (null == geometry) {
                logger.error(String.format("ObjType: %s, Pid: %d, [对象几何获取过程出错]", row.tableName(), row.parentPKValue()));
            } else {
                geometry = GeoTranslator.transform(geometry, 0.00001, 5);

                if (MeshUtils.isMeshLine(geometry) && 1d >= GeometryUtils.getLinkLength(geometry)) {
                    setCheckResult("如果创建的面跨图幅，并与图框线重叠的部分长度小于一个精度格，则不允许创建面",
                            String.format("[%s,%d]", row.tableName().toUpperCase(), row.parentPKValue()), 0);
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
    }
}
