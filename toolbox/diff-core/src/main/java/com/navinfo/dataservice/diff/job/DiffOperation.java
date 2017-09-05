package com.navinfo.dataservice.diff.job;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;

/** 
 * @ClassName: DiffOperation
 * @author xiaoxiaowen4127
 * @date 2017年8月31日
 * @Description: DiffOperation.java
 */
public class DiffOperation extends AbstractOperation {

	public DiffOperation(Connection conn, OperationResult preResult) {
		super(conn, preResult);
	}

	@Override
	public String getName() {
		return "DiffOperation";
	}

	@Override
	public void operate(AbstractCommand cmd) throws Exception {
		DiffOperationCommand mycmd = (DiffOperationCommand)cmd;
		Map<Long,BasicObj> leftObjs = mycmd.getLeftObjs();
		Map<Long,BasicObj> rightObjs = mycmd.getRightObjs();
		log.info("starting obj diff...");
		Collection<Long> unionPids = CollectionUtils.retainAll(leftObjs.keySet(), rightObjs.keySet());
		//新增
		for(Entry<Long,BasicObj> entry:leftObjs.entrySet()){
			BasicObj obj = entry.getValue();
			if(!unionPids.contains(entry.getKey())){//新增
				obj.getMainrow().setOpType(OperationType.INSERT);
				for(List<BasicRow> rows:obj.getSubrows().values()){
					for(BasicRow row:rows){
						row.setOpType(OperationType.INSERT);
					}
				}
				result.putObj(obj);
			}
		}
		for(Entry<Long,BasicObj> entry:rightObjs.entrySet()){
			BasicObj obj = entry.getValue();
			if(unionPids.contains(entry.getKey())){
				obj.diff(leftObjs.get(entry.getKey()), null);
			}else{
				obj.deleteObj();
			}
			result.putObj(obj);
		}
	}

}
