package com.navinfo.dataservice.dao.glm.selector.rd.se;

import java.sql.Connection;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

/**
 * @Title: RdSeSelector.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月1日 上午10:55:25
 * @version: v1.0
 */
public class RdSeSelector extends AbstractSelector {

	public RdSeSelector(Connection conn) throws Exception {
		super(RdSe.class, conn);
	}

}
