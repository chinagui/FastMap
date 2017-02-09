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
 * GLM01437 限速类型不为“普通”时，同一根LINK的顺向限速和逆向限速值不能同时为0，否则报log
 * 
 * @ClassName: GLM01437
 * @author Zhang Xiaolong
 * @date 2017年2月6日 上午11:30:45
 * @Description:限速类型不为“普通”时，同一根LINK的顺向限速和逆向限速值不能同时为0，否则报log
 */

public class GLM01437 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01437.class);

	private Set<Integer> checkLinkSet = new HashSet<>();

	public GLM01437() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (int linkPid : checkLinkSet) {
			logger.info("检查类型：postCheck， 检查规则：GLM01437， 检查要素：RDLINK(" + linkPid + ")");
			StringBuilder sb = new StringBuilder();

			sb.append(
					"SELECT R.GEOMETRY, '[RD_LINK,' || R.LINK_PID || ']' TARGET, R.MESH_ID FROM RD_LINK R,RD_LINK_SPEEDLIMIT LS WHERE R.LINK_PID =");
			sb.append(linkPid);
			sb.append(
					" AND R.LINK_PID = LS.LINK_PID AND LS.FROM_SPEED_LIMIT = 0 AND LS.TO_SPEED_LIMIT = 0 AND LS.SPEED_TYPE <> 0 AND R.U_RECORD <> 2 AND LS.U_RECORD <> 2  ");

			log.info("RdLink后检查GLM01437 SQL:" + sb.toString());

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

				if (rdLinkSpeedlimit.status() != ObjStatus.DELETE && rdLinkSpeedlimit.getSpeedType() != 0) {
					if (rdLinkSpeedlimit.changedFields().containsKey("fromSpeedLimit")) {
						fromSpeedLimit = (int) rdLinkSpeedlimit.changedFields().get("fromSpeedLimit");
					}

					if (rdLinkSpeedlimit.changedFields().containsKey("toSpeedLimit")) {
						toSpeedLimit = (int) rdLinkSpeedlimit.changedFields().get("toSpeedLimit");
					}
					
					if (fromSpeedLimit == 0 &&  toSpeedLimit == 0) {
						checkLinkSet.add(rdLinkSpeedlimit.getLinkPid());
					}
				}
			}
		}
	}
}
