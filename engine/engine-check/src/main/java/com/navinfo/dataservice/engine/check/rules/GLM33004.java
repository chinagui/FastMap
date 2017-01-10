package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeedVia;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

import java.util.ArrayList;
import java.util.List;

import static jdk.nashorn.internal.parser.TokenKind.IR;

/**
 * Created by chaixin on 2017/1/9 0009.
 */
public class GLM33004 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdVariableSpeed) {
                RdVariableSpeed speed = (RdVariableSpeed) row;
                List<IRow> vias = speed.getVias();
                if (vias.isEmpty())
                    continue;
                List<Integer> viaPids = new ArrayList<>();
                for (IRow via : vias) {
                    viaPids.add(((RdVariableSpeedVia) via).getLinkPid());
                }
                String sql = "SELECT 1 FROM RD_VARIABLE_SPEED_VIA RVSV WHERE RVSV.LINK_PID IN (" +
                        StringUtils.getInteStr(viaPids) + ") AND RVSV.U_RECORD != 2";
                DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
                List<Object> resultList = getObj.exeSelect(getConn(), sql);
                if (!resultList.isEmpty())
                    setCheckResult("", "", 0);
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
