package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * @Title: GLM01033
 * @Package: com.navinfo.dataservice.engine.check.rules
 * @Description:
 *      检查对象：RD_LINK
 *      检查原则：1、如果普通路上（3级含3级以下）的供用信息不是“可以通行”（APP_INFO≠1），则报log1；
 *          2、如果高速城高道路上的供用信息不是“可以通行”或“未供用”（APP_INFO≠1、3），则报log2
 * @Author: Crayeres
 * @Date: 2017/3/28
 * @Version: V1.0
 */
public class GLM01033 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
                RdLink link = (RdLink) row;

                // 获取最新的link种别
                int kind = link.getKind();
                if (link.changedFields().containsKey("kind"))
                    kind = Integer.valueOf(link.changedFields().get("kind").toString());

                // 获取最新的link供用信息
                int appInfo = link.getAppInfo();
                if (link.changedFields().containsKey("appInfo"))
                    appInfo = Integer.valueOf(link.changedFields().get("appInfo").toString());

                // 判断是否满足检查原则1
                if (kind >= 3) {
                    if (appInfo != 1) {
                        setCheckResult("", "[RD_LINK," + link.pid() + "]", 0, "普通道路供用信息类型错误！");
                    }
                // 判断是否满足检查原则2
                } else {
                    if (appInfo != 1 && appInfo != 3)
                        setCheckResult("", "[RD_LINK," + link.pid() + "]", 0, "高速城高道路供用信息类型错误！");
                }
            }
        }
    }
}
