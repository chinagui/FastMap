package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeedVia;
import com.navinfo.dataservice.dao.glm.selector.rd.variablespeed.RdVariableSpeedSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GLM33006 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdVariableSpeed) {
                RdVariableSpeed speed = (RdVariableSpeed) row;
                List<Integer> pids = new ArrayList<>();
                pids.add(speed.getInLinkPid());
                int outLinkPid = speed.getOutLinkPid();
                if (speed.changedFields().containsKey("outLinkPid"))
                    outLinkPid = Integer.valueOf(speed.changedFields().get("outLinkPid").toString());
                pids.add(outLinkPid);
                for (IRow via : speed.getVias()) {
                    RdVariableSpeedVia speedVia = (RdVariableSpeedVia) via;
                    pids.add(speedVia.getLinkPid());
                }

                String sql = "SELECT 1 FROM RD_LINK RL, RD_LINK_FORM RLF, RD_LINK_LIMIT RLL WHERE RL.LINK_PID IN (" +
                        StringUtils.getInteStr(pids) + ") AND RL.LINK_PID = RLF.LINK_PID AND RL.LINK_PID = RLL" + ""
                        + ".LINK_PID AND RL.U_RECORD <> 2 AND RLF.U_RECORD <> 2 AND RLL.U_RECORD <> 2 AND (RL.KIND "
                        + "IN" + " " + "(0, 8, 9, 10, 11, 13) OR RLF.FORM_OF_WAY IN (18, 20, 33, 50) OR RL.IMI_CODE "
                        + "IN " + "(1, 2) " + "OR " + "(RLL.TYPE = 2 AND RLL.TIME_DOMAIN IS NULL) OR RLL.TYPE = 3)";
                DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
                List<Object> resultList = getObj.exeSelect(getConn(), sql);
                if (!resultList.isEmpty())
                    setCheckResult("", "", 0);
            } else if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
                RdLink link = (RdLink) row;

                int kind = link.getKind();
                if (link.changedFields().containsKey("kind"))
                    kind = Integer.valueOf(link.changedFields().get("kind").toString());

                if (Arrays.asList(new Integer[]{0, 8, 9, 10, 11, 13}).contains(kind)) {
                    List<RdVariableSpeed> list1 = new RdVariableSpeedSelector(getConn()).loadRdVariableSpeedByLinkPid
                            (link.pid(), false);
                    List<RdVariableSpeed> list2 = new RdVariableSpeedSelector(getConn())
                            .loadRdVariableSpeedByViaLinkPid(link.pid(), false);

                    if (!list1.isEmpty() || list2.isEmpty()) {
                        setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                    }
                }

                int imiCode = link.getImiCode();
                if (link.changedFields().containsKey("imiCode"))
                    imiCode = Integer.valueOf(link.changedFields().get("imiCode").toString());

                if (imiCode == 1 || imiCode == 2) {
                    setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                }
            } else if (row instanceof RdLinkForm && row.status() != ObjStatus.DELETE) {
                RdLinkForm form = (RdLinkForm) row;

                int formOfWay = form.getFormOfWay();
                if (form.changedFields().containsKey("formOfWay"))
                    formOfWay = Integer.valueOf(form.changedFields().get("formOfWay").toString());

                if (Arrays.asList(new Integer[]{18, 20, 33, 50}).contains(formOfWay)) {
                    List<RdVariableSpeed> list1 = new RdVariableSpeedSelector(getConn()).loadRdVariableSpeedByLinkPid
                            (form.getLinkPid(), false);
                    List<RdVariableSpeed> list2 = new RdVariableSpeedSelector(getConn())
                            .loadRdVariableSpeedByViaLinkPid(form.getLinkPid(), false);
                    if (!list1.isEmpty() || list2.isEmpty()) {
                        setCheckResult("", "[RD_LINK," + form.getLinkPid() + "]", 0);
                    }
                }
            } else if (row instanceof RdLinkLimit && row.status() != ObjStatus.DELETE) {
                RdLinkLimit linkLimit = (RdLinkLimit) row;

                int type = linkLimit.getType();
                if (linkLimit.changedFields().containsKey("type"))
                    type = Integer.valueOf(linkLimit.changedFields().get("type").toString());

                if (type == 3) {
                    setCheckResult("", "[RD_LINK," + linkLimit.getLinkPid() + "]", 0);
                } else if (type == 2) {
                    String timeDomain = linkLimit.getTimeDomain();
                    if (linkLimit.changedFields().containsKey("timeDomain"))
                        timeDomain = linkLimit.changedFields().get("timeDomain").toString();

                    if (StringUtils.isEmpty(timeDomain)) {
                        setCheckResult("", "[RD_LINK," + linkLimit.getLinkPid() + "]", 0);
                    }
                }
            }
        }
    }
}
