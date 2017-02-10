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
 * GLM01105 步行街属性的总车道数必须为1
 * 
 * @ClassName: GLM01105
 * @author Zhang Xiaolong
 * @date 2017年2月6日 上午11:30:45
 * @Description: 步行街属性的总车道数必须为1
 */

public class GLM01105 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01105.class);

	/**
	 * 车道数变化的link
	 */
	private Map<Integer, RdLink> linkLaneNumMap = new HashMap<>();

	/**
	 * 形态为步行街道路的link
	 */
	private Set<Integer> linkFormOf20List = new HashSet<>();

	public GLM01105() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (Map.Entry<Integer, RdLink> entry : linkLaneNumMap.entrySet()) {
			int linkPid = entry.getKey();
			RdLink rdLink = entry.getValue();
			if(linkFormOf20List.contains(linkPid))
			{
				logger.debug("检查类型：postCheck， 检查规则：GLM01105， 检查要素：RDLINK(" + rdLink.pid() + "), 触法时机：修改");
				
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
				int laneNum = rdLink.getLaneNum();
				if (rdLink.changedFields().containsKey("laneNum")) {
					laneNum = (int) rdLink.changedFields().get("laneNum");
				}
				if (laneNum != 1) {
					linkLaneNumMap.put(rdLink.getPid(), rdLink);
					
					for(IRow formRow : rdLink.getForms())
					{
						RdLinkForm form = (RdLinkForm) formRow;
						
						if(form.getFormOfWay() == 20)
						{
							linkFormOf20List.add(form.getLinkPid());
							break;
						}
					}
				}
			} else if (row instanceof RdLinkForm) {
				RdLinkForm form = (RdLinkForm) row;

				int formOfWay = form.getFormOfWay();

				if (form.status() == ObjStatus.UPDATE && form.changedFields().containsKey("formOfWay")) {
					formOfWay = (int) form.changedFields().get("formOfWay");
				}
				if (form.status() != ObjStatus.DELETE && formOfWay == 20) {
					linkFormOf20List.add(form.getLinkPid());
					if(!linkLaneNumMap.containsKey(form.getLinkPid()))
					{
						RdLink link = (RdLink) selector.loadById(form.getLinkPid(), true, true);
						
						if(link.getLaneNum() != 1)
						{
							linkLaneNumMap.put(link.getPid(), link);
						}
					}
				}
				if(form.status() == ObjStatus.DELETE && formOfWay== 20)
				{
					linkFormOf20List.remove(form.getLinkPid());
				}
			}
		}
	}
}
