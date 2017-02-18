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
 * GLM01307 单向道路的限速类型为“普通”时，限速来源不能是“方向限速”，否则报log
 * 
 * @ClassName: GLM01307
 * @author Zhang Xiaolong
 * @date 2017年2月6日 上午11:30:45
 * @Description: TODO
 */

public class GLM01307 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01307.class);

	/**
	 * 需要检查的对象
	 */
	private Set<Integer> checkLinkPids = new HashSet<>();

	public GLM01307() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (int linkPid : checkLinkPids) {
			StringBuilder sb = new StringBuilder();

			sb.append(
					"SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID FROM RD_LINK_SPEEDLIMIT S, RD_LINK L WHERE S.LINK_PID = L.LINK_PID AND L.DIRECT = 2 AND S.FROM_LIMIT_SRC = 5 AND S.SPEED_TYPE = 0 AND L.LINK_PID =");

			sb.append(linkPid);

			sb.append(
					" UNION ALL SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID FROM RD_LINK_SPEEDLIMIT S, RD_LINK L WHERE S.LINK_PID = L.LINK_PID AND L.DIRECT = 3 AND S.TO_LIMIT_SRC = 5 AND S.SPEED_TYPE = 0 AND L.LINK_PID =");
			sb.append(linkPid);
			
			logger.info("RdLink后检查GLM01307 SQL:" + sb.toString());
			
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
			if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
				RdLink rdLink = (RdLink) row;
				int direct = rdLink.getDirect();
				if (rdLink.changedFields().containsKey("direct")) {
					direct = (int) rdLink.changedFields().get("direct");
				}
				if (direct != 1) {
					checkLinkPids.add(rdLink.getPid());
				}
			} else if (row instanceof RdLinkSpeedlimit) {
				RdLinkSpeedlimit speedLimit = (RdLinkSpeedlimit) row;

				if(speedLimit.getSpeedType() == 0)
				{
					int fromSpeedLimitSrc = speedLimit.getFromLimitSrc();
					int toSpeedLimitSrc = speedLimit.getToLimitSrc();
					if(speedLimit.status() != ObjStatus.DELETE)
					{
						if(speedLimit.changedFields().containsKey("fromLimitSrc"))
						{
							fromSpeedLimitSrc = (int) speedLimit.changedFields().get("fromLimitSrc");
						}
						if(speedLimit.changedFields().containsKey("toSpeedLimitSrc"))
						{
							toSpeedLimitSrc = (int) speedLimit.changedFields().get("toSpeedLimitSrc");
						}
						
						if(fromSpeedLimitSrc == 5 || toSpeedLimitSrc == 5)
						{
							checkLinkPids.add(speedLimit.getLinkPid());
						}
					}
					if(speedLimit.status() == ObjStatus.DELETE)
					{
						checkLinkPids.remove(speedLimit.getLinkPid());
					}
				}
			}
		}
	}
}
