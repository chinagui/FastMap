package com.navinfo.dataservice.engine.edit.model.ad;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.model.AbstractFaceTopo;

/** 
 * @ClassName: AdFaceTop
 * @author xiaoxiaowen4127
 * @date 2016年8月18日
 * @Description: AdFaceTop.java
 */
public class AdFaceTopo extends AbstractFaceTopo {

	@Override
	public String tableName() {
		return "AD_FACE_TOPO";
	}

	@Override
	public ObjType objType() {
		return ObjType.ADFACETOPO;
	}

}
