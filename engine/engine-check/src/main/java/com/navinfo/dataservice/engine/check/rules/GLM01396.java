package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * @ClassName：GLM01396
 * @author:Feng Haixia
 * @data:2017/03/24
 * @Description:一条link上不能同时制作穿行限制和车辆限制，否则报错
 */
public class GLM01396 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01396.class);
	
	private Set<Integer> LimitLinkPidSet = new HashSet<>();

	public void preCheck(CheckCommand checkCommand) {
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {	
		prepareDateForLinkLimit(checkCommand);
		
		for (Integer linkPid:LimitLinkPidSet) {

			String sqlStr = String.format(
					"SELECT * FROM RD_LINK_LIMIT WHERE TYPE = 3 AND LINK_PID IN (SELECT LINK_PID FROM RD_LINK_LIMIT WHERE TYPE = 2) AND U_RECORD <> 2 AND LINK_PID = %d",
					linkPid);

			logger.info("RdLinkLimit后检查GLM01396 SQL:" + sqlStr);

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sqlStr);

			if (resultList.isEmpty()) {
				continue;
			}

			RdLinkSelector linkSelector = new RdLinkSelector(getConn());
			RdLink link = (RdLink) linkSelector.loadByIdOnlyRdLink(linkPid, false);

			this.setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
		} // for循环
	}
	
	private void prepareDateForLinkLimit(CheckCommand checkCommand) {
		for (IRow row : checkCommand.getGlmList()) {
			if (!(row instanceof RdLinkLimit) || row.status() == ObjStatus.DELETE) {
				continue;
			}

			RdLinkLimit linkLimit = (RdLinkLimit) row;
			int limitType = linkLimit.getType();

			if (linkLimit.changedFields().containsKey("type")) {
				limitType = (int) linkLimit.changedFields().get("type");
			}

			if (limitType == 2 || limitType == 3) {
				LimitLinkPidSet.add(linkLimit.getLinkPid());
			}
		}//for循环
	}
}

