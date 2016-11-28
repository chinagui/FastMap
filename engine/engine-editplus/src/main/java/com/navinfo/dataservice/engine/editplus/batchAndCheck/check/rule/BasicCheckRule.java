package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import sun.tools.tree.ThisExpression;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchRuleCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.CheckRuleCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.CheckRule;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.NiValException;
import com.vividsolutions.jts.geom.Geometry;

public abstract class BasicCheckRule {
	protected Logger log = Logger.getLogger(this.getClass());
	private CheckRuleCommand checkRuleCommand;
	private CheckRule checkRule;
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
		//若全库批处理，则数据统一都是初始化状态，新增修改列表没有记录
		for(String objName:checkRule.getObjNameSet()){
			if(checkRuleCommand.getAllDatas().containsKey(objName)){
				Map<Long, BasicObj> map=checkRuleCommand.getAllDatas().get(objName);
				if(map!=null){rows.putAll(map);}}
			}
		return rows;
	}
	
	public void run()throws Exception{
		Map<Long, BasicObj> rows=getRowList();
		for(Long key:rows.keySet()){
			BasicObj obj=rows.get(key);
			if(!obj.getMainrow().getOpType().equals(OperationType.PRE_DELETED)){
				runCheck(obj);}
		}
	}
	
	public abstract void runCheck(BasicObj obj)throws Exception;

	public CheckRuleCommand getCheckRuleCommand() {
		return checkRuleCommand;
	}

	public void setCheckRuleCommand(CheckRuleCommand checkRuleCommand) {
		this.checkRuleCommand = checkRuleCommand;
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
