package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.model.utils.RdLinkFormUtils;
import com.navinfo.dataservice.engine.check.model.utils.RdLinkUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * GLM01221	Link信息	特殊种别属性	形态	道路种别和形态共存性参见元数据库SC_ROAD_KIND_FORM	种别属性不匹配
 */
public class GLM01221 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
                RdLink link = (RdLink) row;

                int kind = link.getKind();
                if (link.changedFields().containsKey("kind"))
                    kind = Integer.valueOf(link.changedFields().get("kind").toString());

                checkForms(link, kind);
            } else if (row instanceof RdLinkForm && row.status() != ObjStatus.DELETE) {
                RdLink link = (RdLink) new RdLinkSelector(getConn()).loadById(((RdLinkForm) row).getLinkPid(), false);
                int kind = link.getKind();

                checkForms(link, kind);
            }
        }
    }

    private void checkForms(RdLink link, int kind) throws Exception {
        if (!CHECK_RULE.containsKey(Integer.valueOf(kind)))
            return;

        List<Integer> checkForms = CHECK_RULE.get(Integer.valueOf(kind));

        List<IRow> forms = new AbstractSelector(RdLinkForm.class, getConn()).loadRowsByParentId(link.pid(), false);
        StringBuffer formLog = new StringBuffer();
        for (IRow f : forms) {
            RdLinkForm form = (RdLinkForm) f;
            if (checkForms.contains(form.getFormOfWay())) {
                formLog.append(RdLinkFormUtils.formToChi(form.getFormOfWay())).append(", ");
            }
        }
        if (formLog.length() > 0) {
            setCheckResult("", "[RD_LINK," + link.pid() + "]", 0, RdLinkUtils.kindToChi(kind) + "LINK与" + formLog
                    .substring(0, formLog.length() - 2) + " FORM属性值不匹配");
        }
    }

    private static Map<Integer, List<Integer>> CHECK_RULE = new HashMap() {{
        put(1, Arrays.asList(52));
        put(2, Arrays.asList(34));
        put(3, Arrays.asList(10, 52));
        put(4, Arrays.asList(14));
        put(6, Arrays.asList(10, 80));
        put(7, Arrays.asList(2, 10));
        put(8, Arrays.asList(2, 10, 21, 51));
        put(9, Arrays.asList(2, 35, 37));
        put(10, Arrays.asList(2, 20, 35, 36));
    }};

    //    @Override
    //    public void postCheck(CheckCommand checkCommand) throws Exception {
    ////        List<Integer> linkPidList = new ArrayList<Integer>();
    ////        for (IRow obj : checkCommand.getGlmList()) {
    ////            if (obj instanceof RdLink) {
    ////                RdLink rdLink = (RdLink) obj;
    ////
    ////                if (linkPidList.contains(rdLink.getPid())) {
    ////                    continue;
    ////                }
    ////
    ////                Map<String, Object> changedFields = rdLink.changedFields();
    ////                if (changedFields != null && !changedFields.containsKey("kind") && !changedFields.containsKey
    ////                        ("specialTraffic")
    ////                        && !changedFields.containsKey("multiDigitized") && !changedFields.containsKey("imiCode")
    ////                        && !changedFields.containsKey("isViaduct")) {
    ////                    continue;
    ////                }
    ////
    ////                ISelector rdFormSelector = new AbstractSelector(RdLinkForm.class, getConn());
    ////                List<IRow> rdLinkFormList = rdFormSelector.loadRowsByParentId(rdLink.getPid(), false);
    ////
    ////                checkWithRdLink(rdLink, rdLinkFormList, linkPidList);
    ////            } else if (obj instanceof RdLinkForm) {
    ////                RdLinkForm rdLinkForm = (RdLinkForm) obj;
    ////                int linkPid = rdLinkForm.getLinkPid();
    ////
    ////                if (linkPidList.contains(linkPid)) {
    ////                    continue;
    ////                }
    ////                RdLinkSelector rdSelector = new RdLinkSelector(getConn());
    ////                RdLink rdLink = (RdLink) rdSelector.loadByIdOnlyRdLink(linkPid, false);
    ////
    ////                checkWithRdLink(rdLink, rdLink.getForms(), linkPidList);
    ////            }
    ////        }
    //
    //        for (IRow obj : checkCommand.getGlmList()) {
    //            if (obj instanceof RdLink) {
    //                RdLink link = (RdLink) obj;
    //                int kind = link.getKind();
    //                if (link.changedFields().containsKey("kind"))
    //                    kind = (int) link.changedFields().get("kind");
    //                if (kind != 11 && kind != 13)
    //                    continue;
    //
    //                List<IRow> forms = new AbstractSelector(RdLinkForm.class, getConn()).loadRowsByParentId(link
    // .pid(),
    //                        false);
    //                for (IRow row : forms) {
    //                    RdLinkForm form = (RdLinkForm) row;
    //                    if (form.getFormOfWay() != 0)
    //                        this.setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
    //                }
    //            }
    //        }
    //    }
    //
    //    /*
    //     * 道路种别和形态共存性参见元数据库SC_ROAD_KIND_FORM
    //     */
    //    private void checkWithRdLink(RdLink rdLink, List<IRow> rdLinkFormList, List<Integer> linkPidList) throws
    // Exception {
    //        int kind = rdLink.getKind();
    //
    //        Map<Integer, Map<String, List<Integer>>> formMap = ScRoadKindForm.kindFormLoader();
    //        Set<Integer> formKeySet = formMap.keySet();
    //        boolean error = false;
    //        List<String> errorLog = new ArrayList<String>();
    //        if (!formKeySet.contains((Integer) kind)) {
    //            linkPidList.add(rdLink.pid());
    //            errorLog.add("不应出现在数据中的道路等级");
    //            error = true;
    //        } else {
    //            Set<Integer> formSet = RdLinkFormUtils.formToList(rdLinkFormList);
    //
    //            Map<String, List<Integer>> formCoexist = new HashMap<String, List<Integer>>();
    //            formCoexist = formMap.get(kind);
    //
    //            String kindChi = RdLinkUtils.kindToChi(kind);
    //
    //            List<Integer> formOfWays = new ArrayList<Integer>();
    //            formOfWays.addAll(formSet);
    //            formOfWays.removeAll(formCoexist.get("FORM_OF_WAYS"));
    //            if (formOfWays.size() > 0) {
    //                String formOfWayslog = "";
    //                for (int i = 0; i < formOfWays.size(); i++) {
    //                    if (i > 0) {
    //                        formOfWayslog = formOfWayslog + ",";
    //                    }
    //                    formOfWayslog = formOfWayslog + RdLinkFormUtils.formToChi(formOfWays.get(i));
    //                }
    //                error = true;
    //                errorLog.add(kindChi + "link与" + formOfWayslog + "FORM属性值不匹配");
    //            }
    //            if (!formCoexist.isEmpty() && formCoexist.size() > 0) {
    //                int multiDigitized = formCoexist.get("MULTI_DIGITIZED").get(0);
    //                if (rdLink.getMultiDigitized() != multiDigitized) {
    //                    error = true;
    //                    errorLog.add(kindChi + "link,上下线分离属性错误");
    //                }
    //
    //                List<Integer> imiCodelist = formCoexist.get("IMI_CODE");
    //                if (!imiCodelist.contains(rdLink.getImiCode())) {
    //                    error = true;
    //                    errorLog.add(kindChi + "link,IMI属性值错误");
    //                }
    //
    //                int specialTraffic = formCoexist.get("SPECIAL_TRAFFIC").get(0);
    //                if (rdLink.getSpecialTraffic() != specialTraffic) {
    //                    error = true;
    //                    errorLog.add(kindChi + "link,特殊交通类型错误");
    //                }
    //
    //                int isViaduct = formCoexist.get("IS_VIADUCT").get(0);
    //                if (rdLink.getIsViaduct() != isViaduct) {
    //                    error = true;
    //                    errorLog.add(kindChi + "link,是否高架属性值错误");
    //                }
    //            }
    //        }
    //
    //        if (error) {
    //            String target = "[RD_LINK," + rdLink.getPid() + "]";
    //            Geometry geo = rdLink.getGeometry();
    //            int mesh = rdLink.getMeshId();
    //            for (int i = 0; i < errorLog.size(); i++) {
    //                this.setCheckResult(geo, target, mesh, errorLog.get(i));
    //            }
    //        }
    //    }
}
