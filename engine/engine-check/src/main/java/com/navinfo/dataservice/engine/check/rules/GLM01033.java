
package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/*
 * @ClassName：GLM01033
 * @author:Feng Haixia
 * @data:2017/03/28
 * @Description:如果道路上的供用信息不是“可以通行”（APP_INFO≠1），则报log
 */
public class GLM01033 extends baseRule {

	Set<Integer> linkPidSet = new HashSet<Integer>();

	@Override
	public void preCheck(CheckCommand checkCommand) {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			prepareData(row);
		}

		// 普通道路判断供用信息
		checkOrdinaryRoad();
	}

	/**
	 * 道路判断供用信息(不为1报错)
	 */
	private void checkOrdinaryRoad() throws Exception {
		for (Integer linkPid : linkPidSet) {
			String sqlOfHighway = String
					.format("SELECT L.GEOMETRY,'[RD_LINK,' || L.LINK_PID || ']' TARGET,L.MESH_ID FROM RD_LINK L "
							+ "WHERE L.LINK_PID = %d AND L.APP_INFO <> 1", linkPid);

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sqlOfHighway);

			if (resultList.isEmpty()) {
				continue;
			}
			this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int) resultList.get(2));
		}
	}

	/**
	 * @Function:准备数据，更新的linkPid
	 */
	private void prepareData(IRow row) {
		if (!(row instanceof RdLink) || row.status() == ObjStatus.DELETE) {
			return;
		}

		RdLink rdLink = (RdLink) row;

		if (rdLink.changedFields().containsKey("appInfo") || rdLink.changedFields().containsKey("kind")) {
			linkPidSet.add(rdLink.getPid());
		}
	}
}
