package com.navinfo.dataservice.engine.edit.operation.obj.cmg.node;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnodeMesh;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;

import java.util.Arrays;
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
     * @param result 结果集
     */
    public static void handleCmgnodeMesh(List<CmgBuildnode> cmgnodes, Result result) {
        for (CmgBuildnode cmgnode : cmgnodes) {
            Coordinate coordinate = GeoTranslator.transform(
                    cmgnode.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION).getCoordinate();
            List<String> meshes = Arrays.asList(MeshUtils.point2Meshes(coordinate.x, coordinate.y));

            for (IRow row : cmgnode.getMeshes()) {
                if (meshes.contains(String.valueOf(row.mesh()))) {
                    meshes.remove(String.valueOf(row.mesh()));
                } else {
                    result.insertObject(row, ObjStatus.DELETE.DELETE, cmgnode.pid());
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
}
