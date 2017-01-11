package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkRtic;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
 * @ClassName: GLM53002
 * @author songdongyan
 * @date 2017年1月9日
 * @Description: RTIC代码值域超值，应该在1-4095之间。
 * 车厂RTIC图面编辑
 * 车厂RTIC代码编辑
 */
public class GLM53002 extends baseRule{

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
		for(IRow obj:checkCommand.getGlmList()){
			//新增/修改RdLinkRtic
			if(obj instanceof RdLinkRtic ){
				RdLinkRtic rdLinkRtic=(RdLinkRtic) obj;
				checkRdLinkRtic(rdLinkRtic);
			}
			
		}
		
	}

	/**
	 * @param rdLinkRtic
	 */
	private void checkRdLinkRtic(RdLinkRtic rdLinkRtic) {
		String target = "[RD_LINK," + rdLinkRtic.getLinkPid() + "]";
		if(rdLinkRtic.status().equals(ObjStatus.INSERT)){
			if((rdLinkRtic.getCode()<1)||(rdLinkRtic.getCode()>4095)){
				this.setCheckResult("", target, 0);
			}
		}else if(rdLinkRtic.status().equals(ObjStatus.UPDATE)){
			if(rdLinkRtic.changedFields().containsKey("code")){
				int code = Integer.parseInt(rdLinkRtic.changedFields().get("code").toString());
				if((code<1)||(code>4095)){
					this.setCheckResult("", target, 0);
				}
			}
		}
		
	}

}
