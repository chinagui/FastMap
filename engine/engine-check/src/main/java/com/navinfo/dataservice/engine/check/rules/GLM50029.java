package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * @Title: GLM50029
 * @Package: com.navinfo.dataservice.engine.check.rules
 * @Description: AOIZone类型的Face，相互不能存在重叠区域；KDZone类型的Face，相互不能存在重叠区域；
 * @Author: Crayeres
 * @Date: 5/22/2017
 * @Version: V1.0
 */
public class GLM50029 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof AdFace && row.status() != ObjStatus.DELETE) {
                AdFace face = (AdFace) row;
                int regionId = face.getRegionId();
                if (face.changedFields().containsKey("regionId")) {
                    regionId = Integer.parseInt(face.changedFields().get("regionId").toString());
                }

                if (0 != regionId) {
                    AdAdmin adAdmin = (AdAdmin) new AdAdminSelector(getConn()).loadById(regionId, false);
                }
            } else if (row instanceof ZoneFace && row.status() != ObjStatus.DELETE) {
                ZoneFace face = (ZoneFace) row;
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
