package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.List;

/**
 * Created by Crayeres on 2017/2/10.
 */
public class GLM02249 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLinkForm && row.status() != ObjStatus.DELETE) {
                RdLinkForm form = (RdLinkForm) row;

                int formOfWay = form.getFormOfWay();
                if (form.changedFields().containsKey("formOfWay"))
                    formOfWay = (int) form.changedFields().get("formOfWay");

                if (formOfWay == 52) {
                    List<IRow> names = new AbstractSelector(RdLinkName.class, getConn()).loadRowsByParentId(form
                            .getLinkPid(), false);
                    for (IRow n : names) {
                        RdLinkName name = (RdLinkName) n;
                        if (name.getNameType() == 15) {
                            setCheckResult("", "", 0);
                            break;
                        }
                    }
                }
            } else if (row instanceof RdLinkName && row.status() != ObjStatus.DELETE) {
                RdLinkName name = (RdLinkName) row;

                int nameType = name.getNameType();
                if (name.changedFields().containsKey("nameType"))
                    nameType = (int) name.changedFields().get("nameType");

                if (nameType == 15) {
                    RdLink link = (RdLink) new RdLinkSelector(getConn()).loadById(name.getLinkPid(), false);
                    boolean flag = false;
                    List<IRow> forms = link.getForms();
                    for (IRow f : forms) {
                        RdLinkForm ff = (RdLinkForm) f;
                        if (ff.getFormOfWay() == 52) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        setCheckResult(link.getGeometry().toString(), "[RD_LINK, " + link.pid() + "]", link.mesh());
                    }
                }
            }
        }
    }
}
