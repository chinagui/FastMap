package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.mileagepile.RdMileagepile;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * Created by chaixin on 2016/12/7 0007.
 */
public class GLM37016 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow obj : checkCommand.getGlmList()) {
            if (obj instanceof RdMileagepile) {
                RdMileagepile mileagepile = (RdMileagepile) obj;

                String roadName = mileagepile.getRoadName();
                if(mileagepile.changedFields().containsKey("roadName"))
                    roadName = (String) mileagepile.changedFields().get("roadName");

                String regex = "[0-9a-zA-Z|]{1}";
                roadName = roadName.replaceAll(regex, "");
                if (roadName.length() == 0) {
                    setCheckResult(mileagepile.getGeometry(), "[RD_MILEAGEPILE," + mileagepile.pid() + "]", mileagepile.getMeshId());
                }
            }
        }
    }

    public static void main(String[] args) {
        String regex = "[0-9a-zA-Z|]{1}";
        System.out.println("aaa111bbb||||啊".replaceAll(regex, "").length());
    }
}
