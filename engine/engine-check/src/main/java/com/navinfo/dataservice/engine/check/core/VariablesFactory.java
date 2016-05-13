package com.navinfo.dataservice.engine.check.core;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;

public class VariablesFactory {

	public static String getRdLinkPid(IRow data){
		if(data instanceof RdLink){return String.valueOf(((RdLink) data).getPid());}
		if(data instanceof RdRestriction){return String.valueOf(((RdRestriction) data).getInLinkPid());}
		if(data instanceof RdRestrictionDetail){return String.valueOf(((RdRestrictionDetail) data).getOutLinkPid());}
		return null;
	}

}
