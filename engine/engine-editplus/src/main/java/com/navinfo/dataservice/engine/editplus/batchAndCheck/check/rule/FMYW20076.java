package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.utils.RdLinkFormUtils;
import com.navinfo.dataservice.engine.editplus.utils.RdLinkUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @Title: FMYW20076
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description: 检查条件：非删除POI对象
 * 检查规则：
 * 1、普通POI【kind_code不为公交POI（230101，230102，230110，230112，230115，230117，230118，230119，230120，230121，230122，230123，230124），道路名，服务区（230206
 * ），停车区（230207），桥（230201），停车场（230210，230213，230214），公共厕所（210215），报刊零售（130403），日杂店（130104）】关联link表中形态（RD_LINK_FORM
 * .FORM_OF_WAY）中的交叉点内道路（50）时，报log1
 * 2、服务区（230206）、停车区（230207）关联link表中功能等级（RD_LINK.FUNCTION_CLASS）中值为1,2,3时，报log2
 * 3.服务区（230206）、停车区（230207）POI关联link表中形态（RD_LINK_FORM
 * .FORM_OF_WAY）中的匝道（15），隧道（31），步行街（20），公交专用道（22），环岛（33），桥（30），交叉点内道路（50），区域内道路（52），全封闭道路（14）时，报log3
 * 4、服务区（230206）、停车区（230207）POI关联link表中种别代码（RD_LINK.KIND）中的轮渡（13），人渡（11），10级路(障碍物)（15），其它道路（8）时，报log4
 * 备注：种别代码kind=高速道路（1），城市高速（2）时，第2,3,4条规则都不报log
 * @Author: Crayeres
 * @Date: 7/10/2017
 * @Version: V1.0
 */
public class FMYW20076 extends BasicCheckRule {

    private final static List<String> RULE_ONE = Arrays.asList("230101", "230102", "230110", "230112", "230115", "230117", "230118",
            "230119", "230120", "230121", "230122", "230123", "230124", "230206", "230207", "230201", "230210", "230213", "230214",
            "230215", "130403", "130104");

    private final static List<String> RULE_TWO = Arrays.asList("230222", "230206", "230207");

    @Override
    public void runCheck(BasicObj obj) throws Exception {
        if (obj.objName().equals(ObjectName.IX_POI)) {
            IxPoiObj poiObj = (IxPoiObj) obj;
            IxPoi poi = (IxPoi) poiObj.getMainrow();

            String kindCode = poi.getKindCode();
            if (StringUtils.isEmpty(kindCode)) {
                return;
            }

            int linkPid = (int) poi.getLinkPid();
            if (linkPid == 0) {
                return;
            }

            RdLink link = (RdLink) new RdLinkSelector(getCheckRuleCommand().getConn()).loadById(linkPid, false);
            if (!RULE_ONE.contains(kindCode)) {
                List<IRow> forms = link.getForms();
                for (IRow row : forms) {
                    RdLinkForm form = (RdLinkForm) row;
                    if (form.getFormOfWay() == 50) {
                        setCheckResult(poi.getGeometry(), String.format("[IX_POI,%s]", poi.getPid()), poi.getMeshId(),
                                "普通POI不能与交叉点内道路关联，请修改");
                        return;
                    }
                }
            }

            int kind = link.getKind();
            if (kind == 1 || kind == 2) {
                return;
            }

            if (RULE_TWO.contains(kindCode)) {
                int functionClass = link.getFunctionClass();
                if (functionClass == 1 || functionClass == 2 || functionClass == 3) {
                    setCheckResult(poi.getGeometry(), String.format("[IX_POI,%s]", poi.getPid()), poi.getMeshId(), String.format
                            ("服务区、停车区POI不能与功能等级为%s级的道路关联，请修改；", functionClass));
                    return;
                }

                List<IRow> forms = link.getForms();
                for (IRow row : forms) {
                    int formOfWay = ((RdLinkForm) row).getFormOfWay();
                    if (formOfWay == 14 || formOfWay == 15 || formOfWay == 20 || formOfWay == 20 || formOfWay == 22 || formOfWay == 30
                            || formOfWay == 31 || formOfWay == 33 || formOfWay == 50 || formOfWay == 52) {
                        setCheckResult(poi.getGeometry(), String.format("[IX_POI,%s]", poi.getPid()), poi.getMeshId(), String.format
                                ("普通POI不能与%s关联，请修改", RdLinkFormUtils.formToChi(formOfWay)));
                        return;
                    }
                }

                if (kind == 8 || kind == 11 || kind == 13 || kind == 15) {
                    setCheckResult(poi.getGeometry(), String.format("[IX_POI,%s]", poi.getPid()), poi.getMeshId(), String.format
                            ("服务区、停车区POI不能与%s关联，请修改；", RdLinkUtils.kindToChi(kind)));
                    return;
                }
            }
        }
    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {

    }
}
