package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
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
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
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
	private Set<Integer> linkFormOf14List = new HashSet<>();

	public GLM01307() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		List<Integer> linkPids = new ArrayList<>();
		for (Map.Entry<Integer, RdLink> entry : linkDirectMap.entrySet()) {
			int linkPid = entry.getKey();
			RdLink rdLink = entry.getValue();
			if (linkPids.contains(rdLink.pid()))
				continue;
			if(linkFormOf14List.contains(linkPid))
			{
				logger.debug("检查类型：postCheck， 检查规则：GLM01008， 检查要素：RDLINK(" + rdLink.pid() + "), 触法时机：修改");
				
				this.setCheckResult(rdLink.getGeometry(), "[RD_LINK," + rdLink.getPid() + "]", rdLink.getMeshId());
			}
			
			// 检查过的link加入linkPid集合中
			linkPids.add(rdLink.pid());
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
				int direct = rdLink.getDirect();
				if (rdLink.changedFields().containsKey("direct")) {
					direct = (int) rdLink.changedFields().get("direct");
				}
				if (direct != 1) {
					linkDirectMap.put(rdLink.getPid(), rdLink);
					
					for(IRow formRow : rdLink.getForms())
					{
						RdLinkForm form = (RdLinkForm) formRow;
						
						if(form.getFormOfWay() == 14)
						{
							linkFormOf14List.add(form.getLinkPid());
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
				if (form.status() != ObjStatus.DELETE && formOfWay == 14) {
					linkFormOf14List.add(form.getLinkPid());
					if(!linkDirectMap.containsKey(form.getLinkPid()))
					{
						RdLink link = (RdLink) selector.loadById(form.getLinkPid(), true, true);
						
						if(link.getDirect() == 1)
						{
							linkDirectMap.put(link.getPid(), link);
						}
					}
				}
				if(form.status() == ObjStatus.DELETE && formOfWay== 14)
				{
					linkFormOf14List.remove(form.getLinkPid());
				}
			}
		}
	}
}
