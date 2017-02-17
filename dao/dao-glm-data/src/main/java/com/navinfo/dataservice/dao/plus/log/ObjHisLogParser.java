package com.navinfo.dataservice.dao.plus.log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.ChangeLog;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.utils.RowJsonUtils;

import net.sf.json.JSONObject;

/** 
 * @ClassName: ObjHisLogParser
 * @author xiaoxiaowen4127
 * @date 2016年12月5日
 * @Description: ObjHisLogParser.java
 */
public class ObjHisLogParser {
	private static Logger log = Logger.getLogger(ObjHisLogParser.class);

	/**
	 * 分析履历，将履历中涉及的变更过的子表集合返回
	 * @param logs
	 * @return [IX_POI_NAME,IX_POI_ADDRESS]
	 */
	public static Map<String,Set<String>> getChangeTableSet(Map<Long, List<LogDetail>> logs) {
		Map<String,Set<String>> subtables=new HashMap<String,Set<String>>();
		if(logs==null || logs.size()==0){return subtables;}
		for(Long objId:logs.keySet()){
			List<LogDetail> logList = logs.get(objId);
			for(LogDetail logTmp:logList){
				String tableName = logTmp.getTbNm();
				String obName=logTmp.getObNm();
				if(!tableName.equals(obName)){
					if(!subtables.containsKey(obName)){subtables.put(obName, new HashSet<String>());}
					subtables.get(obName).add(tableName);}
			}
		}
		return subtables;
	}
	
	public static void parse(BasicObj obj,List<LogDetail> logs)throws Exception{
		if(obj!=null&&logs!=null){
			String mainTableName = obj.getMainrow().tableName();
			for(LogDetail ld:logs){
				String tb = ld.getTbNm();
				OperationType opTp = OperationType.getOperationType(ld.getOpTp());
				Map<String,Object> oldValues = null;
				if(opTp.equals(OperationType.UPDATE)){
					JSONObject jo = JSONObject.fromObject(ld.getOld());
					oldValues=(Map)JSONObject.toBean(jo, Map.class);
				}
				if(tb.equals(mainTableName)){
					BasicRow row =obj.getMainrow();
					row.addChangeLog(new ChangeLog(opTp,oldValues));
				}else{
					BasicRow row = obj.getSubrow(tb, ld.getTbRowId());
					//删除的履历在对象中不会加载，所以要判断row是否为空
					if(row!=null){
						row.addChangeLog(new ChangeLog(opTp,oldValues));
					}
				}
			}
		}
	}
	/**
	 * 
	 * @param objs
	 * @param logs
	 * @throws Exception
	 */
	public static void parse(Map<Long,BasicObj> objs,Map<Long,List<LogDetail>> logs)throws Exception{
		if(logs==null||logs.size()==0||objs==null||objs.size()==0){
			log.debug("parse nothing.");
			return;
		}
		for(Map.Entry<Long, List<LogDetail>> entry:logs.entrySet()){
			BasicObj obj = objs.get(entry.getKey());
			List<LogDetail> lds = entry.getValue();
			if(obj!=null&&lds!=null){
				parse(obj,lds);
			}
		}
	}
}
