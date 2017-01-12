package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Rdlink	word	RDLINK001	后台
 * 两条RDLink不能首尾点一致
 *
 * @author zhangxiaoyi
 */

public class RdLink001 extends baseRule {

    private static Logger logger = Logger.getLogger(RdLink001.class);

    public RdLink001() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow obj : checkCommand.getGlmList()) {
            if (obj instanceof RdLink) {
                RdLink rdLink = (RdLink) obj;
                logger.debug("检查类型：preCheck， 检查规则：RdLink001， 检查要素：RDLINK(" + rdLink.pid() + "), 触法时机：新增、删除");
                String sql = "SELECT 1"
                        + " FROM RD_LINK L"
                        + " WHERE L.S_NODE_PID IN (" + rdLink.getsNodePid() + "," + rdLink.geteNodePid() + ") "
                        + " AND L.E_NODE_PID IN (" + rdLink.getsNodePid() + "," + rdLink.geteNodePid() + ")"
                        + " AND L.U_RECORD != 2 "
                        + " AND NOT EXISTS (SELECT 1 FROM RD_LINK WHERE LINK_PID = " + rdLink.getPid() + ")";
                logger.debug("执行SQL: " + sql);
                DatabaseOperator getObj = new DatabaseOperator();
                List<Object> resultList = getObj.exeSelect(this.getConn(), sql);
                logger.debug("执行结果: " + resultList.size());
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


