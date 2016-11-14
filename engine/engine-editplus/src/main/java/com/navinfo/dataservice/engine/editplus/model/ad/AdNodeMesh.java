package com.navinfo.dataservice.engine.editplus.model.ad;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.editplus.model.AbstractNodeMesh;
import com.navinfo.dataservice.engine.editplus.model.obj.ObjectType;

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
	public ObjectType objType() {
		return ObjectType.AD_NODE;
	}

}
