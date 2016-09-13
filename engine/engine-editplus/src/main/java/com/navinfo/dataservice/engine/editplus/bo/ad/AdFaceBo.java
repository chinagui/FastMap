package com.navinfo.dataservice.engine.editplus.bo.ad;

import java.util.List;

import com.navinfo.dataservice.engine.editplus.bo.AbstractFaceBo;
import com.navinfo.dataservice.engine.editplus.model.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.ad.AdFace;
import com.navinfo.dataservice.engine.editplus.model.ad.AdFaceTopo;
import com.navinfo.dataservice.engine.editplus.operation.OperationResult;
import com.navinfo.dataservice.engine.editplus.operation.OperationType;

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
