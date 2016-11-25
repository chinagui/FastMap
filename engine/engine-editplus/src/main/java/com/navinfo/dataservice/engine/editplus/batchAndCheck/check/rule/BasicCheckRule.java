package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.CheckCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.CheckRule;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.NiValException;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.vividsolutions.jts.geom.Geometry;

public abstract class BasicCheckRule {
	private CheckCommand checkCommand;
	private CheckRule checkRule;
	public List<String> objNameList=new ArrayList<String>();
	List<NiValException> checkResultList=new ArrayList<NiValException>();

	public List<NiValException> getCheckResultList() {
		return checkResultList;
	}

	public void setCheckResultList(List<NiValException> checkResultList) {
		this.checkResultList = checkResultList;
	}

	public BasicCheckRule() {
		// TODO Auto-generated constructor stub
	}
	
	public Map<Long, BasicObj> getRowList(){
		Map<Long, BasicObj> rows=new HashMap<Long, BasicObj>();
		for(String objName:this.objNameList){
			//首先判断是否是按需批处理，检查对象是新增/修改对象
			if(this.getCheckCommand().getOperationResult().getAddObjs().containsKey(objName)){
				Map<Long, BasicObj> map=this.getCheckCommand().getOperationResult().getAddObjs().get(objName);
				if(map!=null){rows.putAll(map);}}
			if(this.getCheckCommand().getOperationResult().getUpdateObjs().containsKey(objName)){
				Map<Long, BasicObj> map=this.getCheckCommand().getOperationResult().getUpdateObjs().get(objName);
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
	
	public void run()throws Exception{
		for(String objName:this.objNameList){
			Map<Long, BasicObj> rows=getRowList();
				for(Long key:rows.keySet()){
					BasicObj obj=rows.get(key);
					runCheck(objName, obj);
				}
			}
		}
	
	public abstract void runCheck(String objName,BasicObj obj)throws Exception;

	public CheckCommand getCheckCommand() {
		return checkCommand;
	}

	public void setCheckCommand(CheckCommand checkCommand) {
		this.checkCommand = checkCommand;
	}
	
	public void setCheckResult(String loc, String targets,int meshId){
		NiValException checkResult=new NiValException(this.checkRule.getRuleId(), loc, targets, meshId,this.checkRule.getLog());
		this.checkResultList.add(checkResult);
	}
	
	public void setCheckResult(Geometry loc, String targets,int meshId) throws Exception{
		NiValException checkResult=new NiValException(this.checkRule.getRuleId(), loc, targets, meshId,this.checkRule.getLog());
		this.checkResultList.add(checkResult);
	}
	
	public void setCheckResult(String loc, String targets,int meshId,String log){
		NiValException checkResult=new NiValException(this.checkRule.getRuleId(), loc, targets, meshId,log);
		this.checkResultList.add(checkResult);
	}
	
	public void setCheckResult(Geometry loc, String targets,int meshId,String log) throws Exception{
		NiValException checkResult=new NiValException(this.checkRule.getRuleId(), loc, targets, meshId,log);
		this.checkResultList.add(checkResult);
	}

	public CheckRule getCheckRule() {
		return checkRule;
	}

	public void setCheckRule(CheckRule checkRule) {
		this.checkRule = checkRule;
	}

}
