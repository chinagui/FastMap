package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkIntRtic;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkRtic;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
 * @ClassName: GLM53051
 * @author songdongyan
 * @date 2017年1月10日
 * @Description: RTIC代码值域超值，应该在1-4095之间。
 * 互联网RTIC图面编辑
 * 互联网RTIC代码编辑
 */
public class GLM53051 extends baseRule{

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
			if(obj instanceof RdLinkIntRtic ){
				RdLinkIntRtic rdLinkIntRtic=(RdLinkIntRtic) obj;
				checkRdLinkIntRtic(rdLinkIntRtic);
			}
			
		}
		
	}

	/**
	 * @param rdLinkIntRtic
	 */
	private void checkRdLinkIntRtic(RdLinkIntRtic rdLinkIntRtic) {
		String target = "[RD_LINK," + rdLinkIntRtic.getLinkPid() + "]";
		if(rdLinkIntRtic.status().equals(ObjStatus.INSERT)){
			if((rdLinkIntRtic.getCode()<1)||(rdLinkIntRtic.getCode()>4095)){
				this.setCheckResult("", target, 0);
			}
		}else if(rdLinkIntRtic.status().equals(ObjStatus.UPDATE)){
			if(rdLinkIntRtic.changedFields().containsKey("code")){
				int code = Integer.parseInt(rdLinkIntRtic.changedFields().get("code").toString());
				if((code<1)||(code>4095)){
					this.setCheckResult("", target, 0);
				}
			}
		}
		
	}

}
