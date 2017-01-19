package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaixin on 2017/1/19 0019.
 */
public class CheckFaceMustComposedByClosedlink extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow linkRow : checkCommand.getGlmList()) {
            if (linkRow instanceof ZoneLink) {
                ZoneLink link = (ZoneLink) linkRow;
                if (!link.status().equals(OperType.UPDATE)) continue;
                if (!link.changedFields().containsKey("sNodePid") || !link.changedFields().containsKey("eNodePid"))
                    continue;

                String sql = "SELECT 1 FROM ZONE_LINK ZL, ZONE_FACE_TOPO ZFT WHERE ZL.LINK_PID = ZFT.LINK_PID AND ZFT" +
                        ".U_RECORD <> 2 AND ZL.LINK_PID = " + link.pid();
                DatabaseOperator getObj = new DatabaseOperator();
                List<Object> resultList = getObj.exeSelect(this.getConn(), sql);
                if (resultList.size() > 0) {
                    this.setCheckResult("", "", 0);
                }
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
