package com.navinfo.dataservice.engine.check.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * GLM01087
 * 
 * @ClassName: GLM01087
 * @author Zhang Xiaolong
 * @date 2017年2月6日 上午11:30:45
 * @Description: 施工中道路FC必须为5
 */

public class GLM01087 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01087.class);

	/**
	 * 功能等级为非5的link
	 */
	private Map<Integer, RdLink> fucClassLinkMap = new HashMap<>();

	/**
	 * 施工道路linkPid
	 */
	private Set<Integer> limitTypeOf4List = new HashSet<>();

	public GLM01087() {
	}
	
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (Map.Entry<Integer, RdLink> entry : fucClassLinkMap.entrySet()) {
			int linkPid = entry.getKey();
			RdLink rdLink = entry.getValue();
			if(limitTypeOf4List.contains(linkPid))
			{
				logger.debug("检查类型：postCheck， 检查规则：GLM01087， 检查要素：RDLINK(" + rdLink.pid() + "), 触法时机：修改");
				
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
			if (row instanceof RdLink) {
				RdLink rdLink = (RdLink) row;
				int functionClass = rdLink.getFunctionClass();
				if (rdLink.changedFields().containsKey("functionClass")) {
					functionClass = (int) rdLink.changedFields().get("functionClass");
				}
				if (functionClass != 5) {
					fucClassLinkMap.put(rdLink.getPid(), rdLink);
					
					for(IRow limitRow : rdLink.getLimits())
					{
						RdLinkLimit limit = (RdLinkLimit) limitRow;
						
						if(limit.getType() == 4)
						{
							limitTypeOf4List.add(limit.getLinkPid());
							break;
						}
					}
				}
			} else if (row instanceof RdLinkLimit) {
				RdLinkLimit limit = (RdLinkLimit) row;

				int type = limit.getType();

				if (limit.status() == ObjStatus.UPDATE && limit.changedFields().containsKey("type")) {
					type = (int) limit.changedFields().get("type");
				}
				if (limit.status() != ObjStatus.DELETE && type == 4) {
					limitTypeOf4List.add(limit.getLinkPid());
					if(!fucClassLinkMap.containsKey(limit.getLinkPid()))
					{
						RdLink link = (RdLink) selector.loadById(limit.getLinkPid(), true, true);
						
						if(link.getFunctionClass() != 5)
						{
							fucClassLinkMap.put(link.getPid(), link);
						}
					}
				}
				if(limit.status() == ObjStatus.DELETE && type== 4)
				{
					limitTypeOf4List.remove(limit.getLinkPid());
				}
			}
		}
	}
}
