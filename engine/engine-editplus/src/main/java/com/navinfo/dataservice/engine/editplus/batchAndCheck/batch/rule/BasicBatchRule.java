package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchCommand;

public abstract class BasicBatchRule {
	private BatchCommand batchCommand;
	public List<String> objNameList=new ArrayList<String>();

	public BasicBatchRule() {
		// TODO Auto-generated constructor stub
	}
	
	public Map<Long, BasicObj> getRowList(){
		Map<Long, BasicObj> rows=new HashMap<Long, BasicObj>();
		//若全库批处理，则数据统一都是初始化状态，新增修改列表没有记录
		for(String objName:objNameList){
			if(this.getBatchCommand().getOperationResult().getAllObjsMap().containsKey(objName)){
				Map<Long, BasicObj> map=this.getBatchCommand().getOperationResult().getAllObjsMap().get(objName);
				if(map!=null){rows.putAll(map);}}
			}
		return rows;
	}
	
	public void run() throws Exception{
		for(String objName:this.objNameList){
			Map<Long, BasicObj> rows=getRowList();
				for(Long key:rows.keySet()){
					BasicObj obj=rows.get(key);
					runBatch(objName, obj);
				}
			}
		}
	
	public abstract void runBatch(String objName,BasicObj obj) throws Exception;

	public BatchCommand getBatchCommand() {
		return batchCommand;
	}

	public void setBatchCommand(BatchCommand batchCommand) {
		this.batchCommand = batchCommand;
	}

}
