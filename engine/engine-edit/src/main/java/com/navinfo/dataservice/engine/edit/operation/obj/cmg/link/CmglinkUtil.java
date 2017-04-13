package com.navinfo.dataservice.engine.edit.operation.obj.cmg.link;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlinkMesh;
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
public final class CmglinkUtil {
    private CmglinkUtil() {
    }

    /**
     * 删除CMG-FACE时处理CMG-LINK图幅
     * @param cmglinks 待处理CMG-LINK
     * @param result 结果集
     */
    public static void handleCmglinkMesh(List<CmgBuildlink> cmglinks, int cmgfaceMeshId, Result result) {
        for (CmgBuildlink cmglink : cmglinks) {
            if (cmglink.getMeshes().size() > 1) {
                for (IRow row : cmglink.getMeshes()) {
                    if (cmgfaceMeshId == row.mesh()) {
                        result.insertObject(row, ObjStatus.DELETE, row.parentPKValue());
                        break;
                    }
                }
                continue;
            }

            Coordinate[] coordinates = GeoTranslator.transform(
                    cmglink.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION).getCoordinates();
            List<String> meshes = Arrays.asList(MeshUtils.line2Meshes(
                    coordinates[0].x, coordinates[0].y, coordinates[coordinates.length].x, coordinates[coordinates.length].y));

            for (IRow row : cmglink.getMeshes()) {
                if (meshes.contains(String.valueOf(row.mesh()))) {
                    meshes.remove(String.valueOf(row.mesh()));
                } else {
                    result.insertObject(row, ObjStatus.DELETE, cmglink.pid());
                }
            }

            for (String meshId : meshes) {
                CmgBuildlinkMesh cmglinkMesh = new CmgBuildlinkMesh();
                cmglinkMesh.setLinkPid(cmglink.pid());
                cmglinkMesh.setMeshId(Integer.valueOf(meshId));
                result.insertObject(cmglinkMesh, ObjStatus.INSERT, cmglink.pid());
            }
        }
    }
}
