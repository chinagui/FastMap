package com.navinfo.dataservice.engine.check.rules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/*
 * @ClassName：PERMIT_MODIFICATE_CROSSINNER_POINT
 * @author:Feng Haixia
 * @data:2017/03/28
 * @Description:对组成路口的node挂接的link线进行编辑操作时，不能分离组成路口的node点；
 */
public class PERMIT_MODIFICATE_CROSSINNER_POINT extends baseRule {

	Set<Integer> changeGeoNodePidSet = new HashSet<Integer>();

	public void preCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);

		for (Integer nodePid : changeGeoNodePidSet) {
			String sql = String.format(
					"SELECT A.* FROM RD_CROSS A,RD_CROSS_NODE B WHERE A.PID=B.PID AND B.NODE_PID = %d", nodePid);

			RdCrossSelector crossSelector = new RdCrossSelector(this.getConn());
			List<RdCross> cross = crossSelector.loadCrossBySql(sql, false);

			if (cross.isEmpty()) {
				continue;
			}
			this.setCheckResult("", "", 0);
		} // for循环
	}

	public void postCheck(CheckCommand checkCommand) throws Exception {
	}

	/*
	 * @Function:更新node点几何的nodePid集合
	 */
	private void prepareData(CheckCommand checkCommand) {
		for (IRow row : checkCommand.getGlmList()) {
			if (!(row instanceof RdLink) || row.status() == ObjStatus.DELETE) {
				continue;
			}

			RdLink rdLink=(RdLink)row;
			
			if(rdLink.changedFields().containsKey("sNodePid")){
				changeGeoNodePidSet.add(rdLink.getsNodePid());
			}
			
			if(rdLink.changedFields().containsKey("eNodePid")){
				changeGeoNodePidSet.add(rdLink.geteNodePid());
			}
		}//for循环
	}
}
