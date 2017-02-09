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
 * GLM01092
 * 
 * @ClassName: GLM01092
 * @author Zhang Xiaolong
 * @date 2017年2月6日 上午11:30:45
 * @Description: 私道的FC应该为5
 */

public class GLM01092 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01092.class);

	/**
	 * 功能等级为非5的link
	 */
	private Map<Integer, RdLink> fucClassLinkMap = new HashMap<>();

	/**
	 * 私道linkPid
	 */
	private Set<Integer> linkFormOf18List = new HashSet<>();

	public GLM01092() {
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
			if(linkFormOf18List.contains(linkPid))
			{
				logger.debug("检查类型：postCheck， 检查规则：GLM01092， 检查要素：RDLINK(" + rdLink.pid() + "), 触法时机：修改");
				
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
					
					for(IRow formRow : rdLink.getForms())
					{
						RdLinkForm form = (RdLinkForm) formRow;
						
						if(form.getFormOfWay() == 18)
						{
							linkFormOf18List.add(form.getLinkPid());
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
				if (form.status() != ObjStatus.DELETE && formOfWay == 18) {
					linkFormOf18List.add(form.getLinkPid());
					if(!fucClassLinkMap.containsKey(form.getLinkPid()))
					{
						RdLink link = (RdLink) selector.loadById(form.getLinkPid(), true, true);
						
						if(link.getFunctionClass() != 5)
						{
							fucClassLinkMap.put(link.getPid(), link);
						}
					}
				}
				if(form.status() == ObjStatus.DELETE && formOfWay== 18)
				{
					linkFormOf18List.remove(form.getLinkPid());
				}
			}
		}
	}
}
