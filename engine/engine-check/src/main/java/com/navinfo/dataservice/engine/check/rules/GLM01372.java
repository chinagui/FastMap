package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * IMI属性不能和特殊交通类型共存，否则报LOG
* @ClassName: GLM01372 
* @author Zhang Xiaolong
* @date 2017年2月13日 下午4:51:56 
* @Description: IMI属性不能和特殊交通类型共存，否则报LOG
 */
public class GLM01372 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
                RdLink link = (RdLink) row;

                int specialTraffic = link.getSpecialTraffic();
                if (link.changedFields().containsKey("specialTraffic"))
                	specialTraffic = (int) link.changedFields().get("specialTraffic");

                int imiCode = link.getImiCode();
                if (link.changedFields().containsKey("imiCode"))
                	imiCode = (int) link.changedFields().get("imiCode");

                if (specialTraffic == 1 && imiCode != 0) {
                    setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                }
            }
        }
    }
}
