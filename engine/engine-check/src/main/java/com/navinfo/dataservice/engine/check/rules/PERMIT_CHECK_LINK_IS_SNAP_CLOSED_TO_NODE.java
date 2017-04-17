package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
 * @ClassName: PERMIT_CHECK_LINK_IS_SNAP_CLOSED_TO_NODE
 * @author songdongyan
 * @date 2017年4月13日
 * @Description: 不能打断形成过短的link
 * 新增link前检查
 * 移动端点前检查
 * 分离节点前检查
 * 删除形状点前检查
 * 打断link前检查
 * 新增node点前检查
 */
public class PERMIT_CHECK_LINK_IS_SNAP_CLOSED_TO_NODE extends baseRule {

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow row:checkCommand.getGlmList()){
			//条件线限速时间段,条件线限速限速条件
			if (row instanceof RdLink){
				RdLink rdLink = (RdLink) row;
				this.checkRdLink(rdLink);
			}
		}
	}

	/**
	 * @param rdLink
	 */
	private void checkRdLink(RdLink rdLink) {
		if(rdLink.status().equals(ObjStatus.INSERT)){
			double length = rdLink.getLength();
			if(length<2){
				this.setCheckResult("", "", 0);
			}
		}
		else if(rdLink.status().equals(ObjStatus.UPDATE)){
			if(rdLink.changedFields().containsKey("length")){
				double length = Double.parseDouble(rdLink.changedFields().get("length").toString());
				if(length<2){
					this.setCheckResult("", "", 0);
				}
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

}
