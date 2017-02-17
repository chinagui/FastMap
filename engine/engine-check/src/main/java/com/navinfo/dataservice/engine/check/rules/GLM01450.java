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
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * @ClassName: GLM01450
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description:限速类型为“普通”时，高速、城高的可通行方向上的速度限制不能为0，否则报LOG
 */
public class GLM01450 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01450.class);

	private Set<Integer> check1 = new HashSet<>();

	public GLM01450() {
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
					" AND RLS.U_RECORD <> 2 AND (RL.KIND = 1 OR RL.KIND = 2) AND ((RL.DIRECT = 1 AND (RLS.FROM_SPEED_LIMIT = 0 OR RLS.TO_SPEED_LIMIT = 0)) OR (RL.DIRECT = 2 AND RLS.FROM_SPEED_LIMIT = 0) OR (RL.DIRECT = 3 AND RLS.TO_SPEED_LIMIT = 0))");

			logger.info("RdLink后检查GLM01450 check1-> SQL:" + sb.toString());

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

				if (rdLinkSpeedlimit.status() != ObjStatus.DELETE) {
					if (rdLinkSpeedlimit.changedFields().containsKey("fromSpeedLimit")) {
						fromSpeedLimit = (int) rdLinkSpeedlimit.changedFields().get("fromSpeedLimit");
					}
					if (rdLinkSpeedlimit.changedFields().containsKey("toSpeedLimit")) {
						toSpeedLimit = (int) rdLinkSpeedlimit.changedFields().get("toSpeedLimit");
					}

					if (fromSpeedLimit == 0 || toSpeedLimit == 0) {
						check1.add(rdLinkSpeedlimit.getLinkPid());
					}
				}
			} else if (row instanceof RdLink) {
				RdLink rdLink = (RdLink) row;

				int kind = rdLink.getKind();

				int direct = rdLink.getDirect();

				if (rdLink.status() != ObjStatus.DELETE) {
					if (rdLink.changedFields().containsKey("kind")) {
						kind = (int) rdLink.changedFields().get("kind");
					}

					if (rdLink.changedFields().containsKey("direct")) {
						direct = (int) rdLink.changedFields().get("direct");
					}
					if (kind == 1 || kind == 2) {
						check1.add(rdLink.getPid());
					}
					if (direct != 0) {
						check1.add(rdLink.getPid());
					}
				}
			}
		}
	}

}
