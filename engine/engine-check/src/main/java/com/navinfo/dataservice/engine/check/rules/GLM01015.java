package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//GLM01015	两条或者多条不同的Link具有相同的两个端点	闭合环未打断	
//RD_LINK、RW_LINK、AD_LINK、ZONE_LINK、LC_LINK、LU_LINK、ADAS_LINK	新增link

public class GLM01015 extends baseRule {

    private static Logger logger = Logger.getLogger(GLM01015.class);

    public GLM01015() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        List<Integer> linkPids = new ArrayList<>();
        for (IRow obj : checkCommand.getGlmList()) {
            if (obj instanceof RdLink) {
                RdLink rdLink = (RdLink) obj;
                if (linkPids.contains(rdLink.pid()))
                    continue;
                logger.debug("linkPids: " + Arrays.toString(linkPids.toArray()));
                logger.debug("检查类型：postCheck， 检查规则：GLM01015， 检查要素：RDLINK(" + rdLink.pid() + "), 触法时机：新增、修改、删除");
                linkPids.add(rdLink.pid());
                StringBuilder sb = new StringBuilder();
                sb.append("select a.link_pid from rd_link a where A.U_RECORD != 2 AND a.link_pid = ");
                sb.append(rdLink.getPid());
                sb.append(" and  exists (select 1 from rd_link b "
                        + "where a.link_pid != b.link_pid AND B.U_RECORD != 2"
                        + "and a.s_node_pid in (b.s_node_pid,b.e_node_pid) "
                        + "and a.e_node_pid in (b.s_node_pid,b.e_node_pid))");
                String sql = sb.toString();
                logger.debug("执行SQL: " + sql);
                DatabaseOperator getObj = new DatabaseOperator();
                List<Object> resultList = getObj.exeSelect(this.getConn(), sql);
                logger.debug("执行结果: " + resultList.size());
                if (resultList.size() > 0) {
                    this.setCheckResult(rdLink.getGeometry(), "[RD_LINK," + rdLink.getPid() + "]", rdLink.getMeshId());
                }
            }
        }
    }
}

