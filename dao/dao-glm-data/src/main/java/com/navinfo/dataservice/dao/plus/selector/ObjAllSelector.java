package com.navinfo.dataservice.dao.plus.selector;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;

/** 
 * @ClassName: ObjAllSelector
 * @author xiaoxiaowen4127
 * @date 2017年8月31日
 * @Description: ObjAllSelector.java
 */
public class ObjAllSelector {
	protected static Logger log = LoggerRepos.getLogger(ObjAllSelector.class);

	public static Map<Long,BasicObj> selectAll(Connection conn,String objType,Set<String> sepcTables,boolean isLock,boolean isWait)throws Exception{
		Map<Long,BasicObj> objs = new HashMap<Long,BasicObj>();
		return null;
	}
	
	
	
}
