package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by chaixin on 2016/12/20 0020.
 */
public class GLM35003 extends baseRule {
    private Logger log = Logger.getLogger(GLM35003.class);

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink) {
                RdLink link = (RdLink) row;
                int newDirect = -1;
                if (link.changedFields().containsKey("direct"))
                    newDirect = (int) link.changedFields().get("direct");
                if ((newDirect == -1 && link.getDirect() == 1) || newDirect == 1)
                    return;
                StringBuffer sql = new StringBuffer();
                sql.append("SELECT RL.GEOMETRY, '[RD_LINK,' || RL.LINK_PID || ']', RL.MESH_ID");
                sql.append(" FROM RD_LINK RL, RD_HGWG_LIMIT RHL WHERE RL.LINK_PID = RHL.LINK_PID");
                sql.append(" AND RL.LINK_PID = ").append(link.pid());
                if ((newDirect == -1 && link.getDirect() == 2) || newDirect == 2)
                    sql.append(" AND RHL.DIRECT = 3");
                if ((newDirect == -1 && link.getDirect() == 3) || newDirect == 3)
                    sql.append(" AND RHL.DIRECT = 2");
                DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
                List<Object> resultList = getObj.exeSelect(this.getConn(), sql.toString());
                if (resultList.isEmpty())
                    return;
                setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int) resultList.get(2));
            }
        }
    }
}
