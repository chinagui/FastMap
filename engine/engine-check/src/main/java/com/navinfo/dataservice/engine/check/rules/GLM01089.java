package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * @ClassName: GLM01089
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description:制作了穿行限制或车辆限制道路FC必须为5
 */
public class GLM01089 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01089.class);

	private Set<Integer> check1 = new HashSet<>();

	public GLM01089() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (Integer linkPid : check1) {
			StringBuilder sb = new StringBuilder();

			sb.append(
					"SELECT RL.GEOMETRY, '[RD_LINK,' || RL.LINK_PID || ']' TARGET, RL.MESH_ID FROM RD_LINK RL, RD_LINK_LIMIT RLL WHERE RL.LINK_PID = RLL.LINK_PID AND RL.LINK_PID = ");

			sb.append(linkPid);

			sb.append(
					" AND RLL.U_RECORD <> 2 AND (RLL.TYPE = 2 OR RLL.TYPE = 3) AND RL.FUNCTION_CLASS <> 5");

			logger.info("RdLink后检查GLM01089 check1-> SQL:" + sb.toString());

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sb.toString());

			if (!resultList.isEmpty()) {
				this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(),
						(int) resultList.get(2));
			}
		}
	}

	/**
	 * @param checkCommand
	 * @throws Exception
	 */
	private void prepareData(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row instanceof RdLinkLimit) {
				RdLinkLimit rdLinkLimit = (RdLinkLimit) row;
				int type = rdLinkLimit.getType();
				if (rdLinkLimit.status() != ObjStatus.DELETE) {
					if (rdLinkLimit.changedFields().containsKey("type")) {
						type = (int) rdLinkLimit.changedFields().get("type");
					}
					if (type == 2 || type == 3) {
						check1.add(rdLinkLimit.getLinkPid());
					}
				}
			}
			else if (row instanceof RdLink) {
				RdLink rdLink = (RdLink) row;

				int functionClass = rdLink.getFunctionClass();

				if (rdLink.status() != ObjStatus.DELETE) {
					if (rdLink.changedFields().containsKey("functionClass")) {
						functionClass = (int) rdLink.changedFields().get("functionClass");
					}

					if (functionClass != 5) {
						check1.add(rdLink.getPid());
					}
				}
			}
		}
	}

}
