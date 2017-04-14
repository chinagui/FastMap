package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkKind;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * Created by chaixin on 2017/1/13 0013.
 * 行政界假想线种别，只能和无属性共存
 * LINK种别编辑服务端后检查
 * LINK属性编辑服务端后检查
 */
public class GLM50008 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
    	for (IRow row : checkCommand.getGlmList()) {
    		if (row instanceof AdLink) {
    			AdLink adLink = (AdLink) row;
    			int kind = adLink.getKind();
    			if (adLink.changedFields().containsKey("kind")) {
    				kind = (int) adLink.changedFields().get("kind");
    			}
    			if (kind != 0)
    				continue;
    			
    			int form = adLink.getForm();
    			if (adLink.changedFields().containsKey("form"))
    				form = (int) adLink.changedFields().get("form");
    			if ((kind == 0)&&(form != 1)){
    				String target = "[AD_LINK, " + adLink.pid() + "]";
    				setCheckResult("", target, 0);
    			} 
    		}
    	}
    }
}
