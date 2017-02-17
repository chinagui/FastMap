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
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * @ClassName: GLM01405
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description:如果一个点只挂接了一条非未供用的link，则该link不能为单方向
 */
public class GLM01405 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01405.class);

	private Set<Integer> check1 = new HashSet<>();

	public GLM01405() {
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
					"WITH TMP AS (SELECT RL.S_NODE_PID S_NODE_PID, RL.E_NODE_PID E_NODE_PID FROM RD_LINK RL WHERE RL.LINK_PID = ");

			sb.append(linkPid);

			sb.append(
					" AND RL.APP_INFO <> 3 AND RL.DIRECT <> 1 ) SELECT RL.GEOMETRY, '[RD_LINK,' || RL.LINK_PID || ']', RL.MESH_ID FROM RD_LINK RL, TMP T WHERE RL.LINK_PID = ");
			
			sb.append(linkPid);
			
			sb.append(" AND (NOT EXISTS (SELECT 1 FROM RD_LINK WHERE U_RECORD <> 2 AND (S_NODE_PID = T.S_NODE_PID OR E_NODE_PID = T.S_NODE_PID) AND LINK_PID <> ");
			
			sb.append(linkPid);
			
			sb.append(" ) OR NOT EXISTS (SELECT 1 FROM RD_LINK WHERE U_RECORD <> 2 AND (S_NODE_PID = T.E_NODE_PID OR E_NODE_PID = T.E_NODE_PID) AND LINK_PID <> ");
			
			sb.append(linkPid);
			
			sb.append("))");
			logger.info("RdLink后检查GLM01405 check1-> SQL:" + sb.toString());

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
			if (row instanceof RdLink) {
				RdLink rdLink = (RdLink) row;
				int direct = rdLink.getKind();

				int appInfo = rdLink.getAppInfo();

				if (rdLink.status() != ObjStatus.DELETE) {
					if (rdLink.changedFields().containsKey("direct")) {
						direct = (int) rdLink.changedFields().get("direct");
					}
					if (rdLink.changedFields().containsKey("appInfo")) {
						appInfo = (int) rdLink.changedFields().get("appInfo");
					}
					if (direct != 1) {
						check1.add(rdLink.getPid());
					}
					if (appInfo != 3) {
						check1.add(rdLink.getPid());
					}
				}
			}
		}
	}

}
