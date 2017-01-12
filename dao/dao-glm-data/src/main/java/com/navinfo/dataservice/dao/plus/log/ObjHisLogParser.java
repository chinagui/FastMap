package com.navinfo.dataservice.dao.plus.log;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.ChangeLog;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
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
		if(logs==null||objs==null){
			log.debug("parse nothing.");
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
