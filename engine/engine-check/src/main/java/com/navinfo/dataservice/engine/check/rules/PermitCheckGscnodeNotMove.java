package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

import java.util.List;

/**
 * Rdlink	html	PERMIT_CHECK_GSCNODE_NOT_MOVE
 * 后台	创建或修改link，节点不能到已有的立交点处，请先删除立交关系
 */

public class PermitCheckGscnodeNotMove extends baseRule {

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow obj : checkCommand.getGlmList()) {
            if (obj instanceof RdLink) {
                RdLink link = (RdLink) obj;
                Geometry geometry = GeoTranslator.transform(link.getGeometry(), 0.00001, 5);
                if (!link.changedFields().containsKey("geometry") && link.changedFields().size() > 0) {
                    continue;
                } else if (link.changedFields().containsKey("geometry")) {
                    geometry = GeoTranslator.geojson2Jts((JSONObject) link.changedFields().get("geometry"), 0.00001, 5);
                }
                StringBuffer sb = new StringBuffer();
                sb.append("SELECT RG.LINK_PID FROM RD_GSC RG, RD_GSC_LINK RGL WHERE RG.U_RECORD <> 2 AND RG.PID = RGL" +
                        ".PID (");
                for (Coordinate coor : geometry.getCoordinates()) {
                    sb.append("(RG.GEOMETRY.SDO_POINT.X = ").append(coor.x + " ");
                    sb.append("AND RG.GEOMETRY.SDO_POINT.Y = ").append(coor.y + " ").append(") ").append("OR ");
                }
                String sql = sb.substring(0, sb.lastIndexOf("OR")) + ") AND RGL.LINK_PID != " + link.pid();
                DatabaseOperator getObj = new DatabaseOperator();
                List<Object> resultList = getObj.exeSelect(this.getConn(), sql);
                if (!resultList.isEmpty()) {
                    boolean hasGsc = true;
                    for (Object o : resultList) {
                        int gscLinkPid = Integer.valueOf(o.toString());
                        if (link.pid() == gscLinkPid) {
                            hasGsc = false;
                            break;
                        }
                    }
                    if (hasGsc)
                        this.setCheckResult(geometry, "[RD_LINK," + link.getPid() + "]", link.getMeshId());
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
    }
}


