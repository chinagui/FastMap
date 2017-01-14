package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.graph.HashSetRdLinkAndPid;

/*
 * GLM01204	Link信息	大陆环岛检查	形态	一组含“环岛”属性的link组成的link链上的所有link，道路种别必须相同。	环岛的种别不同
 */
public class GLM01204 extends baseRule {

    public GLM01204() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        // TODO_CHECK 检查未考虑修改后值问题
        List<Integer> linkPidList = new ArrayList<Integer>();
        for (IRow obj : checkCommand.getGlmList()) {
            if (obj instanceof RdLink) {
                RdLink rdLink = (RdLink) obj;
                //一条环岛link链上的link不重复检查
                if (linkPidList.contains(rdLink.getPid())) {
                    continue;
                }

                Map<String, Object> changedFields = rdLink.changedFields();
                if (!changedFields.containsKey("kind")) {
                    continue;
                }

                //非环岛link不查此规则
                List<IRow> forms = new AbstractSelector(RdLinkForm.class, getConn()).loadRowsByParentId(rdLink.pid(),
                        false);
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

                int formOfWay = rdLinkForm.getFormOfWay();
                if (rdLinkForm.changedFields().containsKey("formOfWay"))
                    formOfWay = (int) rdLinkForm.changedFields().get("formOfWay");
                //rdlinkform有新增或者修改环岛记录的才进行检查，其他情况的即使原来有环岛link也不需要触发检查
                if (formOfWay != 33) {
                    continue;
                }

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
            }
        }
    }

    private void checkWithRdLink(RdLink rdLink, List<Integer> linkPidList) throws Exception {
        //获取rdLink对应的链
        HashSetRdLinkAndPid huandaoChain = getLoader().loadHandaoChain(getConn(), rdLink);

        linkPidList.removeAll(huandaoChain.getRdLinkPidSet());
        linkPidList.addAll(huandaoChain.getRdLinkPidSet());

        int kind = rdLink.getKind();
        if (rdLink.changedFields().containsKey("kind"))
            kind = (int) rdLink.changedFields().get("kind");
        Iterator<RdLink> huandaoIterator = huandaoChain.iterator();
        String target = "";
        boolean isError = false;
        while (huandaoIterator.hasNext()) {
            RdLink linkObj = huandaoIterator.next();
            if (!target.isEmpty()) {
                target = target + ";";
            }
            target = target + "[RD_LINK," + linkObj.getPid() + "]";
            if (kind != linkObj.getKind()) {
                isError = true;
            }
        }
        if (isError) {
            this.setCheckResult(rdLink.getGeometry(), target, rdLink.getMeshId());
        }
    }


}
