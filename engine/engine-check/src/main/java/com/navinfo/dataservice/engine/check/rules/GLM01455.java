package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * 同一路口挂接的所有道路（交叉口内LINK除外）都有POI连接路形态，路口中任意一条交叉口内LINK没有POI连接路形态时，程序报LOG
* @ClassName: GLM01455 
* @author Zhang Xiaolong
* @date 2017年2月24日 下午4:12:03 
* @Description: TODO
 */
public class GLM01455 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLinkForm) {
                RdLinkForm form = (RdLinkForm) row;

                RdLinkSelector linkSelector = new RdLinkSelector(getConn());
                RdLink link = (RdLink) linkSelector.loadByIdOnlyRdLink(form.getLinkPid(), false);

                int sNodePid = link.getsNodePid();
                checkCrossNode(linkSelector, link, sNodePid);

                int eNodePid = link.geteNodePid();
                checkCrossNode(linkSelector, link, eNodePid);
            }
        }
    }

    private void checkCrossNode(RdLinkSelector linkSelector, RdLink link, int sNodePid) throws Exception {
        RdCrossSelector crossSelector = new RdCrossSelector(getConn());
        RdCross cross = null;
        try {
            cross = crossSelector.loadCrossByNodePid(sNodePid, false);
        } catch (Exception e) {
            return;
        }
        List<Integer> nodes = new ArrayList<>();
        for (IRow n : cross.getNodes()) {
            nodes.add(((RdCrossNode) n).getNodePid());
        }
        // log.info("GLM01454:[nodes.size()=" + nodes.size() + ",nodes=" + Arrays.toString(nodes.toArray()) + "]");

        List<Integer> innerLinks = new ArrayList<>();
        for (IRow l : cross.getLinks()) {
            innerLinks.add(((RdCrossLink) l).getLinkPid());
        }
        // log.info("GLM01454:[innerLinks.size()=" + innerLinks.size() + ",innerLinks=" + Arrays.toString(innerLinks
        //        .toArray()) + "]");

        List<RdLink> links = linkSelector.loadByNodePids(nodes, false);
        // log.info("GLM01454:[links.size()=" + links.size() + "]");

        boolean flag = false;
        for (RdLink l : links) {
            // log.info("GLM01454:[linkPid=" + l.pid() + "]");
            if (!innerLinks.contains(l.pid())) {
                List<IRow> forms = new AbstractSelector(RdLinkForm.class, getConn()).loadRowsByParentId(l.pid(), false);
                l.setForms(forms);
                boolean formFlag = false;
                for (IRow f : forms) {
                    RdLinkForm ff = (RdLinkForm) f;
                    if (ff.getFormOfWay() == 36) {
                        formFlag = true;
                        break;
                    }
                }
                if (!formFlag) {
                    flag = true;
                    break;
                }
            }
        }
        if (!flag) {
            // log.info("GLM01454:[非交叉点内link均为区域内道路]");
            for (RdLink l : links) {
                if (innerLinks.contains(l.pid())) {
                    List<IRow> forms = new AbstractSelector(RdLinkForm.class, getConn()).loadRowsByParentId(l.pid(),
                            false);
                    flag = true;
                    // log.info("GLM01454:[forms.size()=" + forms.size() + "]");
                    for (IRow f : forms) {
                        RdLinkForm ff = (RdLinkForm) f;
                        // log.info("GLM01454:[formOfWay=" + ff.getFormOfWay() + "]");
                        if (ff.getFormOfWay() == 36) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                        break;
                    }
                }
            }
        }
    }
}
