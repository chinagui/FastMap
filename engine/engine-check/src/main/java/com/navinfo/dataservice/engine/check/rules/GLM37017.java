package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.mileagepile.RdMileagepile;
import com.navinfo.dataservice.engine.check.core.baseRule;
import net.sf.json.JSONObject;

/**
 * Created by chaixin on 2016/12/7 0007.
 */
public class GLM37017 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow obj : checkCommand.getGlmList()) {
            if (obj instanceof RdMileagepile) {
                RdMileagepile mileagepile = (RdMileagepile) obj;

                String roadNum = mileagepile.getRoadNum();
                //log.info("GLM37017:[roadNum=" + roadNum + "]");
                if (mileagepile.changedFields().containsKey("roadNum"))
                    roadNum = String.valueOf(mileagepile.changedFields().get("roadNum").toString());
                //log.info("GLM37017:[roadNum=" + roadNum + "]");

                if (null == roadNum || roadNum.length() == 0) {
                    setCheckResult(mileagepile.getGeometry(), "[RD_MILEAGEPILE," + mileagepile.pid() + "]",
                            mileagepile.getMeshId());
                }
            }
        }
    }
}
