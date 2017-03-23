package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/*
 * @ClassName：GLM01284
 * @author:Feng Haixia
 * @data:2017/03/23
 * @Description:限制信息类型为“单行限制”，该限制信息的“时间段”不能为空 
 */
public class GLM01284 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01284.class);
	Set<Integer> limitLinkPidSet = new HashSet<>();

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareDateForLinkForm(checkCommand);

		for (Integer linkPid : limitLinkPidSet) {
			String sqlStr = String.format(
					"SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID FROM RD_LINK_LIMIT LM, RD_LINK L "
							+ "WHERE LM.TYPE = 2 AND LM.TIME_DOMAIN IS NULL AND L.LINK_PID = LM.LINK_PID AND L.U_RECORD<> 2 AND LM.U_RECORD <> 2 AND L.LINK_PID = %d",
					linkPid);

			logger.info("RdLinkLimit后检查GLM01284 SQL:" + sqlStr);

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sqlStr);

			if (resultList.isEmpty()) {
				continue;
			}
			this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int) resultList.get(2));
		} // for循环
	}

	private void prepareDateForLinkForm(CheckCommand checkCommand) {
		for (IRow row : checkCommand.getGlmList()) {
			if (!(row instanceof RdLinkLimit) || row.status() != ObjStatus.DELETE) {
				continue;
			}

			RdLinkLimit rdlinkLimit = (RdLinkLimit) row;
			int limitType = rdlinkLimit.getType();
			String timeDomain = rdlinkLimit.getTimeDomain();

			// 触发时机为“限制类型编辑”or“车辆类型编辑（link限制表）”
			if (rdlinkLimit.changedFields().containsKey("type")) {
				limitType = (int) rdlinkLimit.changedFields().get("type");
			}
			if (rdlinkLimit.changedFields().containsKey("timeDomain")) {
				timeDomain = rdlinkLimit.changedFields().get("timeDomain").toString();
			}

			if (limitType == 2 || timeDomain.isEmpty()) {
				limitLinkPidSet.add(rdlinkLimit.getLinkPid());
			}
		} // for循环
	}
}
