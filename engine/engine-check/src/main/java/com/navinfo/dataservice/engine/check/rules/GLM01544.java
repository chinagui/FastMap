package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkNameSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/*
 * @ClassName：GLM01399
 * @author:Feng Haixia
 * @data:2017/03/24
 * @Description:同一根link上不同的道路名称中不允许有相同的NAME值
 */
public class GLM01544 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01544.class);

	private Set<Integer> nameLinkPidSet = new HashSet<>();

	@Override
	public void preCheck(CheckCommand checkCommand) {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareChangeDataSet(checkCommand);

		RdLinkNameSelector rdName = new RdLinkNameSelector(this.getConn());
		Map<Integer, List<RdLinkName>> nameList = rdName.loadNameByLinkPids(nameLinkPidSet);

		for (Map.Entry<Integer, List<RdLinkName>> entry : nameList.entrySet()) {
			List<RdLinkName> names = entry.getValue();

			boolean flag = compareSameName(names);
			if (flag == true)
				continue;

			RdLinkSelector linkSelector = new RdLinkSelector(getConn());
			RdLink link = (RdLink) linkSelector.loadByIdOnlyRdLink(entry.getKey(), false);
			this.setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
		}
	}

	/**
	 * 比对同一link上的名称是否相同
	 * 
	 * @param names
	 * @return
	 */
	private boolean compareSameName(List<RdLinkName> names) {
		boolean flag = true;
		for (int i = 0; i < names.size() - 1; i++) {
			for (int j = i + 1; j < names.size(); j++) {
				if (names.indexOf(i) == names.indexOf(j)) {
					flag = false;
					break;
				}
			}
			if (flag = false)
				break;
		}

		return flag;
	}

	private void prepareChangeDataSet(CheckCommand checkCommand) {
		for (IRow row : checkCommand.getGlmList()) {
			if (!(row instanceof RdLinkName) || row.status() == ObjStatus.DELETE) {
				continue;
			}

			RdLinkName rdlinkName = (RdLinkName) row;

			nameLinkPidSet.add(rdlinkName.getLinkPid());
		}
	}
}
