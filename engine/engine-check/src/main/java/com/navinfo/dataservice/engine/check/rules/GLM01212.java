package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.graph.HashSetRdLinkAndPid;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/*
 * GLM01212	Link信息	大陆环岛检查	形态	一组含“环岛”属性的link组成的link链上的所有link，速度限制值必须相同。	环岛的速度限制值不同
 */
public class GLM01212 extends baseRule {

    public GLM01212() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        List<Integer> linkPidList = new ArrayList<Integer>();
        for (IRow obj : checkCommand.getGlmList()) {
            if (obj instanceof RdLink) {
                RdLink rdLink = (RdLink) obj;
                //一条环岛link链上的link不重复检查
                if (linkPidList.contains(rdLink.getPid())) {
                    continue;
                }
                //非环岛link不查此规则
                List<IRow> forms = rdLink.getForms();
                if (forms.size() == 0) {
                    linkPidList.add(rdLink.getPid());
                    continue;
                }
                boolean isHuandao = false;
                for (int i = 0; i < forms.size(); i++) {
                    RdLinkForm form = (RdLinkForm) forms.get(i);
                    if (form.getFormOfWay() == 33) {
                        isHuandao = true;
                    }
                }
                if (!isHuandao) {
                    linkPidList.add(rdLink.getPid());
                    continue;
                }

                checkWithRdLink(rdLink, linkPidList);
            } else if (obj instanceof RdLinkForm) {
                RdLinkForm rdLinkForm = (RdLinkForm) obj;
                int linkPid = rdLinkForm.getLinkPid();
                //一条环岛link链上的link不重复检查
                if (linkPidList.contains(linkPid)) {
                    continue;
                }

                //rdlinkform有新增或者修改环岛记录的才进行检查，其他情况的即使原来有环岛link也不需要触发检查
                if (rdLinkForm.getFormOfWay() != 33) {
                    continue;
                }

                // log.info("GLM01212:[formOfWay=" + rdLinkForm.getFormOfWay() + "]");
                RdLinkSelector rdSelector = new RdLinkSelector(getConn());
                RdLink rdLink = (RdLink) rdSelector.loadByIdOnlyRdLink(linkPid, false);
                /*
                //非环岛link不查此规则
				List<IRow> forms=rdLink.getForms();
				if(forms.size()==0){linkPidList.add(linkPid);continue;}
				boolean isHuandao=false;
				for(int i=0;i<forms.size();i++){
					RdLinkForm form=(RdLinkForm) forms.get(i);
					if(form.getFormOfWay()==33){isHuandao=true;}
				}
				if(!isHuandao){linkPidList.add(linkPid);continue;}*/

                checkWithRdLink(rdLink, linkPidList);
            } else if (obj instanceof RdLinkSpeedlimit) {
                RdLinkSpeedlimit rdLinkSpeedlimit = (RdLinkSpeedlimit) obj;
                int linkPid = rdLinkSpeedlimit.getLinkPid();

                //一条环岛link链上的link不重复检查
                if (linkPidList.contains(linkPid)) {
                    continue;
                }

                Map<String, Object> changedFields = rdLinkSpeedlimit.changedFields();
                if (changedFields != null && !changedFields.containsKey("fromSpeedLimit") && !changedFields
                        .containsKey("toSpeedLimit")) {
                    continue;
                }

                RdLinkSelector rdSelector = new RdLinkSelector(getConn());
                RdLink rdLink = (RdLink) rdSelector.loadByIdOnlyRdLink(linkPid, false);

                ISelector rdFormSelector = new AbstractSelector(RdLinkForm.class, getConn());
                //非环岛link不查此规则
                List<IRow> forms = rdFormSelector.loadRowsByParentId(linkPid, false);
                rdLink.setForms(forms);

                if (forms.size() == 0) {
                    linkPidList.add(linkPid);
                    continue;
                }
                boolean isHuandao = false;
                for (int i = 0; i < forms.size(); i++) {
                    RdLinkForm form = (RdLinkForm) forms.get(i);
                    if (form.getFormOfWay() == 33) {
                        isHuandao = true;
                    }
                }
                if (!isHuandao) {
                    linkPidList.add(linkPid);
                    continue;
                }

                checkWithRdLink(rdLink, linkPidList);
            }
        }
    }

    private void checkWithRdLink(RdLink rdLink, List<Integer> linkPidList) throws Exception {
        //获取rdLink对应的链
        HashSetRdLinkAndPid huandaoChain = getLoader().loadHandaoChain(getConn(), rdLink);

        linkPidList.removeAll(huandaoChain.getRdLinkPidSet());
        linkPidList.addAll(huandaoChain.getRdLinkPidSet());

        Set<Integer> chainPidSet = huandaoChain.getRdLinkPidSet();
        String pidStr = chainPidSet.toString().replace("[", "").replace("]", "");
        // log.info("GLM01212:[pidStr=" + pidStr + "]");
        String sql = "SELECT COUNT(COUNT(0)) FROM RD_LINK_SPEEDLIMIT L" + " WHERE L.LINK_PID IN (" + pidStr + ")" + "" +
                "   AND L.U_RECORD != 2 GROUP" + " BY " + "L" + ".SPEED_TYPE, L.FROM_SPEED_LIMIT, L.TO_SPEED_LIMIT";
        // log.info("GLM01212:[sql=" + sql + "]");
        DatabaseOperator getObj = new DatabaseOperator();
        List<Object> resultList = getObj.exeSelect(getConn(), sql);
        if (resultList.size() > 0 && Integer.valueOf((String) resultList.get(0)) > 1) {
            String target = chainPidSet.toString().replace(" ", "").
                    replace("[", "[RD_LINK%").replace(",", "];[RD_LINK,").replace("%", ",");
            this.setCheckResult(rdLink.getGeometry(), target, rdLink.getMeshId());
        }
    }


}
