package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * Rdlink	html	PERMIT_CHECK_NOT_MOVE_LINK_SNAP_NODE	后台
 * 该link的端点已经作为了同一点，不能再用该端点进行合并点操作
 *
 * @author zhangxiaoyi
 */

public class PermitCheckNotMoveLinkSnapNode extends baseRule {

    public PermitCheckNotMoveLinkSnapNode() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow obj : checkCommand.getGlmList()) {
            if (obj instanceof RdLink) {
                RdLink rdLink = (RdLink) obj;
                Map<String, Object> changedFields = rdLink.changedFields();
                if (changedFields == null) {
                    continue;
                }
                String nodeIds = "";
                if (changedFields.containsKey("sNodePid")) {
                    nodeIds = nodeIds + changedFields.get("sNodePid") + "," + rdLink.getsNodePid();
                }
                if (changedFields.containsKey("eNodePid")) {
                    nodeIds = nodeIds + changedFields.get("eNodePid") + "," + rdLink.geteNodePid();
                }
                if (nodeIds.isEmpty()) {
                    continue;
                }
                String sql = "SELECT 1"
                        + "  FROM RD_SAMENODE_PART S"
                        + " WHERE S.TABLE_NAME = 'RD_NODE' AND S.U_RECORD != 2 "
                        + "   AND S.NODE_PID IN (" + nodeIds + ")";
                DatabaseOperator getObj = new DatabaseOperator();
                List<Object> resultList = getObj.exeSelect(this.getConn(), sql);
                if (resultList != null && resultList.size() > 0) {
                    this.setCheckResult(rdLink.getGeometry(), "[RD_LINK," + rdLink.getPid() + "]", rdLink.getMeshId());
                    break;
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
    }
}


