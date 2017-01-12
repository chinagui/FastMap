package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.mileagepile.RdMileagepile;
import com.navinfo.dataservice.engine.check.core.baseRule;
import net.sf.json.JSONObject;

/**
 * Created by chaixin on 2016/12/7 0007.
 */
public class GLM37011 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow obj : checkCommand.getGlmList()) {
            if (obj instanceof RdMileagepile) {
                RdMileagepile mileagepile = new RdMileagepile();
                mileagepile.copy(obj);
                mileagepile.Unserialize(JSONObject.fromObject(obj.changedFields()));

                String roadNum = mileagepile.getRoadNum();
                boolean hasLetter = false;
                boolean hasDigit = false;
                for (Character c : roadNum.toCharArray()) {
                    if (Character.isLetter(c)) {
                        hasLetter = true;
                    } else if (Character.isDigit(c)) {
                        hasDigit = true;
                    } else {
                        setCheckResult(mileagepile.getGeometry(), "[RD_MILEAGEPILE," + mileagepile.pid() + "]", mileagepile.getMeshId());
                        break;
                    }
                }
                if (!hasLetter || !hasDigit)
                    setCheckResult(mileagepile.getGeometry(), "[RD_MILEAGEPILE," + mileagepile.pid() + "]", mileagepile.getMeshId());
            }
        }
    }
}
