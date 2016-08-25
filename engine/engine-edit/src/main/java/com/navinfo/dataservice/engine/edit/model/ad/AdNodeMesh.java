package com.navinfo.dataservice.engine.edit.model.ad;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.model.AbstractNodeMesh;

/** 
 * @ClassName: AdNodeMesh
 * @author xiaoxiaowen4127
 * @date 2016年8月18日
 * @Description: AdNodeMesh.java
 */
public class AdNodeMesh extends AbstractNodeMesh {

	@Override
	public String tableName() {
		return "AD_NODE_MESH";
	}

	@Override
	public ObjType objType() {
		return ObjType.ADNODEMESH;
	}

}
