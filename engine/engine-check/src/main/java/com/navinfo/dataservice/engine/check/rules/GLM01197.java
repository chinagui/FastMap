package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.graph.HashSetRdLinkAndPid;

/*
 * GLM01197	Link信息	特殊交通类型检查	形态	同一个特殊交通类型的组成link，道路功能等级必须相同。	特殊交通类型的功能等级不同
 */
public class GLM01197 extends baseRule {

    public GLM01197() {
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
            //只有主表rdlink的修改才引起该检查项
            if (obj instanceof RdLink) {
                RdLink rdLink = (RdLink) obj;
                //一条特殊交通类型链上的link不重复检查
                if (linkPidList.contains(rdLink.getPid())) {
                    continue;
                }

                Map<String, Object> changedFields = rdLink.changedFields();
                if (changedFields != null && !changedFields.containsKey("specialTraffic")
                        && !changedFields.containsKey("functionClass")) {
                    continue;
                }

                //非特殊交通类型link不查此规则
                if (rdLink.getSpecialTraffic() == 0) {
                    linkPidList.add(rdLink.getPid());
                    continue;
                }
                checkWithRdLink(rdLink, linkPidList);
            }
        }
    }

    private void checkWithRdLink(RdLink rdLink, List<Integer> linkPidList) throws Exception {
        //获取rdLink对应的特殊交通类型链
        HashSetRdLinkAndPid specTrafficChain = getLoader().loadSpecTrafficChain(getConn(), rdLink);

        linkPidList.removeAll(specTrafficChain.getRdLinkPidSet());
        linkPidList.addAll(specTrafficChain.getRdLinkPidSet());

        int fc = rdLink.getFunctionClass();
        Iterator<RdLink> specIterator = specTrafficChain.iterator();
        String target = "";
        boolean isError = false;
        while (specIterator.hasNext()) {
            RdLink linkObj = specIterator.next();
            if (!target.isEmpty()) {
                target = target + ";";
            }
            target = target + "[RD_LINK," + linkObj.getPid() + "]";
            if (fc != linkObj.getFunctionClass()) {
                isError = true;
            }
        }
        if (isError) {
            this.setCheckResult(rdLink.getGeometry(), target, rdLink.getMeshId());
        }
    }

}
