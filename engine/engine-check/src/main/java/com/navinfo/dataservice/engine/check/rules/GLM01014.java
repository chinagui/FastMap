package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import org.apache.log4j.Logger;

public class GLM01014 extends baseRule {
    private static Logger logger = Logger.getLogger(GLM01014.class);

    public void preCheck(CheckCommand checkCommand) {
    }

    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow obj : checkCommand.getGlmList()) {
            if (obj instanceof RdLink) {
                RdLink rdLink = (RdLink) obj;
                // logger.debug("检查类型：postCheck， 检查规则：GLM01014， 检查要素：RDLINK(" + rdLink.pid() + "), 触法时机：新增、修改、删除");
                int sNodePid = rdLink.getsNodePid();
                if (rdLink.changedFields().containsKey("sNodePid"))
                    sNodePid = Integer.valueOf(rdLink.changedFields().get("sNodePid").toString());

                int eNodePid = rdLink.geteNodePid();
                if (rdLink.changedFields().containsKey("eNodePid"))
                    eNodePid = Integer.valueOf(rdLink.changedFields().get("eNodePid").toString());

                // logger.debug("检查参数: S_NODE_PID(" + sNodePid + "), E_NODE_PID(" + eNodePid + ")");
                if (sNodePid == 0 || eNodePid == 0)
                    continue;
                if (sNodePid == eNodePid) {
                    this.setCheckResult(rdLink.getGeometry(), "[RD_LINK," + rdLink.getPid() + "]", rdLink.getMeshId());
                }
            }
        }
    }
}