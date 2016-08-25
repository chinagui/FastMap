package com.navinfo.dataservice.engine.edit.bo.ad;

import java.util.List;
import com.navinfo.dataservice.engine.edit.model.BasicObj;
import com.navinfo.dataservice.engine.edit.model.OperationResult;
import com.navinfo.dataservice.engine.edit.model.OperationType;
import com.navinfo.dataservice.engine.edit.model.ad.AdFace;
import com.navinfo.dataservice.engine.edit.model.ad.AdFaceTopo;
import com.navinfo.dataservice.engine.edit.bo.AbstractFaceBo;

/**
 * @ClassName: BoAdLink
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: BoAdLink.java
 */
public class AdFaceBo extends AbstractFaceBo {
	protected AdFace obj;

	@Override
	public AdFaceTopo createTopo(long linkPid){
		AdFaceTopo topo = new AdFaceTopo();
		topo.setOpType(OperationType.INSERT);
		topo.setFacePid(obj.getPid());
		topo.setLinkPid(linkPid);
		return topo;
	}
	public OperationResult breakoff(AdLinkBo oldLink, AdLinkBo newLeftLink,
			AdLinkBo newRightLink) throws Exception {
		OperationResult result = super.breakoff(oldLink,newLeftLink,newRightLink);
		return result;
	}

	@Override
	public AdFaceBo copy() {
		return null;
	}

	@Override
	public void setObj(BasicObj obj) {
		this.obj= (AdFace) obj;
	}

	@Override
	public BasicObj getObj() {
		return obj;
	}
}
