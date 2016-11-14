package com.navinfo.dataservice.engine.editplus.model.ad;

import com.navinfo.dataservice.engine.editplus.model.AbstractLinkMesh;
import com.navinfo.dataservice.engine.editplus.model.obj.ObjectType;

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
	public ObjectType objType() {
		return ObjectType.AD_LINK;
	}

}
