package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;

import java.util.Arrays;

/**
 * Created by Crayeres on 2017/2/9.
 */
public class GLM02085_1 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        // LINK门牌表功能暂未开发 无需进行检查
        //for (IRow row : checkCommand.getGlmList()) {
        //    if (row instanceof RdLinkForm && row.status() != ObjStatus.DELETE) {
        //        RdLinkForm form = (RdLinkForm) row;
        //
        //        int formOfWay = form.getFormOfWay();
        //        if (form.changedFields().containsKey("formOfWay"))
        //            formOfWay = (int) form.changedFields().get("formOfWay");
        //
        //        if (Arrays.asList(new Integer[]{15, 33, 14, 31, 30, 50, 22, 32}).contains(formOfWay)) {
        //            // 判断是否拥有门牌信息
        //        }
        //    }
        //    // 匹配LINK门牌表信息
        //}
    }
}
