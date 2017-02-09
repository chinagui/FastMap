package com.navinfo.dataservice.engine.check.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

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
	 * 方向为单方向的link
	 */
	private Map<Integer, RdLink> linkDirectMap = new HashMap<>();

	/**
	 * 限速类型为普通限速，限速来源为“方向限速”
	 */
	private Set<Integer> speedLimitLinkSet = new HashSet<>();

	public GLM01307() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (Map.Entry<Integer, RdLink> entry : linkDirectMap.entrySet()) {
			int linkPid = entry.getKey();
			RdLink rdLink = entry.getValue();
			if(speedLimitLinkSet.contains(linkPid))
			{
				logger.debug("检查类型：postCheck， 检查规则：GLM01307， 检查要素：RDLINK(" + rdLink.pid() + ")");
				
				this.setCheckResult(rdLink.getGeometry(), "[RD_LINK," + rdLink.getPid() + "]", rdLink.getMeshId());
			}
		}
	}

	/**
	 * @param checkCommand
	 * @throws Exception 
	 */
	private void prepareData(CheckCommand checkCommand) throws Exception {
		AbstractSelector selector = new AbstractSelector(RdLink.class, getConn());
		for (IRow row : checkCommand.getGlmList()) {
			if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
				RdLink rdLink = (RdLink) row;
				int direct = rdLink.getDirect();
				if (rdLink.changedFields().containsKey("direct")) {
					direct = (int) rdLink.changedFields().get("direct");
				}
				if (direct != 1) {
					
					linkDirectMap.put(rdLink.getPid(), rdLink);
					
					List<IRow> limits = rdLink.getSpeedlimits();
					
					for(IRow limitRow : limits)
					{
						RdLinkSpeedlimit speedlimit = (RdLinkSpeedlimit) limitRow;
						
						if(speedlimit.getSpeedType() ==0 && (speedlimit.getFromLimitSrc() == 5 || speedlimit.getToSpeedLimit() == 5))
						{
							speedLimitLinkSet.add(rdLink.getPid());
						}
						break;
					}
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
							speedLimitLinkSet.add(speedLimit.getLinkPid());
							if(!linkDirectMap.containsKey(speedLimit.getLinkPid()))
							{
								RdLink link = (RdLink) selector.loadById(speedLimit.getLinkPid(), true, true);
								
								if(link.getDirect() != 1)
								{
									linkDirectMap.put(link.getPid(), link);
								}
							}
						}
					}
					if(speedLimit.status() == ObjStatus.DELETE)
					{
						speedLimitLinkSet.remove(speedLimit.getLinkPid());
					}
				}
			}
		}
	}
}
