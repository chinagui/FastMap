package com.navinfo.dataservice.engine.editplus.model.batchAndCheck;

import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.ObjChildrenIncreSelector;

public class BasicRuleCommand {
	//检查/批处理的执行数据对象
	private Map<String,Map<Long,BasicObj>> allDatas;
	private Connection conn;
	/*检查/批处理规则执行过程中，用到的参考数据池子。
	 * 若修改参考数据，那么需要在框架最外面，增加轮训referDatas的过程，
	 * 将所有修改的数据put仅OperationResult(batch,check入口的时候会传这个参数)中*/
	private Map<String,Map<Long,BasicObj>> referDatas=new HashMap<String,Map<Long,BasicObj>>();
	//根据业务需要，添加各种参数。例如，有规则需要子任务的业务类型，则 key:"subTaskWorkKind",value:0(0,2,4)
	private Map<String,Object> parameter;

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
	
	public Map<String, Object> getParameter() {
		return parameter;
	}

	public void setParameter(Map<String, Object> parameter) {
		this.parameter = parameter;
	}

	/**
	 * 根据pid集合加载非删除数据
	 * @param objPids 要加载数据的集合
	 * @param objType 要加载数据的类型，来自ObjectType枚举类
	 * @param referSubrow 要加载数据的参考用子表，
	 * @param isLock 是否对加载数据加锁，true 加锁，false 不加
	 * @return Map<Long,BasicObj> ：key:pid,value:数据对象BasicObj
	 * @throws Exception
	 */
	public Map<Long,BasicObj> loadReferObjs(Collection<Long> objPids,String objType,Set<String> referSubrow,boolean isLock) throws Exception{
		Map<String,Map<Long,BasicObj>> returnDatas=new HashMap<String,Map<Long,BasicObj>>();
		Map<Long,BasicObj> returnDataTmp=new HashMap<Long, BasicObj>();
		if(!objType.isEmpty()){
			Map<Long,BasicObj> allDataTmp=allDatas.get(objType);
			Map<Long, BasicObj> referDatasMap=referDatas.get(objType);
			Set<Long> unLoadPid=new HashSet<Long>();
			for(Long pid:objPids){
				if(allDataTmp!=null && allDataTmp.containsKey(pid)){
					BasicObj obj=allDataTmp.get(pid);
					if(!obj.isDeleted()){
						returnDataTmp.put(pid, allDataTmp.get(pid));}
				}else if(referDatasMap!=null && referDatasMap.containsKey(pid)){
					BasicObj obj=referDatasMap.get(pid);
					if(!obj.isDeleted()){
						returnDataTmp.put(pid, referDatasMap.get(pid));}
				}else{
					unLoadPid.add(pid);
				}
			}
			boolean isMainOnly=false;
			if(referSubrow==null||referSubrow.isEmpty()){isMainOnly=true;}
			Map<Long,BasicObj> unLoadMap=ObjBatchSelector.selectByPids(getConn(), objType, referSubrow, isMainOnly,unLoadPid, isLock, true);
			if(referDatasMap==null){referDatasMap=new HashMap<Long,BasicObj>();}
			referDatasMap.putAll(unLoadMap);
			referDatas.put(objType, referDatasMap);			
			for(Long objPid:unLoadMap.keySet()){
				BasicObj obj=referDatasMap.get(objPid);
				if(!obj.isDeleted()){
					returnDataTmp.put(objPid,obj);}
			}
			if(referSubrow!=null&&!referSubrow.isEmpty()){
				returnDatas.put(objType, returnDataTmp);
				//加载selectByPids的同时已经加载了子表，但是有些不是此次加载的，需要二次加载下子表
				Map<String,Set<String>> selConfig=new HashMap<String,Set<String>>();
				selConfig.put(objType, referSubrow);
				//增量加载需要参考的子表数据
				ObjChildrenIncreSelector.increSelect(conn,returnDatas, selConfig);
			}
			return returnDataTmp;
		}
		return null;
	}
	
	/**
	 * 根据pid集合加载非删除数据同时加载修改log
	 * @param objPids 要加载数据的集合
	 * @param objType 要加载数据的类型，来自ObjectType枚举类
	 * @param referSubrow 要加载数据的参考用子表，
	 * @param isLock 是否对加载数据加锁，true 加锁，false 不加
	 * @return Map<Long,BasicObj> ：key:pid,value:数据对象BasicObj
	 * @throws Exception
	 */
	public Map<Long,BasicObj> loadReferObjsByLog(Collection<Long> objPids,String objType,Set<String> referSubrow,boolean isLock) throws Exception{
		Map<String,Map<Long,BasicObj>> returnDatas=new HashMap<String,Map<Long,BasicObj>>();
		Map<Long,BasicObj> returnDataTmp=new HashMap<Long, BasicObj>();
		if(!objType.isEmpty()){
			Map<Long,BasicObj> allDataTmp=allDatas.get(objType);
			Set<Long> unLoadPid=new HashSet<Long>();
			for(Long pid:objPids){
				if(allDataTmp!=null && allDataTmp.containsKey(pid)){
					BasicObj obj=allDataTmp.get(pid);
					if(!obj.isDeleted()){
						returnDataTmp.put(pid, allDataTmp.get(pid));}
				}else{
					unLoadPid.add(pid);
				}
			}
			boolean isMainOnly=false;
			
			Map<Long, List<LogDetail>> logs = PoiLogDetailStat.loadAllLog(getConn(), unLoadPid);			
			Map<String, Set<String>> tabNames=ObjHisLogParser.getChangeTableSet(logs);
			if(referSubrow==null){
				referSubrow=new HashSet<String>();
				referSubrow.addAll(tabNames.get(objType));
			}
			if(referSubrow==null||referSubrow.isEmpty()){isMainOnly=true;}
			Map<Long,BasicObj> unLoadMap=ObjBatchSelector.selectByPids(getConn(), objType, referSubrow, isMainOnly,unLoadPid, isLock, true);
			//将poi对象与履历合并起来
			ObjHisLogParser.parse(unLoadMap, logs);
			
			for(Long objPid:unLoadMap.keySet()){
				BasicObj obj=unLoadMap.get(objPid);
				if(!obj.isDeleted()){
					returnDataTmp.put(objPid,obj);}
			}
			if(referSubrow!=null&&!referSubrow.isEmpty()){
				returnDatas.put(objType, returnDataTmp);
				//加载selectByPids的同时已经加载了子表，但是有些不是此次加载的，需要二次加载下子表
				Map<String,Set<String>> selConfig=new HashMap<String,Set<String>>();
				selConfig.put(objType, referSubrow);
				//增量加载需要参考的子表数据
				ObjChildrenIncreSelector.increSelect(conn,returnDatas, selConfig);
			}
			return returnDataTmp;
		}
		return null;
	}	
}
