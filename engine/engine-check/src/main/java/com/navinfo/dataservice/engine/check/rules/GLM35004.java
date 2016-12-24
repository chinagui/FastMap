package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by chaixin on 2016/12/20 0020.
 */
public class GLM35004 extends baseRule {
    private Logger log = Logger.getLogger(GLM35004.class);

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (!(row instanceof RdLinkForm))
                return;
            RdLinkForm form = (RdLinkForm) row;
            int newKind = -1;
            if (form.changedFields().containsKey("formOfWay")) {
                newKind = (int) form.changedFields().get("formOfWay");
            }
            if ((newKind == -1 && (form.getFormOfWay() != 9 && form.getFormOfWay() != 10)) && newKind != 9 && newKind != 10)
                return;
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT RL.GEOMETRY,'[RD_LINK,' || RL.LINK_PID || ']'");
            sql.append(",RL.MESH_ID FROM RD_LINK RL, RD_HGWG_LIMIT RHL WHERE RL.LINK_PID = ");
            sql.append(form.getLinkPid());
            sql.append(" AND RL.LINK_PID = RHL.LINK_PID ");
            DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
            List<Object> resultList = getObj.exeSelect(this.getConn(), sql.toString());
            if (resultList.isEmpty())
                return;
            this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int) resultList.get(2));
        }
    }
}
