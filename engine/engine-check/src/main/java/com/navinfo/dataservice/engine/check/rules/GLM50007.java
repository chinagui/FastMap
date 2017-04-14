package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
 * @ClassName: GLM50007
 * @author songdongyan
 * @date 2017年4月9日
 * @Description:省、直辖市区界种别，只能和无属性共存
 * link种别编辑服务端后检查
 * LINK属性编辑服务端后检查
 */
public class GLM50007 extends baseRule {

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row instanceof AdLink) {
				AdLink adLink = (AdLink) row;
				int kind = adLink.getKind();
				if (adLink.changedFields().containsKey("kind")) {
					kind = (int) adLink.changedFields().get("kind");
				}
				if (kind != 1)
					continue;
				
				int form = adLink.getForm();
				if (adLink.changedFields().containsKey("form"))
					form = (int) adLink.changedFields().get("form");
				if ((kind == 1)&&(form != 1)){
					String target = "[AD_LINK, " + adLink.pid() + "]";
					setCheckResult("", target, 0);
				} 
			}
		}
	}

}
