package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.graph.HashSetRdLinkAndPid;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/*
 * GLM01208	Link信息	大陆环岛检查	形态	环岛接续link方向都为进入该环岛或都为退出该环岛，报err；
 * "1.环岛接续link均为进入环岛方向2.环岛接续link均为退出环岛方向"
*/
public class GLM01208 extends baseRule {

    public GLM01208() {
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

                //rdlinkform有新增或者修改环岛记录的才进行检查，其他情况的即使原来有环岛link也不需要触发检查
                if (rdLinkForm.getFormOfWay() != 33) {
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

        String sqlCommon = "WITH T AS"
                + " (SELECT E_NODE_PID AS NODE_ID"
                + "    FROM RD_LINK"
                + "   WHERE LINK_PID IN ( " + huandaoChain.getRdLinkPidSet().toString().replace("[", "").replace("]",
                "") + ")"
                + " AND U_RECORD != 2"
                + "  UNION"
                + "  SELECT S_NODE_PID AS NODE_ID FROM RD_LINK WHERE LINK_PID IN ( " + huandaoChain.getRdLinkPidSet()
                .toString().replace("[", "").replace("]", "") + ")"
                + " AND U_RECORD != 2)"
                + " SELECT DISTINCT 1"
                + "  FROM T, RD_LINK L"
                + " WHERE L.U_RECORD != 2"
                + " AND NOT EXISTS (SELECT 1"
                + "          FROM RD_LINK_FORM F"
                + "         WHERE L.LINK_PID = F.LINK_PID"
                + "           AND F.FORM_OF_WAY = 33"
                + "           AND F.U_RECORD != 2)";

        DatabaseOperator getObj = new DatabaseOperator();
        List<Object> resultList = new ArrayList<Object>();

        //环岛挂接link有双方向
        String sql = sqlCommon
                + "   AND ((T.NODE_ID = L.E_NODE_PID AND L.DIRECT IN (0, 1)) OR"
                + "       (T.NODE_ID = L.S_NODE_PID AND L.DIRECT IN (0, 1)))";
        resultList = getObj.exeSelect(this.getConn(), sql);
        if (resultList.size() > 0) {
            return;
        }

        //环岛挂接link没有进入环岛
        sql = sqlCommon
                + "   AND ((T.NODE_ID = L.E_NODE_PID AND L.DIRECT = 2) OR"
                + " (T.NODE_ID = L.S_NODE_PID AND L.DIRECT = 3))";
        resultList = getObj.exeSelect(this.getConn(), sql);
        if (resultList.size() == 0) {
            this.setCheckResult(rdLink.getGeometry(),
                    huandaoChain.getRdLinkPidSet().toString().replace(" ", "").
                            replace("[", "[RD_LINK%").replace(",", "];[RD_LINK,").replace("%", ","),
                    rdLink.getMeshId(), "环岛接续link均为退出环岛方向");
            return;
        }

        //环岛挂接link没有退出环岛
        sql = sqlCommon
                + "   AND ((T.NODE_ID = L.E_NODE_PID AND L.DIRECT = 3) OR"
                + " (T.NODE_ID = L.S_NODE_PID AND L.DIRECT = 2))";
        resultList = getObj.exeSelect(this.getConn(), sql);
        if (resultList.size() == 0) {
            this.setCheckResult(rdLink.getGeometry(),
                    huandaoChain.getRdLinkPidSet().toString().replace(" ", "").
                            replace("[", "[RD_LINK%").replace(",", "];[RD_LINK,").replace("%", ","),
                    rdLink.getMeshId(), "环岛接续link均为进入环岛方向");
            return;
        }
    }
}
