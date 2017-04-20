package com.navinfo.dataservice.engine.edit.operation.obj.cmg.node;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnodeMesh;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildfaceSelector;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @Title: CmglinkUtil
 * @Package: com.navinfo.dataservice.engine.edit.operation.obj.cmg.link
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/12
 * @Version: V1.0
 */
public final class CmgnodeUtil {
    private CmgnodeUtil() {
    }

    /**
     * 删除CMG-FACE时处理CMG-NODE图幅
     * @param cmgnodes 待处理CMG-NODE
     * @param cmgfaceMeshId 修形后CMG-FACE的图符号
     * @param result 结果集
     * @param conn 数据库链接
     */
    public static void handleCmgnodeMesh(List<CmgBuildnode> cmgnodes, int cmgfaceMeshId, Connection conn, Result result) {
        for (CmgBuildnode cmgnode : cmgnodes) {
            if (cmgnode.getMeshes().size() > 1) {
                for (IRow row : cmgnode.getMeshes()) {
                    if (cmgfaceMeshId == row.mesh()) {
                        result.insertObject(row, ObjStatus.DELETE, row.parentPKValue());
                        break;
                    }
                }
                continue;
            }

            try {
                // 当CMG-LINK关联面大于一并且图幅数为1，在删除面时不处理图幅
                List<CmgBuildface> cmgfaces = new CmgBuildfaceSelector(conn).listTheAssociatedFaceOfTheNode(cmgnode.pid(), false);
                if (cmgfaces.size() > 1) {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Coordinate coordinate = GeoTranslator.transform(
                    cmgnode.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION).getCoordinate();
            List<String> meshes = new ArrayList(Arrays.asList(MeshUtils.point2Meshes(coordinate.x, coordinate.y)));

            for (IRow row : cmgnode.getMeshes()) {
                if (meshes.contains(String.valueOf(row.mesh()))) {
                    meshes.remove(String.valueOf(row.mesh()));
                } else {
                    result.insertObject(row, ObjStatus.DELETE, cmgnode.pid());
                }
            }

            for (String meshId : meshes) {
                CmgBuildnodeMesh cmgnodeMesh = new CmgBuildnodeMesh();
                cmgnodeMesh.setNodePid(cmgnode.pid());
                cmgnodeMesh.setMeshId(Integer.valueOf(meshId));
                result.insertObject(cmgnodeMesh, ObjStatus.INSERT, cmgnode.pid());
            }
        }
    }

    /**
     * 四舍五入保留五位精度
     * @param itude 待修正经纬度
     * @return 截取后经纬度
     */
    public static double reviseItude(double itude) {
        return new BigDecimal(itude).setScale(Constant.BASE_PRECISION, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}