package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.meta.ScRoadKindForm;
import com.navinfo.dataservice.engine.check.model.utils.RdLinkFormUtils;
import com.navinfo.dataservice.engine.check.model.utils.RdLinkUtils;
import com.vividsolutions.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.hadoop.yarn.webapp.hamlet.HamletSpec.Scope.row;

/*
 * GLM01221	Link信息	特殊种别属性	形态	道路种别和形态共存性参见元数据库SC_ROAD_KIND_FORM	种别属性不匹配
 */
public class GLM01221 extends baseRule {

    public GLM01221() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
//        List<Integer> linkPidList = new ArrayList<Integer>();
//        for (IRow obj : checkCommand.getGlmList()) {
//            if (obj instanceof RdLink) {
//                RdLink rdLink = (RdLink) obj;
//
//                if (linkPidList.contains(rdLink.getPid())) {
//                    continue;
//                }
//
//                Map<String, Object> changedFields = rdLink.changedFields();
//                if (changedFields != null && !changedFields.containsKey("kind") && !changedFields.containsKey
//                        ("specialTraffic")
//                        && !changedFields.containsKey("multiDigitized") && !changedFields.containsKey("imiCode")
//                        && !changedFields.containsKey("isViaduct")) {
//                    continue;
//                }
//
//                ISelector rdFormSelector = new AbstractSelector(RdLinkForm.class, getConn());
//                List<IRow> rdLinkFormList = rdFormSelector.loadRowsByParentId(rdLink.getPid(), false);
//
//                checkWithRdLink(rdLink, rdLinkFormList, linkPidList);
//            } else if (obj instanceof RdLinkForm) {
//                RdLinkForm rdLinkForm = (RdLinkForm) obj;
//                int linkPid = rdLinkForm.getLinkPid();
//
//                if (linkPidList.contains(linkPid)) {
//                    continue;
//                }
//                RdLinkSelector rdSelector = new RdLinkSelector(getConn());
//                RdLink rdLink = (RdLink) rdSelector.loadByIdOnlyRdLink(linkPid, false);
//
//                checkWithRdLink(rdLink, rdLink.getForms(), linkPidList);
//            }
//        }

        for (IRow obj : checkCommand.getGlmList()) {
            if (obj instanceof RdLink) {
                RdLink link = (RdLink) obj;
                int kind = link.getKind();
                if (link.changedFields().containsKey("kind"))
                    kind = (int) link.changedFields().get("kind");
                if (kind != 11 && kind != 13)
                    continue;

                List<IRow> forms = new AbstractSelector(RdLinkForm.class, getConn()).loadRowsByParentId(link.pid(),
                        false);
                for (IRow row : forms) {
                    RdLinkForm form = (RdLinkForm) row;
                    if (form.getFormOfWay() != 0)
                        this.setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                }
            }
        }
    }

    /*
     * 道路种别和形态共存性参见元数据库SC_ROAD_KIND_FORM
     */
    private void checkWithRdLink(RdLink rdLink, List<IRow> rdLinkFormList, List<Integer> linkPidList) throws Exception {
        int kind = rdLink.getKind();

        Map<Integer, Map<String, List<Integer>>> formMap = ScRoadKindForm.kindFormLoader();
        Set<Integer> formKeySet = formMap.keySet();
        boolean error = false;
        List<String> errorLog = new ArrayList<String>();
        if (!formKeySet.contains((Integer) kind)) {
            linkPidList.add(rdLink.pid());
            errorLog.add("不应出现在数据中的道路等级");
            error = true;
        } else {
            Set<Integer> formSet = RdLinkFormUtils.formToList(rdLinkFormList);

            Map<String, List<Integer>> formCoexist = new HashMap<String, List<Integer>>();
            formCoexist = formMap.get(kind);

            String kindChi = RdLinkUtils.kindToChi(kind);

            List<Integer> formOfWays = new ArrayList<Integer>();
            formOfWays.addAll(formSet);
            formOfWays.removeAll(formCoexist.get("FORM_OF_WAYS"));
            if (formOfWays.size() > 0) {
                String formOfWayslog = "";
                for (int i = 0; i < formOfWays.size(); i++) {
                    if (i > 0) {
                        formOfWayslog = formOfWayslog + ",";
                    }
                    formOfWayslog = formOfWayslog + RdLinkFormUtils.formToChi(formOfWays.get(i));
                }
                error = true;
                errorLog.add(kindChi + "link与" + formOfWayslog + "FORM属性值不匹配");
            }
            if (!formCoexist.isEmpty() && formCoexist.size() > 0) {
                int multiDigitized = formCoexist.get("MULTI_DIGITIZED").get(0);
                if (rdLink.getMultiDigitized() != multiDigitized) {
                    error = true;
                    errorLog.add(kindChi + "link,上下线分离属性错误");
                }

                List<Integer> imiCodelist = formCoexist.get("IMI_CODE");
                if (!imiCodelist.contains(rdLink.getImiCode())) {
                    error = true;
                    errorLog.add(kindChi + "link,IMI属性值错误");
                }

                int specialTraffic = formCoexist.get("SPECIAL_TRAFFIC").get(0);
                if (rdLink.getSpecialTraffic() != specialTraffic) {
                    error = true;
                    errorLog.add(kindChi + "link,特殊交通类型错误");
                }

                int isViaduct = formCoexist.get("IS_VIADUCT").get(0);
                if (rdLink.getIsViaduct() != isViaduct) {
                    error = true;
                    errorLog.add(kindChi + "link,是否高架属性值错误");
                }
            }
        }

        if (error) {
            String target = "[RD_LINK," + rdLink.getPid() + "]";
            Geometry geo = rdLink.getGeometry();
            int mesh = rdLink.getMeshId();
            for (int i = 0; i < errorLog.size(); i++) {
                this.setCheckResult(geo, target, mesh, errorLog.get(i));
            }
        }
    }
}
