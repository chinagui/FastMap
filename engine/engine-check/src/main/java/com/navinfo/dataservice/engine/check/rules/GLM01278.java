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
 * @ClassName: GLM01411
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 单方向的路只能制作单方向的UsageFee，并且UsageFee的通行方向应与Link的通行方向一致，否则报log
 */
public class GLM01278 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01278.class);

	private Set<Integer> check1 = new HashSet<>();
	
	public GLM01278() {
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
					" AND RL.DIRECT <> 1 AND RLL.TYPE = 7 AND RL.U_RECORD <> 2 AND RLL.U_RECORD <> 2 AND RLL.LIMIT_DIR <> RL.DIRECT   ");

			logger.info("RdLink后检查GLM01411 check1-> SQL:" + sb.toString());

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
				if (limit.status() != ObjStatus.DELETE) {
					if(limit.changedFields().containsKey("type"))
					{
						type = (int) limit.changedFields().get("type");
					}
					//Usage Fee Required
					if (type == 6) {
						check1.add(limit.getLinkPid());
					}
					if(limit.changedFields().containsKey("limitDir"))
					{
						check1.add(limit.getLinkPid());
					}
				}
				else if(row instanceof RdLink)
				{
					RdLink rdLink = (RdLink) row;
					
					int direct = rdLink.getDirect();
					if (rdLink.status() != ObjStatus.DELETE) {
						if(rdLink.changedFields().containsKey("direct"))
						{
							direct = (int) rdLink.changedFields().get("direct");
						}
						//单方向道路
						if(direct != 1)
						{
							check1.add(rdLink.getPid());
						}
					}
					
				}
			} 
		}
	}

}
