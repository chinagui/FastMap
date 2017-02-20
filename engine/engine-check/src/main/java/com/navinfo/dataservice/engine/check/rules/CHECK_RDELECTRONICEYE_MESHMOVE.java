package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

/**
 * @author songdongyan
 * @ClassName: CHECK_RDELECTRONICEYE_MESHMOVE
 * @date 2016年8月25日
 * @Description: 不允许跨图幅移动
 */
public class CHECK_RDELECTRONICEYE_MESHMOVE extends baseRule {


    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow obj : checkCommand.getGlmList()) {
            //获取新建RdBranch信息
            if (obj instanceof RdElectroniceye) {
                RdElectroniceye rdElectroniceye = (RdElectroniceye) obj;
                Map<String, Object> changedFields = rdElectroniceye.changedFields();

                int meshId = rdElectroniceye.getMeshId();
                int changeMeshId = meshId;

                if (changedFields.containsKey("geometry")) {
                    Geometry geometry = GeoTranslator.geojson2Jts((JSONObject) changedFields.get("geometry"),
                            0.00001, 5);
                    String[] meshes = MeshUtils.point2Meshes(geometry.getCoordinate().x, geometry.getCoordinate().y);
                    if (meshes.length == 1) {
                        changeMeshId = Integer.valueOf(meshes[0]);
                    } else {
                        setCheckResult("", "", 0);
                    }
                } else {
                    continue;
                }

                if (meshId != changeMeshId) {
                    this.setCheckResult("", "", 0);
                    return;
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
    }

}
