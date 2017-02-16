package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

import java.util.List;

/**
 * Created by chaixin on 2017/1/13 0013.
 */
public class GLM50048 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof ZoneFace) {
                ZoneFace face = (ZoneFace) row;
                String sql = "SELECT COUNT(1) NUM FROM ZONE_FACE ZF, ZONE_FACE_TOPO ZFT WHERE ZF.FACE_PID = ZFT" + ""
                        + ".FACE_PID AND ZF.FACE_PID = " + face.pid() + " AND ZF.U_RECORD <> 2 AND ZFT.U_RECORD <> 2 " +
                        "" + "" + "GROUP BY ZF.FACE_PID";
                DatabaseOperator getObj = new DatabaseOperator();
                List<Object> resultList = getObj.exeSelect(this.getConn(), sql);
                if (!resultList.isEmpty()) {
                    int num = Integer.valueOf(resultList.get(0).toString());
                    if (num < 2)
                        setCheckResult(face.getGeometry(), "[ZONE_FACE," + face.pid() + "]", face.mesh());
                }
            }
        }
    }
}
