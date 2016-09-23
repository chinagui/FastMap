package com.navinfo.dataservice.engine.editplus.model.ad;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.editplus.model.AbstractLinkMesh;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
 * @ClassName: AdLinkMesh
 * @author xiaoxiaowen4127
 * @date 2016年8月18日
 * @Description: AdLinkMesh.java
 */
public class AdLinkMesh extends AbstractLinkMesh {
	@Override
	public String tableName() {
		return "AD_LINK";
	}
	@Override
	public ObjType objType() {
		return ObjType.ADLINKMESH;
	}

}
