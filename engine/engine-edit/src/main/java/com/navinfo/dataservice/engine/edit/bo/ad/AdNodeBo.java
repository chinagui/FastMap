package com.navinfo.dataservice.engine.edit.bo.ad;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;

/** 
 * @ClassName: BoAdNode
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: BoAdNode.java
 */
public class AdNodeBo extends NodeBo{
	public AdNodeBo(Connection conn, int nodePid, boolean isLock) {
		this.adNode = PoFactory.getInstance().getByPK(conn, AdNode.class, nodePid, isLock);
	}
	
	protected AdNode adNode;
}
