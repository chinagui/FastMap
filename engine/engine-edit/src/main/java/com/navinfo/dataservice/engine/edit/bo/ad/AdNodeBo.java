package com.navinfo.dataservice.engine.edit.bo.ad;

import com.navinfo.dataservice.engine.edit.bo.AbstractBo;
import com.navinfo.dataservice.engine.edit.bo.AbstractNodeBo;
import com.navinfo.dataservice.engine.edit.model.BasicObj;
import com.navinfo.dataservice.engine.edit.model.ad.AdNode;
import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: BoAdNode
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: BoAdNode.java
 */
public class AdNodeBo extends AbstractNodeBo{
	
	protected AdNode obj;

	@Override
	public AdNodeBo copy() throws Exception {
		return null;
	}

	@Override
	public void setObj(BasicObj obj) {
		obj = (AdNode)obj;
	}

	@Override
	public AdNode getObj() {
		// TODO Auto-generated method stub
		return obj;
	}
}
