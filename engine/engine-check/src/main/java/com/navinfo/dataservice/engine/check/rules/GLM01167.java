package com.navinfo.dataservice.engine.check.rules;

import java.util.HashSet;
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
 * @ClassName: GLM01167
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 检查对象：含有SA\PA属性的link，且含有IC\JCT和匝道属性。
				   检查原则：检查对象上不能含有“POI连接路”属性，否则报err
 */
public class GLM01167 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01167.class);

	/**
	 * sa/pa形态的link
	 */
	private Set<Integer> saPaLinkPidSet = new HashSet<>();
	
	/**
	 * ic/jct形态的link
	 */
	private Set<Integer> icJctLinkPidSet = new HashSet<>();
	
	/**
	 * poi连接路形态的link
	 */
	private Set<Integer> poiFormSet = new HashSet<>();
	
	/**
	 * 最终报检查log的link
	 */
	private Set<Integer> resultLinkPidSet = new HashSet<>();
	
	public GLM01167() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		AbstractSelector selector = new AbstractSelector(RdLink.class, getConn());
		for (Integer linkPid : resultLinkPidSet) {
			logger.debug("检查类型：postCheck， 检查规则：GLM01167， 检查要素：RDLINK(" + linkPid + "), 触法时机：道路属性编辑");
			RdLink rdLink = (RdLink) selector.loadById(linkPid, true, true);
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
				if(formOfWay == 10|| formOfWay == 11)
				{
					if (form.status() != ObjStatus.DELETE) {
						icJctLinkPidSet.add(form.getLinkPid());
					}
					else
					{
						icJctLinkPidSet.remove(form.getLinkPid());
					}
				}
				else if(formOfWay == 12|| formOfWay == 13)
				{
					if (form.status() != ObjStatus.DELETE) {
						saPaLinkPidSet.add(form.getLinkPid());
					}
					else
					{
						saPaLinkPidSet.remove(form.getLinkPid());
					}
				}
				else if(formOfWay == 36)
				{
					if (form.status() != ObjStatus.DELETE) {
						poiFormSet.add(form.getLinkPid());
					}
					else
					{
						poiFormSet.remove(form.getLinkPid());
					}
				}
			}
		}
		//获取满足sa/pa,ic/jct,poi连接路的link
		resultLinkPidSet.addAll(poiFormSet);
		resultLinkPidSet.retainAll(saPaLinkPidSet);
		resultLinkPidSet.retainAll(icJctLinkPidSet);
	}

}
