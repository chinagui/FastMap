package com.navinfo.dataservice.engine.editplus.model.ad;

import com.navinfo.dataservice.dao.plus.obj.ObjectType;
import com.navinfo.dataservice.engine.editplus.model.AbstractLinkMesh;

/** 
 * @ClassName: AdLinkMesh
 * @author xiaoxiaowen4127
 * @date 2016年8月18日
 * @Description: AdLinkMesh.java
 */
public class AdLinkMesh extends AbstractLinkMesh {

	public AdLinkMesh(long objPid) {
		super(objPid);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String tableName() {
		return "AD_LINK";
	}

}
