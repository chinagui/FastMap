package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchRealimage;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Crayeres on 2017/2/10.
 */
public class GLM12051_2 extends baseRule {
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

                if (formOfWay == 20) {
                    RdBranchSelector selector = new RdBranchSelector(getConn());
                    List<RdBranch> branches = new ArrayList<>();
                    List<RdBranch> list1 = selector.loadByLinkPid(form.getLinkPid(), 1, false);
                    List<RdBranch> list2 = selector.loadByLinkPid(form.getLinkPid(), 2, false);
                    branches.addAll(list1);
                    branches.addAll(list2);
                    for (RdBranch branch : branches) {
                        List<IRow> images = new AbstractSelector(RdBranchRealimage.class, getConn())
                                .loadRowsByParentId(branch.pid(), false);
                        if (!images.isEmpty()) {
                            setCheckResult("", "[RD_LINK," + form.getLinkPid() + "]", 0);
                            break;
                        }
                    }
                }
            } else if (row instanceof RdBranch && row.status() != ObjStatus.DELETE) {
                RdBranch branch = (RdBranch) row;

                int inLinkPid = branch.getInLinkPid();
                if (branch.changedFields().containsKey("inLinkPid")) {
                    inLinkPid = Integer.valueOf(branch.changedFields().get("inLinkPid").toString());
                }

                int outLinkPid = branch.getOutLinkPid();
                if (branch.changedFields().containsKey("outLinkPid")) {
                    outLinkPid = Integer.valueOf(branch.changedFields().get("outLinkPid").toString());
                }

                RdLinkSelector selector = new RdLinkSelector(getConn());
                RdLink link = (RdLink) selector.loadById(inLinkPid, false);
                boolean flag = false;
                List<IRow> forms = link.getForms();
                for (IRow f : forms) {
                    RdLinkForm ff = (RdLinkForm) f;
                    if (ff.getFormOfWay() == 20) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                    continue;
                }

                link = (RdLink) selector.loadById(outLinkPid, false);
                flag = false;
                forms = link.getForms();
                for (IRow f : forms) {
                    RdLinkForm ff = (RdLinkForm) f;
                    if (ff.getFormOfWay() == 20) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                    continue;
                }
            }
        }
    }
}
