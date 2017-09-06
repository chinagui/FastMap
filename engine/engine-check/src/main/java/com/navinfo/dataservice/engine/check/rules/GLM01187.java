package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.model.utils.RdLinkFormUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * GLM01187	Link信息	属性不匹配	形态	道路属性共存性参照元数据库表SC_ROAD_FORMS_COEXIST
 */
public class GLM01187 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLinkForm && row.status() != ObjStatus.DELETE) {
                List<IRow> forms = new AbstractSelector(RdLinkForm.class, getConn()).loadRowsByParentId(((RdLinkForm)
                        row).getLinkPid(), false);

                StringBuffer log = new StringBuffer();
                for (IRow f : forms) {
                    RdLinkForm form = (RdLinkForm) f;

                    if (!CHECK_RULE.containsKey(Integer.valueOf(form.getFormOfWay())))
                        continue;

                    StringBuffer buffer = new StringBuffer();
                    List<Integer> checkForms = CHECK_RULE.get(Integer.valueOf(form.getFormOfWay()));
                    for (IRow ff : forms) {
                        RdLinkForm fform = (RdLinkForm) ff;
                        if (checkForms.contains(Integer.valueOf(fform.getFormOfWay()))) {
                            buffer.append(RdLinkFormUtils.formToChi(fform.getFormOfWay())).append(",");
                        }
                    }
                    if (StringUtils.isEmpty(buffer.toString()))
                        continue;

                    log.append(RdLinkFormUtils.formToChi(form.getFormOfWay()) + "与" + buffer.substring(0, buffer
                            .length() - 1) + "FORM属性值不匹配; ");
                }
                if (StringUtils.isNotEmpty(log.toString())) {
                    setCheckResult("", "[RD_LINK," + ((RdLinkForm) row).getLinkPid() + "]", 0, log.substring(0, log
                            .length() - 2));
                }
            }
        }
    }

    private static Map<Integer, List<Integer>> CHECK_RULE = new HashMap() {{
        put(10, Arrays.asList(1, 2, 11));
        put(11, Arrays.asList(37, 38));
        put(12, Arrays.asList(1, 2));
        put(13, Arrays.asList(1, 2));
        put(14, Arrays.asList(1, 2, 37, 50));
        put(15, Arrays.asList(1, 2, 34, 36));
        put(20, Arrays.asList(1));
        put(22, Arrays.asList(1, 2, 24, 37, 39, 52));
        put(24, Arrays.asList(52));
        put(30, Arrays.asList(1, 31));
        put(31, Arrays.asList(1));
        put(33, Arrays.asList(1));
        put(36, Arrays.asList(1));
        put(37, Arrays.asList(1));
        put(38, Arrays.asList(1));
        put(39, Arrays.asList(1));
        put(50, Arrays.asList(1));
        put(52, Arrays.asList(1, 2));
    }};
    //
    //public GLM01187() {
    //    // TODO Auto-generated constructor stub
    //}
    //
    //@Override
    //public void preCheck(CheckCommand checkCommand) throws Exception {
    //    // TODO Auto-generated method stub
    //}
    //
    //@Override
    //public void postCheck(CheckCommand checkCommand) throws Exception {
    //    List<Integer> linkPidList = new ArrayList<Integer>();
    //    for (IRow obj : checkCommand.getGlmList()) {
    //        if (obj instanceof RdLink) {
    //            RdLink rdLink = (RdLink) obj;
    //
    //            if (linkPidList.contains(rdLink.getPid())) {
    //                continue;
    //            }
    //
    //            Map<String, Object> changedFields = rdLink.changedFields();
    //            if (changedFields != null && !changedFields.containsKey("specialTraffic")
    //                    && !changedFields.containsKey("multiDigitized") && !changedFields.containsKey("imiCode")
    //                    && !changedFields.containsKey("isViaduct")) {
    //                continue;
    //            }
    //
    //		/*RdLinkFormSelector rdFormSelector=new RdLinkFormSelector(getConn());
    //            List<IRow> rdLinkFormList=rdFormSelector.loadRowsByParentId(rdLink.getPid(), false);
    //		*/
    //            checkWithRdLink(rdLink, rdLink.getForms(), linkPidList);
    //        } else if (obj instanceof RdLinkForm) {
    //            RdLinkForm rdLinkForm = (RdLinkForm) obj;
    //            int linkPid = rdLinkForm.getLinkPid();
    //
    //            if (linkPidList.contains(linkPid)) {
    //                continue;
    //            }
    //            RdLinkSelector rdSelector = new RdLinkSelector(getConn());
    //            RdLink rdLink = (RdLink) rdSelector.loadByIdOnlyRdLink(linkPid, false);
    //
    //            ISelector rdFormSelector = new AbstractSelector(RdLinkForm.class, getConn());
    //            List<IRow> rdLinkFormList = rdFormSelector.loadRowsByParentId(rdLink.getPid(), false);
    //
    //            checkWithRdLink(rdLink, rdLinkFormList, linkPidList);
    //        }
    //    }
    //}
    //
    ///*
    // * 道路属性共存性参照元数据库表SC_ROAD_KIND_FORM
    // */
    //private void checkWithRdLink(RdLink rdLink, List<IRow> rdLinkFormList, List<Integer> linkPidList) throws
    // Exception {
    //    Set<Integer> formSet = RdLinkFormUtils.formToList(rdLinkFormList);
    //    if (formSet.size() == 0) {
    //        return;
    //    }
    //
    //    Map<Integer, Map<String, List<Integer>>> formMap = ScRoadKindForm.kindFormLoader();
    //    Set<Integer> formKeySet = formMap.keySet();
    //    formKeySet.retainAll(formSet);
    //    Iterator<Integer> iteratorFormKey = formKeySet.iterator();
    //    boolean error = false;
    //    Map<String, List<Integer>> formCoexist = new HashMap<String, List<Integer>>();
    //    List<String> errorLog = new ArrayList<String>();
    //    String currentFormChi = "";
    //    while (iteratorFormKey.hasNext()) {
    //        int currentForm = iteratorFormKey.next();
    //        formCoexist = formMap.get(currentForm);
    //        currentFormChi = RdLinkFormUtils.formToChi(currentForm);
    //        List<Integer> formOfWays = new ArrayList<Integer>();
    //        formOfWays.addAll(formSet);
    //        formOfWays.remove((Integer) currentForm);
    //        formOfWays.removeAll(formCoexist.get("FORM_OF_WAYS"));
    //        if (formOfWays.size() > 0) {
    //            String formOfWayslog = "";
    //            for (int i = 0; i < formOfWays.size(); i++) {
    //                if (i > 0) {
    //                    formOfWayslog = formOfWayslog + ",";
    //                }
    //                formOfWayslog = formOfWayslog + RdLinkFormUtils.formToChi(formOfWays.get(i));
    //            }
    //            error = true;
    //            errorLog.add(currentFormChi + "与" + formOfWayslog + "FORM属性值不匹配");
    //        }
    //        break;
    //    }
    //    if (!formCoexist.isEmpty() && formCoexist.size() > 0) {
    //        int multiDigitized = formCoexist.get("MULTI_DIGITIZED").get(0);
    //        if (rdLink.getMultiDigitized() != multiDigitized) {
    //            error = true;
    //            errorLog.add(currentFormChi + "link,上下线分离属性错误");
    //        }
    //
    //        List<Integer> imiCodelist = formCoexist.get("IMI_CODE");
    //        if (!imiCodelist.contains(rdLink.getImiCode())) {
    //            error = true;
    //            errorLog.add(currentFormChi + "link,IMI属性值错误");
    //        }
    //
    //        int specialTraffic = formCoexist.get("SPECIAL_TRAFFIC").get(0);
    //        if (rdLink.getSpecialTraffic() != specialTraffic) {
    //            error = true;
    //            errorLog.add(currentFormChi + "link,特殊交通类型错误");
    //        }
    //
    //        int isViaduct = formCoexist.get("IS_VIADUCT").get(0);
    //        if (rdLink.getIsViaduct() != isViaduct) {
    //            error = true;
    //            errorLog.add(currentFormChi + "link,是否高架属性值错误");
    //        }
    //    }
    //
    //    if (error) {
    //        String target = "[RD_LINK," + rdLink.getPid() + "]";
    //        Geometry geo = rdLink.getGeometry();
    //        int mesh = rdLink.getMeshId();
    //        for (int i = 0; i < errorLog.size(); i++) {
    //            this.setCheckResult(geo, target, mesh, errorLog.get(i));
    //        }
    //    }
    //}
}
