package com.navinfo.dataservice.engine.editplus.model.ad;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.plus.obj.ObjectType;
import com.navinfo.dataservice.engine.editplus.model.AbstractNodeMesh;

/** 
 * @ClassName: AdNodeMesh
 * @author xiaoxiaowen4127
 * @date 2016年8月18日
 * @Description: AdNodeMesh.java
 */
public class AdNodeMesh extends AbstractNodeMesh {

	public AdNodeMesh(long objPid) {
		super(objPid);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String tableName() {
		return "AD_NODE_MESH";
	}

}
