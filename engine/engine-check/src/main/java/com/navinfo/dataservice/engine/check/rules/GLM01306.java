package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
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
 * @ClassName：GLM01306
 * @author:Feng Haixia
 * @data:2017/03/22
 * @Description: 非公交车专用道、非10级路的UsageFee若设置车辆类型，则只能同时是“客车（小汽车）”、
 *               “配送卡车”、“运输卡车”、“急救车”、“出租车”、“公交车”，否则报log
 */
public class GLM01306 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01306.class);

	private Set<Integer> limitResultPidSet = new HashSet<>();

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareRDLinkLimitData(checkCommand);

		for (Integer linkPid : this.limitResultPidSet) {
			String sqlStr = String.format(
					"SELECT * FROM RD_LINK_LIMIT LM WHERE (LM.TYPE=6 AND LM.VEHICLE !=903) AND LM.U_RECORD <> 2 AND LM.LINK_PID = %d ",
					linkPid);

			logger.info("RdLinkLimit后检查GLM01306 SQL:" + sqlStr);

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> tempResultList = new ArrayList<Object>();
			tempResultList = getObj.exeSelect(this.getConn(), sqlStr);

			if (tempResultList.isEmpty()) {
				continue;
			}

			RdLinkSelector linkSelector = new RdLinkSelector(getConn());
			RdLink link = (RdLink) linkSelector.loadByIdOnlyRdLink(linkPid, false);
			boolean containBusLane = false;

			// 道路属性是否为“公交专用车道”
			for (IRow form : link.getForms()) {
				RdLinkForm formWay = (RdLinkForm) form;

				if (formWay.getFormOfWay() != 22)
					continue;
				containBusLane = true;
				break;
			}

			if (link.getKind() != 15 && containBusLane == false) {
				this.setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
			}
		}
	}

	/*
	 * 准备检查数据
	 * 
	 * @param checkCommand
	 * 
	 * @result 已修改限制信息数据集
	 */
	private void prepareRDLinkLimitData(CheckCommand checkCommand) {
		for (IRow row : checkCommand.getGlmList()) {
			if (!(row instanceof RdLinkLimit) || row.status() == ObjStatus.DELETE) {
				continue;
			}

			RdLinkLimit rdlinkLimit = (RdLinkLimit) row;
			int limitType = rdlinkLimit.getType();
			long limitVehicle = rdlinkLimit.getVehicle();

			if (rdlinkLimit.changedFields().containsKey("type")) {
				limitType = (int) rdlinkLimit.changedFields().get("type");
			}

			if (rdlinkLimit.changedFields().containsKey("vehicle")) {
				limitVehicle = rdlinkLimit.getVehicle();
			}

			if (limitType == 6 || limitVehicle != 903) {
				limitResultPidSet.add(rdlinkLimit.getLinkPid());
			}
		}//for循环
	}
}
