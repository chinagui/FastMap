package com.navinfo.dataservice.engine.editplus.model.ad;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.editplus.glm.NonObjPidException;
import com.navinfo.dataservice.engine.editplus.model.AbstractFaceTopo;
import com.navinfo.dataservice.engine.editplus.model.obj.ObjectType;

/** 
 * @ClassName: AdFaceTop
 * @author xiaoxiaowen4127
 * @date 2016年8月18日
 * @Description: AdFaceTop.java
 */
public class AdFaceTopo extends AbstractFaceTopo {

	public AdFaceTopo(long objPid) {
		super(objPid);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String tableName() {
		return "AD_FACE_TOPO";
	}

}
