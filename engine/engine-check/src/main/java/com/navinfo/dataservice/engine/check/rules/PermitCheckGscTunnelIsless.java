/**
 *
 */
package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Zhang Xiaolong
 * @ClassName: PermitCheckGscTunnelIsless
 * @date 2016年12月19日 下午2:16:28
 * @Description: TODO
 */
public class PermitCheckGscTunnelIsless extends baseRule {

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        AbstractSelector selector = new AbstractSelector(this.getConn());

        OperType operType = checkCommand.getOperType();

        for (IRow obj : checkCommand.getGlmList()) {
            Set<Integer> linkPidSet = new HashSet<>();
            if (obj instanceof RdGsc) {
                RdGsc gsc = (RdGsc) obj;

                List<IRow> gscLinkList = gsc.getLinks();

                for (IRow linkRow : gscLinkList) {
                    RdGscLink gscLink = (RdGscLink) linkRow;

                    checkGscTunnelIsLess(gscLink, linkPidSet, selector, operType);
                }
            } else if (obj instanceof RdGscLink) {
                RdGscLink gscLink = (RdGscLink) obj;

                checkGscTunnelIsLess(gscLink, linkPidSet, selector, operType);
            }

            if (linkPidSet.size() > 0) {
                this.setCheckResult("", "", 0, "任何link与隧道属性的RDlink创建立交后，隧道属性的RDlink的高度层次最小(linkPid:"
                        + StringUtils.join(linkPidSet.toArray(), ",") + ")");
                return;
            }

        }
    }

    public void postCheck(CheckCommand checkCommand) throws Exception {
    }

    private void checkGscTunnelIsLess(RdGscLink gscLink, Set<Integer> linkPidSet, AbstractSelector selector, OperType
            operType)
            throws Exception {
        if (operType == OperType.CREATE) {
            if (gscLink.getTableName().equals("RD_LINK") && gscLink.getZlevel() != 0) {
                int linkPid = gscLink.getLinkPid();

                List<IRow> formRows = selector.loadRowsByClassParentId(RdLinkForm.class, linkPid, true, null, null);

                for (IRow row : formRows) {
                    RdLinkForm form = (RdLinkForm) row;
                    // 判断是否是隧道形态
                    if (form.getFormOfWay() == 31) {
                        linkPidSet.add(linkPid);
                    }
                }
            }
        } else if (operType == OperType.UPDATE) {
            if (gscLink.getTableName().equals("RD_LINK") && gscLink.getZlevel() == 0) {
                if (gscLink.changedFields().containsKey("zlevel")) {
                    int level = (int) gscLink.changedFields().get("zlevel");
                    if (level != 0) {
                        int linkPid = gscLink.getLinkPid();

                        List<IRow> formRows = selector.loadRowsByClassParentId(RdLinkForm.class, linkPid, true, null, null);

                        for (IRow row : formRows) {
                            RdLinkForm form = (RdLinkForm) row;
                            // 判断是否是隧道形态
                            if (form.getFormOfWay() == 31) {
                                linkPidSet.add(linkPid);
                            }
                        }
                    }
                }
            }
        }
    }
}
