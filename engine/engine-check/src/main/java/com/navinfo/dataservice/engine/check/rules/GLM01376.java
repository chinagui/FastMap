package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * @ClassName: GLM01376
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 1.一条link的同一方向上，只能存在一条类型为单行限制的限制信息，否则报log；
2.一条link上不能有限制类型（单行限制除外）相同的限制信息，否则报log；
 */
public class GLM01376 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01376.class);

	//1.一条link的同一方向上，只能存在一条类型为单行限制的限制信息，否则报log；
	private Set<Integer> check1 = new HashSet<>();
	
	//2.一条link上不能有限制类型（单行限制除外）相同的限制信息，否则报log；
	private Set<Integer> check2 = new HashSet<>();

	public GLM01376() {
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
					"WITH TMP1 AS (SELECT RL.LINK_PID FROM RD_LINK RL, RD_LINK_LIMIT RLL WHERE RL.LINK_PID = RLL.LINK_PID AND RL.LINK_PID = ");

			sb.append(linkPid);

			sb.append(
					" AND RLL.TYPE <> 1 AND RL.U_RECORD <> 2 AND RLL.U_RECORD <> 2 GROUP BY RL.LINK_PID, RLL.TYPE HAVING COUNT(*) > 1) SELECT RL.GEOMETRY, '[RD_LINK,' || RL.LINK_PID || ']' TARGET, RL.MESH_ID FROM RD_LINK RL, TMP1 WHERE RL.LINK_PID = TMP1.LINK_PID ");

			logger.info("RdLink后检查GLM01376 check1-> SQL:" + sb.toString());

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sb.toString());

			if (!resultList.isEmpty()) {
				this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(),
						(int) resultList.get(2));
			}
		}
		
		for (Integer linkPid : check2) {
			StringBuilder sb = new StringBuilder();

			sb.append(
					"WITH TMP2 AS (SELECT RL.LINK_PID FROM RD_LINK RL, RD_LINK_LIMIT RLL WHERE RL.LINK_PID = RLL.LINK_PID AND RL.LINK_PID =");

			sb.append(linkPid);

			sb.append(
					" AND RLL.TYPE = 1 AND RL.U_RECORD <> 2 AND RLL.U_RECORD <> 2 GROUP BY RL.LINK_PID, RLL.LIMIT_DIR HAVING COUNT(*) > 1) SELECT RL.GEOMETRY, '[RD_LINK,' || RL.LINK_PID || ']' TARGET, RL.MESH_ID FROM RD_LINK RL, TMP2 WHERE RL.LINK_PID = TMP2.LINK_PID ");

			logger.info("RdLink后检查GLM01376 check2-> SQL:" + sb.toString());

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
				RdLinkLimit limit = (RdLinkLimit) row;
				int type = limit.getType();
				if (limit.status() != ObjStatus.DELETE && limit.changedFields().containsKey("type")) {
					type = (int) limit.changedFields().get("formOfWay");
				}
				if (type != 1) {
					check1.add(limit.getLinkPid());
				}
				else
				{
					check2.add(limit.getLinkPid());
				}
			} 
		}
	}

}
