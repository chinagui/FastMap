package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkRtic;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
 * @ClassName: GLM53038
 * @author songdongyan
 * @date 2017年1月9日
 * @Description: 道路制作有RTIC信息，RTIC方向不能是无
 * 车厂RTIC图面编辑
 */
public class GLM53038 extends baseRule{

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
	 * @throws Exception 
	 */
	private void checkRdLinkRtic(RdLinkRtic rdLinkRtic) throws Exception {
		if(rdLinkRtic.status().equals(ObjStatus.INSERT)){
			if(rdLinkRtic.getRticDir() == 0){
				String target = "[RD_LINK," + rdLinkRtic.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}else if(rdLinkRtic.status().equals(ObjStatus.UPDATE)){
			if(rdLinkRtic.changedFields().containsKey("rticDir")){
				int rticDir = Integer.parseInt(rdLinkRtic.changedFields().get("rticDir").toString());
				if(rticDir==0){
					String target = "[RD_LINK," + rdLinkRtic.getLinkPid() + "]";
					this.setCheckResult("", target, 0);
				}
			}
		}
		
	}

}


