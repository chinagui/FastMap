package com.navinfo.dataservice.dao.plus.selector;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.glm.GlmTableNotFoundException;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;

/** 
 * @ClassName: oBJ
 * @author xiaoxiaowen4127
 * @date 2016年11月25日
 * @Description: oBJ.java
 */
public class ObjChildrenIncreSelector {
	
	/**
	 * 
	 * @param objs:key:对象
	 * @param selConfig
	 * @throws Exception
	 */
	public static void increSelect(Connection conn,Map<String,Map<Long,BasicObj>> objs,Map<String,Set<String>> selConfig)throws Exception{
		if(objs==null) return;
		if(selConfig==null) return;
		for(Map.Entry<String, Map<Long,BasicObj>> entry:objs.entrySet()){
			increSelect(conn,entry.getValue(),selConfig.get(entry.getKey()));
		}
	}
	
	public static void increSelect(Connection conn,Map<Long,BasicObj> objs,Set<String> selConfig) throws GlmTableNotFoundException, SQLException{
		
		if(objs!=null&&objs.size()>0&&selConfig!=null&&selConfig.size()>0){
			//逐张子表
			for(String tab:selConfig){
				List<BasicObj> myObjs = new ArrayList<BasicObj>();
				List<Long> pids = new ArrayList<Long>();
				//逐个对象
				for(Map.Entry<Long, BasicObj> entry:objs.entrySet()){
					BasicObj obj = entry.getValue();
					if((obj.getMainrow().getOpType().equals(OperationType.UPDATE)||obj.getMainrow().getOpType().equals(OperationType.DELETE))
							&&obj.getRowsByName(tab)==null){
						myObjs.add(obj);
						pids.add(entry.getKey());
					}
				}
				//sel
				ObjBatchSelector.selectChildren(conn, myObjs, tab, pids);
			}
		}
	}
}
