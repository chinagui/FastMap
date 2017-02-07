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
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * GLM01514 窄道路的车道数等级只能为1
 * 
 * @ClassName: GLM01514
 * @author Zhang Xiaolong
 * @date 2017年2月6日 上午11:30:45
 * @Description: TODO
 */

public class GLM01514 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01514.class);

	/**
	 * link车道树等级不为1的link
	 */
	private Map<Integer, RdLink> linkLaneClassMap = new HashMap<>();

	/**
	 * 形态为窄道路的linkPid
	 */
	private Set<Integer> linkFormOf43List = new HashSet<>();

	public GLM01514() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (Integer linkPid : linkFormOf43List) {
			if(linkLaneClassMap.containsKey(linkPid))
			{
				RdLink rdLink = linkLaneClassMap.get(linkPid);
				
				logger.debug("检查类型：postCheck， 检查规则：GLM01008， 检查要素：RDLINK(" + rdLink.pid() + "), 触法时机：修改");
				
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
				int laneClass = rdLink.getLaneClass();
				if (rdLink.changedFields().containsKey("laneClass")) {
					laneClass = (int) rdLink.changedFields().get("laneClass");
				}
				if (laneClass != 1) {
					linkLaneClassMap.put(rdLink.getPid(), rdLink);
					
					for(IRow formRow : rdLink.getForms())
					{
						RdLinkForm form = (RdLinkForm) formRow;
						
						if(form.getFormOfWay() == 43)
						{
							linkFormOf43List.add(form.getLinkPid());
							break;
						}
					}
				}
			} else if (row instanceof RdLinkForm) {
				RdLinkForm form = (RdLinkForm) row;

				int formOfWay = form.getFormOfWay();

				if (form.changedFields().containsKey("formOfWay")) {
					formOfWay = (int) form.changedFields().get("formOfWay");
				}
				if (form.status() != ObjStatus.DELETE && formOfWay == 43) {
					linkFormOf43List.add(form.getLinkPid());
					if(!linkLaneClassMap.containsKey(form.getLinkPid()))
					{
						RdLink link = (RdLink) selector.loadById(form.getLinkPid(), true, true);
						
						if(link.getLaneClass() != 1)
						{
							linkLaneClassMap.put(link.getPid(), link);
						}
					}
				}
				if(form.status() == ObjStatus.DELETE && formOfWay== 43)
				{
					linkFormOf43List.remove(form.getLinkPid());
				}
			}
		}
	}
}
