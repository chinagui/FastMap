
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
 * @Description:1、如果普通路上（3级含3级以下）的供用信息不是“可以通行”（APP_INFO≠1），则报log1；
 *              2、如果高速城高道路上的供用信息不是“可以通行”或“未供用”（APP_INFO≠1、3），则报log2
 */
public class GLM01033 extends baseRule {

	Set<Integer> linkPidSetHighWay = new HashSet<Integer>();
	Set<Integer> linkPidSetNotHighWay = new HashSet<Integer>();

	@Override
	public void preCheck(CheckCommand checkCommand) {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			prepareData(row);
		}

		// 高速道路判断供用信息
		checkHighwayRoad();

		// 普通道路判断供用信息
		checkOrdinaryRoad();
	}

	/*
	 * 高速道路判断供用信息(不为1,3报错)
	 */
	private void checkHighwayRoad() throws Exception
	{
		for (Integer linkPid : linkPidSetHighWay) {
			String sqlOfHighway = String
					.format("SELECT L.GEOMETRY,'[RD_LINK,'+L.LINK_PID+']' TARGET,L.MESH_ID FROM RD_LINK L "
							+ "WHERE L.LINK_PID = %d AND L.APP_INFO NOT IN (1,3)", linkPid);

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sqlOfHighway);

			if (resultList.isEmpty()) {
				continue;
			}
			this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int) resultList.get(2),
					"高速城高道路供用信息类型错误！");
		}
	}
	
	/*
	 * 普通道路判断供用信息(不为1报错)
	 */
	private void checkOrdinaryRoad() throws Exception
	{
		for (Integer linkPid : linkPidSetNotHighWay) {
			String sqlOfHighway = String
					.format("SELECT L.GEOMETRY,'[RD_LINK,'+L.LINK_PID+']' TARGET,L.MESH_ID FROM RD_LINK L "
							+ "WHERE L.LINK_PID = %d AND L.APP_INFO <> 1", linkPid);

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sqlOfHighway);

			if (resultList.isEmpty()) {
				continue;
			}
			this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int) resultList.get(2),
					"普通道路供用信息类型错误！");
		}
	}
	
	/*
	 * @Function:准备数据，区分高速路和普通路
	 */
	private void prepareData(IRow row) {
		if (!(row instanceof RdLink) || row.status() == ObjStatus.DELETE) {
			return;
		}

		RdLink rdLink = (RdLink) row;
		int kind = rdLink.getKind();

		if (rdLink.changedFields().containsKey("kind")) {
			kind = (Integer) rdLink.changedFields().get("kind");
		}

		if (kind == 1 || kind == 2) {
			linkPidSetHighWay.add(rdLink.getPid());
		} else {
			linkPidSetNotHighWay.add(rdLink.getPid());
		}
	}
}
