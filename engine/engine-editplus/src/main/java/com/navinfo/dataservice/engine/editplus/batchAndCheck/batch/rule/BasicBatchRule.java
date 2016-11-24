package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchCommand;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;

public abstract class BasicBatchRule {
	private BatchCommand batchCommand;
	public List<String> objNameList=new ArrayList<String>();

	public BasicBatchRule() {
		// TODO Auto-generated constructor stub
	}
	
	public Map<Long, BasicObj> getRowList(){
		Map<Long, BasicObj> rows=new HashMap<Long, BasicObj>();
		for(String objName:this.objNameList){
			//首先判断是否是按需批处理，检查对象是新增/修改对象
			if(this.getBatchCommand().getOperationResult().getAddObjs().containsKey(objName)){
				Map<Long, BasicObj> map=this.getBatchCommand().getOperationResult().getAddObjs().get(objName);
				if(map!=null){rows.putAll(map);}}
			if(this.getBatchCommand().getOperationResult().getUpdateObjs().containsKey(objName)){
				Map<Long, BasicObj> map=this.getBatchCommand().getOperationResult().getUpdateObjs().get(objName);
				if(map!=null){rows.putAll(map);}}
			}
		if(rows.isEmpty()){
			//若全库批处理，则数据统一都是初始化状态，新增修改列表没有记录
			for(String objName:objNameList){
				//
				}
		}
		return rows;
	}
	
	public void run(){
		for(String objName:this.objNameList){
			Map<Long, BasicObj> rows=getRowList();
				for(Long key:rows.keySet()){
					BasicObj obj=rows.get(key);
					runBatch(objName, obj);
				}
			}
		}
	
	public abstract void runBatch(String objName,BasicObj obj);

	public BatchCommand getBatchCommand() {
		return batchCommand;
	}

	public void setBatchCommand(BatchCommand batchCommand) {
		this.batchCommand = batchCommand;
	}

}
