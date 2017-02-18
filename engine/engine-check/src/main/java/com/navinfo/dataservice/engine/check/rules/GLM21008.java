package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLinkPart;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.List;

/**
 * Created by Crayeres on 2017/2/9.
 */
public class GLM21008 extends baseRule {
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
                    formOfWay = Integer.valueOf(form.changedFields().get("formOfWay").toString());

                if (formOfWay == 12 || formOfWay == 13) {
                    RdSameLinkPart part = new RdSameLinkSelector(getConn()).loadLinkPartByLink(form.getLinkPid(),
                            "RD_LINK", false);
                    if (null != part) {
                        setCheckResult("", "[RD_LINK," + form.getLinkPid() + "]", 0);
                    }
                }
            } else if (row instanceof RdSameLink && row.status() != ObjStatus.DELETE) {
                RdSameLink sameLink = (RdSameLink) row;

                for (IRow r : sameLink.getParts()) {
                    RdSameLinkPart part = (RdSameLinkPart) r;

                    String tableName = part.getTableName();
                    if ("".equals(tableName) || !"RD_LINK".equals(tableName))
                        continue;

                    int linkPid = part.getLinkPid();
                    if (part.changedFields().containsKey("linkPid"))
                        linkPid = Integer.valueOf(part.changedFields().get("linkPid").toString());

                    RdLink link = (RdLink) new RdLinkSelector(getConn()).loadById(linkPid, false);
                    List<IRow> forms = link.getForms();
                    boolean flag = false;
                    for (IRow f : forms) {
                        RdLinkForm ff = (RdLinkForm) f;
                        // log.info("GLM21008:[FORM_OF_WAY=]" + ff.getFormOfWay());
                        if (ff.getFormOfWay() == 12 || ff.getFormOfWay() == 13) {
                            flag = true;
                        }
                    }
                    if (flag) {
                        setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                    }
                }
            }
        }
    }
}
