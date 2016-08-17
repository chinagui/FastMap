package com.navinfo.dataservice.engine.edit.bo.ad;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.engine.edit.bo.AbstractBo;
import com.navinfo.dataservice.engine.edit.bo.NodeBo;
import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: BoAdNode
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: BoAdNode.java
 */
public class AdNodeBo extends NodeBo{
	
	protected AdNode po;

	@Override
	public void setPo(IObj po) {
		this.po=(AdNode)po;
	}

	@Override
	public IObj getPo() {
		return po;
	}

	@Override
	public Geometry getGeometry() {
		return po.getGeometry();
	}

	@Override
	public void setGeometry(Geometry geo) {
		this.po.setGeometry(geo);
		
	}

	@Override
	public AdNodeBo copy() throws Exception {
		return null;
	}
}
