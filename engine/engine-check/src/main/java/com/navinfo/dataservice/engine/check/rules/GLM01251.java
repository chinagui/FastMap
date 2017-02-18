package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * @ClassName: GLM01251
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description:限速类型为“普通”时,若双向道路一侧的限速来源为“方向限速”，则另一侧的限速来源也必须为“方向限速”，否则报log
 */
public class GLM01251 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01251.class);

	private Set<Integer> check1 = new HashSet<>();

	public GLM01251() {
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
					"SELECT RL.GEOMETRY, '[RD_LINK,' || RL.LINK_PID || ']' TARGET, RL.MESH_ID FROM RD_LINK RL, RD_LINK_SPEEDLIMIT RLS WHERE RL.LINK_PID = RLS.LINK_PID AND RL.LINK_PID = ");

			sb.append(linkPid);

			sb.append(
					" AND RLS.U_RECORD <> 2 AND RLS.SPEED_TYPE = 0 AND ((RLS.FROM_LIMIT_SRC = 5 AND RLS.TO_LIMIT_SRC <> 5) OR (RLS.FROM_LIMIT_SRC <> 5 AND RLS.TO_LIMIT_SRC = 5))");

			logger.info("RdLink后检查GLM01251 check1-> SQL:" + sb.toString());

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
			if (row instanceof RdLinkSpeedlimit) {
				RdLinkSpeedlimit rdLinkSpeedlimit = (RdLinkSpeedlimit) row;
				int fromSpeedLimit = rdLinkSpeedlimit.getFromSpeedLimit();

				int toSpeedLimit = rdLinkSpeedlimit.getToSpeedLimit();
				
				int speedType = rdLinkSpeedlimit.getSpeedType();

				if (rdLinkSpeedlimit.status() != ObjStatus.DELETE) {
					if (rdLinkSpeedlimit.changedFields().containsKey("fromSpeedLimit")) {
						fromSpeedLimit = (int) rdLinkSpeedlimit.changedFields().get("fromSpeedLimit");
					}
					if (rdLinkSpeedlimit.changedFields().containsKey("toSpeedLimit")) {
						toSpeedLimit = (int) rdLinkSpeedlimit.changedFields().get("toSpeedLimit");
					}
					if (rdLinkSpeedlimit.changedFields().containsKey("speedType")) {
						speedType = (int) rdLinkSpeedlimit.changedFields().get("speedType");
					}
					if (speedType == 0) {
						check1.add(rdLinkSpeedlimit.getLinkPid());
					}
					if((fromSpeedLimit == 5 && toSpeedLimit != 5) ||(fromSpeedLimit != 5 && toSpeedLimit == 5))
					{
						check1.add(rdLinkSpeedlimit.getLinkPid());
					}
				}
			}
		}
	}

}
