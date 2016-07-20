package com.navinfo.dataservice.engine.edit.bo.ad;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;

/** 
 * @ClassName: BoAdLink
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: BoAdLink.java
 */
public class AdFaceBo {
	protected AdFace adFace;
	protected List<AdFaceTopo> topos;
	
	public Result breakoff(AdLink oldLink, AdLink newLeftLink, AdLink newRightLink){
		return null;
	}
	
}
