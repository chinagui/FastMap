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
 * @ClassName: GLM01376
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 10级路上不允许制作了车辆限制或时间段的信息
 */
public class CHECK_LINK10_NOT_HAVE_VERTICALLIMIT_TIMELIMIT extends baseRule {

	private static Logger logger = Logger.getLogger(CHECK_LINK10_NOT_HAVE_VERTICALLIMIT_TIMELIMIT.class);

	private Set<Integer> check1 = new HashSet<>();

	public CHECK_LINK10_NOT_HAVE_VERTICALLIMIT_TIMELIMIT() {
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
					"WITH TMP AS (SELECT RL.LINK_PID FROM RD_LINK RL, RD_LINK_LIMIT RLL WHERE RL.LINK_PID = RLL.LINK_PID AND RL.LINK_PID =");

			sb.append(linkPid);

			sb.append(
					" AND RL.U_RECORD <> 2 AND RLL.U_RECORD <> 2 AND RL.KIND = 10 AND (RLL.TYPE = 2 OR RLL.TIME_DOMAIN IS NOT NULL) GROUP BY RL.LINK_PID) SELECT RL.GEOMETRY, '[RD_LINK,' || RL.LINK_PID || ']' TARGET, RL.MESH_ID FROM RD_LINK RL, TMP T WHERE RL.LINK_PID = T.LINK_PID ");

			logger.info("RdLink后检查GLM01376 check1-> SQL:" + sb.toString());

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
				String timeDomain = limit.getTimeDomain();
				if (limit.status() != ObjStatus.DELETE && limit.changedFields().containsKey("type")) {
					type = (int) limit.changedFields().get("formOfWay");
				}
				if (limit.status() != ObjStatus.DELETE && limit.changedFields().containsKey("timeDomain")) {
					timeDomain = (String) limit.changedFields().get("timeDomain");
				}
				if (type == 2 || timeDomain != null) {
					check1.add(limit.getLinkPid());
				}
			} 
			else if(row instanceof RdLink)
			{
				RdLink rdLink = (RdLink) row;
				
				int kind = rdLink.getKind();
				if (rdLink.status() != ObjStatus.DELETE) {
					if(rdLink.changedFields().containsKey("kind"))
					{
						kind = (int) rdLink.changedFields().get("kind");
					}
					if(kind == 10)
					{
						check1.add(rdLink.getPid());
					}
				}
				
			}
		}
	}

}
