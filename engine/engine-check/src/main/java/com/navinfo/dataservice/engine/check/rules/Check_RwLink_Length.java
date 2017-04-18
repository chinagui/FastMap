package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * 前检查：铁路link长度应大于2米
 * @author Feng Haixia
 * @since 2017/4/12
 */
public class Check_RwLink_Length extends baseRule {
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (!(row instanceof RwLink) || row.status() == ObjStatus.DELETE) {
				continue;
			}

			RwLink rwLink = (RwLink) row;
			double length = rwLink.getLength();

			if (rwLink.changedFields().containsKey("length")) {
				length = (double) rwLink.changedFields().get("length");
			}

			if (length <= 2.0) {
				this.setCheckResult("", "", 0);
			}
		}
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {
	}
}
