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
 * @ClassName: GLM01064
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 10级路link没有制作“详细开发”，但两端挂接的link中至少有一根为“详细开发“，则报LOG
 */
public class GLM01064 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01064.class);

	//1.一条link的同一方向上，只能存在一条类型为单行限制的限制信息，否则报log；
	private Set<Integer> check1 = new HashSet<>();

	public GLM01064() {
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
					"WITH TMP AS (SELECT RL2.LINK_PID FROM RD_LINK RL1, RD_LINK RL2 WHERE (RL1.S_NODE_PID = RL2.S_NODE_PID OR RL1.S_NODE_PID = RL2.E_NODE_PID OR RL1.E_NODE_PID = RL2.S_NODE_PID OR RL1.E_NODE_PID = RL2.E_NODE_PID) AND RL1.LINK_PID =");

			sb.append(linkPid);

			sb.append(
					" AND RL2.LINK_PID <> ");
			
			sb.append(linkPid);
			
			sb.append(
					" AND RL2.DEVELOP_STATE = 1 AND RL1.U_RECORD <> 2 AND RL2.U_RECORD <> 2) SELECT RL.GEOMETRY,'[RD_LINK,' || RL.LINK_PID || ']' TARGET, RL.MESH_ID FROM RD_LINK RL WHERE RL.KIND = 10 AND RL.DEVELOP_STATE <> 1 AND RL.LINK_PID =");
			
			sb.append(linkPid);
			
			sb.append(" AND EXISTS (SELECT * FROM TMP) ");
			
			logger.info("RdLink后检查GLM01376 SQL:" + sb.toString());

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
				int developState = rdLink.getDevelopState();
				int kind = rdLink.getKind();
				if (rdLink.status() != ObjStatus.DELETE && rdLink.changedFields().containsKey("developState")) {
					developState = (int) rdLink.changedFields().get("developState");
				}
				if (rdLink.status() != ObjStatus.DELETE && rdLink.changedFields().containsKey("kind")) {
					kind = (int) rdLink.changedFields().get("kind");
				}
				if (developState != 1) {
					check1.add(rdLink.pid());
				}
				if(kind == 10)
				{
					check1.add(rdLink.pid());
				}
			} 
		}
	}

}
