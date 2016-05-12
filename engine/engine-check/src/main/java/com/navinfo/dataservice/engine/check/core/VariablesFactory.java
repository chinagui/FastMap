package com.navinfo.dataservice.engine.check.core;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class VariablesFactory {

	public static String getRdLinkPid(IRow data){
		if(data instanceof RdLink){return String.valueOf(((RdLink) data).getPid());}
		return null;
	}

}
