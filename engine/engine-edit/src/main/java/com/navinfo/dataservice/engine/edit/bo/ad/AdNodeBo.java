package com.navinfo.dataservice.engine.edit.bo.ad;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.engine.edit.bo.NodeBo;

/** 
 * @ClassName: BoAdNode
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: BoAdNode.java
 */
public class AdNodeBo extends NodeBo{
	
	protected AdNode adNode;

	@Override
	public void setPo(IObj po) {
		this.adNode=(AdNode)po;
		this.geometry=adNode.getGeometry();
	}

	@Override
	public IObj getPo() {
		// TODO Auto-generated method stub
		return adNode;
	}
}
