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
 * @ClassName：GLM01399
 * @author:Feng Haixia
 * @data:2017/03/22
 * @Description:限制信息类型为3穿行限制时，车辆类型必须为空，否则报错;
 *              限制信息类型为2车辆限制时，车辆类型一定非空，否则报错;
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
					"SELECT * FROM RD_LINK_LIMIT LM WHERE ((LM.TYPE=6 AND LM.VEHICLE !=903) AND LM.U_RECORD <> 2 AND L.LINK_PID = {0} ",
					linkPid);

			logger.info("RdLinkLimit后检查GLM01306 SQL:" + sqlStr);

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> tempResultList = new ArrayList<Object>();
			List<Object> resultList = new ArrayList<Object>();
			tempResultList = getObj.exeSelect(this.getConn(), sqlStr);

			if (tempResultList.isEmpty()) {
				continue;
			}

			RdLinkSelector linkSelector = new RdLinkSelector(getConn());
			RdLink link = (RdLink) linkSelector.loadByIdOnlyRdLink(linkPid, false);
			boolean containBusLane = false;

			//道路属性是否为“公交专用车道”
			for (IRow form : link.getForms()) {
				RdLinkForm formWay = (RdLinkForm) form;

				if (formWay.getFormOfWay() != 22)
					continue;
				containBusLane = true;
				break;
			}

			if (link.getKind() != 15 && containBusLane == false) {
				this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(),
						(int) resultList.get(2));
			}
		}
	}

	/*
	 * 准备检查数据
	 * @param checkCommand
	 * @result 已修改限制信息数据集
	 */
	private void prepareRDLinkLimitData(CheckCommand checkCommand) {
		for (IRow row : checkCommand.getGlmList()) {
			if (!(row instanceof RdLinkLimit) || row.status() == ObjStatus.DELETE) {
				continue;
			}

			RdLinkLimit rdlinkLimit = (RdLinkLimit) row;

			if (!rdlinkLimit.changedFields().containsKey("type")
					|| rdlinkLimit.changedFields().containsKey("vehicle")) {
				continue;
			}

			limitResultPidSet.add(rdlinkLimit.getLinkPid());
		}
	}
}
