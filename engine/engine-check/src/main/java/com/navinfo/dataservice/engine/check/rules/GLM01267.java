package com.navinfo.dataservice.engine.check.rules;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * @ClassName: GLM01267
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 检查对象：含“环岛”属性的link；
				   检查原则：限速类型为“普通”时，该link上的速度限制等级不能为1，2，3，否则报err
 */
public class GLM01267 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01267.class);

	/**
	 * 包含环岛形态的linkPid
	 */
	private Set<Integer> formOf33Set = new HashSet<>();
	
	/**
	 * 限速类型为“普通”，link上的速度限制等级为1，2，3的linkPid
	 */
	private Set<Integer> speedLimitLinkPidSet = new HashSet<>();
	
	/**
	 * 最终需要提示log的linkPid集合
	 */
	private Set<Integer> resultLinkPidSet = new HashSet<>();

	public GLM01267() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		AbstractSelector selector = new AbstractSelector(RdLink.class, getConn());
		for (Integer linkPid : resultLinkPidSet) {
			RdLink rdLink = (RdLink) selector.loadById(linkPid, true, true);
			logger.debug("检查类型：postCheck， 检查规则：GLM01267， 检查要素：RDLINK(" + linkPid + "), 触法时机：线限速等级编辑");
			this.setCheckResult(rdLink.getGeometry(), "[RD_LINK," + linkPid + "]", rdLink.getMeshId());
		}
	}

	/**
	 * @param checkCommand
	 * @throws Exception
	 */
	private void prepareData(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row instanceof RdLinkForm) {
				RdLinkForm form = (RdLinkForm) row;

				int formOfWay = form.getFormOfWay();

				if (form.status() == ObjStatus.UPDATE && form.changedFields().containsKey("formOfWay")) {
					formOfWay = (int) form.changedFields().get("formOfWay");
				}
				if (form.status() != ObjStatus.DELETE && formOfWay == 33) {
					formOf33Set.add(form.getLinkPid());
				}
				if (form.status() == ObjStatus.DELETE && formOfWay == 33) {
					formOf33Set.remove(form.getLinkPid());
				}
			}
			else if(row instanceof RdLinkSpeedlimit)
			{
				RdLinkSpeedlimit limit = (RdLinkSpeedlimit) row;
				
				int speedType = limit.getSpeedType();
				
				if(limit.status() != ObjStatus.DELETE)
				{
					if (limit.changedFields().containsKey("speedType")) {
						speedType = (int) limit.changedFields().get("speedType");
					}
					if (speedType == 0) {
						//限速等级
						int speedClass = limit.getSpeedClass();
						
						if(limit.changedFields().containsKey("speedClass"))
						{
							speedClass = limit.getSpeedClass();
						}
						if(speedClass == 1 || speedClass == 2 || speedClass == 3)
						{
							speedLimitLinkPidSet.add(limit.getLinkPid());
						}
					}
				}
				else
				{
					speedLimitLinkPidSet.remove(limit.getLinkPid());
				}
			}
		}
		//取交集就是符合log条件的linkpid
		resultLinkPidSet.addAll(formOf33Set);
		
		resultLinkPidSet.retainAll(speedLimitLinkPidSet);
	}

}
