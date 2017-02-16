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
 * @ClassName: GLM01260
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 限速类型为“普通”时，FC=4的道路，限速等级必须为2-8
 */
public class GLM01260 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01260.class);

	private Set<Integer> resultLinkPidSet = new HashSet<>();

	public GLM01260() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (Integer linkPid : resultLinkPidSet) {
			StringBuilder sb = new StringBuilder();

			sb.append(
					"SELECT R.GEOMETRY, '[RD_LINK,' || R.LINK_PID || ']' TARGET, R.MESH_ID FROM RD_LINK R,RD_LINK_SPEEDLIMIT LS WHERE R.LINK_PID =");

			sb.append(linkPid);
			
			sb.append(" AND R.LINK_PID = LS.LINK_PID AND R.FUNCTION_CLASS = 4 AND LS.SPEED_TYPE = 0 AND LS.SPEED_CLASS <= 1 AND R.U_RECORD <> 2 AND LS.U_RECORD <> 2");

			logger.info("RdLink后检查GLM01260 SQL:" + sb.toString());

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
				int functionClass = rdLink.getFunctionClass();
				if (rdLink.status() != ObjStatus.DELETE && rdLink.changedFields().containsKey("direct")) {
					functionClass = (int) rdLink.changedFields().get("functionClass");
				}
				if (functionClass == 4) {
					resultLinkPidSet.add(rdLink.getPid());
				}
			} else if (row instanceof RdLinkSpeedlimit) {
				RdLinkSpeedlimit rdLinkSpeedlimit = (RdLinkSpeedlimit) row;

				int speedType = rdLinkSpeedlimit.getSpeedType();
				
				int speedClass = rdLinkSpeedlimit.getSpeedClass();
				
				if(rdLinkSpeedlimit.status() != ObjStatus.DELETE)
				{
					if(rdLinkSpeedlimit.changedFields().containsKey("speedType"))
					{
						speedType = (int) rdLinkSpeedlimit.changedFields().get("limitDir");
					}
					if(rdLinkSpeedlimit.changedFields().containsKey("speedClass"))
					{
						speedClass = (int) rdLinkSpeedlimit.changedFields().get("speedClass");
					}
					if(speedType == 0)
					{
						resultLinkPidSet.add(rdLinkSpeedlimit.getLinkPid());
					}
					if(speedClass <=1)
					{
						resultLinkPidSet.add(rdLinkSpeedlimit.getLinkPid());
					}
				}
			}
		}
	}

}
