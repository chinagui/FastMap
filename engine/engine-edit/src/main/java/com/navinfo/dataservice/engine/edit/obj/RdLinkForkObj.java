package com.navinfo.dataservice.engine.edit.obj;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

/** 
 * @ClassName: RdLinkForkObj
 * @author xiaoxiaowen4127
 * @date 2016年12月29日
 * @Description: RdLinkForkObj.java
 */
public class RdLinkForkObj {
	protected long nodePid;
	protected List<RdLink> addLinks;
	protected List<RdLink> delLinks;
	protected List<RdLink> relLinks;
	protected Map<Long,Long> inheritRelations;//key是源link，value是继承的link

	public String parseChange(Result result)throws Exception{
		
		
		return null;
	}
	
	
	private void replaceLinks(Result result) throws Exception{
		
		
	}
	private void recomputeFork(Result result)throws Exception{
		
	}
}
