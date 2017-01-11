package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkIntRtic;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
 * @ClassName: GLM53080
 * @author songdongyan
 * @date 2017年1月10日
 * @Description: RTIC等级不能为0
 * 互联网RTIC图面编辑
 * 互联网RTIC等级编辑
 */
public class GLM53080 extends baseRule{

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
				RdLinkIntRtic rdLinkRtic=(RdLinkIntRtic) obj;
				checkRdLinkIntRtic(rdLinkRtic);
			}
		}
		
	}



	/**
	 * @param rdLinkRtic
	 * @throws Exception 
	 */
	private void checkRdLinkIntRtic(RdLinkIntRtic rdLinkRtic) throws Exception {
		if(rdLinkRtic.status().equals(ObjStatus.INSERT)){
			if(rdLinkRtic.getRank()==0){
				String target = "[RD_LINK," + rdLinkRtic.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}else if(rdLinkRtic.status().equals(ObjStatus.UPDATE)){
			if(rdLinkRtic.changedFields().containsKey("rank")){
				int rank = Integer.parseInt(rdLinkRtic.changedFields().get("rank").toString());
				if(rank==0){
					String target = "[RD_LINK," + rdLinkRtic.getLinkPid() + "]";
					this.setCheckResult("", target, 0);
				}
			}
		}
	}

}

