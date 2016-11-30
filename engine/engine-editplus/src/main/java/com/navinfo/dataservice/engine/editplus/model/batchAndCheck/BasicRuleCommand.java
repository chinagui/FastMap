package com.navinfo.dataservice.engine.editplus.model.batchAndCheck;

import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjChildrenIncreSelector;

public class BasicRuleCommand {
	private Map<String,Map<Long,BasicObj>> allDatas;
	private Connection conn;
	private Map<String,Map<Long,BasicObj>> referDatas;

	public BasicRuleCommand() {
		// TODO Auto-generated constructor stub
	}
	
	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public Map<String,Map<Long,BasicObj>> getAllDatas() {
		return allDatas;
	}

	public void setAllDatas(Map<String,Map<Long,BasicObj>> allDatas) {
		this.allDatas = allDatas;
	}

	public Map<String,Map<Long,BasicObj>> getReferDatas() {
		return referDatas;
	}

	public void setReferDatas(Map<String,Map<Long,BasicObj>> referDatas) {
		this.referDatas = referDatas;
	}
	
	public Map<Long,BasicObj> loadReferObjs(Set<Long> objPids,String ObjType,Set<String> referSubrow) throws Exception{
		Map<String,Map<Long,BasicObj>> returnDatas=new HashMap<String,Map<Long,BasicObj>>();
		Map<Long,BasicObj> returnDataTmp=new HashMap<Long, BasicObj>();
		if(!ObjType.isEmpty()&&referSubrow!=null&&!referSubrow.isEmpty()){
			Map<Long,BasicObj> allDataTmp=allDatas.get(ObjType);
			Map<Long, BasicObj> referDatasMap=referDatas.get(ObjType);
			for()
			//增量加载需要参考的子表数据
			ObjChildrenIncreSelector.increSelect(conn,result.getAllObjsMap(), selConfig);
		}
		return null;
	}	
}
