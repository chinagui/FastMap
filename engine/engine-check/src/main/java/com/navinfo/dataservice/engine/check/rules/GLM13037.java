package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgateName;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.tollgate.RdTollgateSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * 关系型收费站必须存在收费站名称，否则报LOG
 * 
 */
public class GLM13037 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {

		// 本次操作涉及到的收费站的pid
		List<Integer> tollgatePids = new ArrayList<Integer>();

		for (IRow obj : checkCommand.getGlmList()) {

			if (obj instanceof RdTollgate) {

				RdTollgate tollgate = (RdTollgate) obj;

				if (!tollgatePids.contains(tollgate.pid())) {

					tollgatePids.add(tollgate.pid());
				}
			}

			if (obj instanceof RdTollgateName) {

				RdTollgateName tollgateName = (RdTollgateName) obj;

				if (!tollgatePids.contains(tollgateName.getPid())) {

					tollgatePids.add(tollgateName.getPid());
				}
			}
		}

		if (tollgatePids.size() == 0) {
			return;
		}

		RdTollgateSelector tollgateSelector = new RdTollgateSelector(getConn());

		List<IRow> rowTollgates = tollgateSelector.loadByIds(tollgatePids,
				false, true);

		for (IRow rowTollgate : rowTollgates) {

			RdTollgate tollgate = (RdTollgate) rowTollgate;

			if (tollgate.getNames().size() == 0) {

				this.setCheckResult("", "[RD_TOLLGATE," + tollgate.getPid()
						+ "]", 0);
			}
		}
	}
}
