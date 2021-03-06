package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
//import sun.tools.tree.ThisExpression;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.CheckRuleCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.CheckRule;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.NiValException;
import com.vividsolutions.jts.geom.Geometry;

public abstract class BasicCheckRule {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	private CheckRuleCommand checkRuleCommand;
	private CheckRule checkRule;
	List<NiValException> checkResultList=new ArrayList<NiValException>();
	public Map<String,Map<Long, BasicObj>> myReferDataMap=new HashMap<String, Map<Long,BasicObj>>();

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
		loadReferDatas(rows.values());
		for(Long key:rows.keySet()){
			BasicObj obj=rows.get(key);
			if(!obj.getMainrow().getOpType().equals(OperationType.PRE_DELETED)){
				try{
					runCheck(obj);
				}catch(Exception e){
					log.warn(e.getMessage(),e);
				}
			}
		}
	}
	
	public abstract void runCheck(BasicObj obj)throws Exception;
	
	public abstract void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception;

	public CheckRuleCommand getCheckRuleCommand() {
		return checkRuleCommand;
	}

	public void setCheckRuleCommand(CheckRuleCommand checkRuleCommand) {
		this.checkRuleCommand = checkRuleCommand;
	}
	
	public void setCheckResult(String loc, String targets,int meshId){
		NiValException checkResult=new NiValException(checkRule.getRuleId(), loc, targets, meshId,checkRule.getLog(),checkRule.getRuleLevel());
		//splitTargets(targets);
		this.checkResultList.add(checkResult);
	}
	
	public void setCheckResult(Geometry loc, String targets,int meshId) throws Exception{
		NiValException checkResult=new NiValException(checkRule.getRuleId(), loc, targets, meshId,checkRule.getLog(),checkRule.getRuleLevel());
		//splitTargets(targets);
		this.checkResultList.add(checkResult);
	}
	/**
	 * 根据对象，组成检查log；参数log可为null，若为null，则取规则的log描述作为检查log。
	 * @param obj
	 * @param log 可为null
	 * @throws Exception
	 */
	public void setCheckResult(BasicObj obj, String log) throws Exception{
		if(log==null || log.isEmpty()){log=this.checkRule.getLog();}
		String targets="["+obj.getMainrow().tableName()+","+obj.objPid()+"]";
		NiValException checkResult=new NiValException(checkRule.getRuleId(), "", targets,0,log,checkRule.getRuleLevel());
		//splitTargets(targets);
		this.checkResultList.add(checkResult);
	}
	/**
	 * 根据对象，组成检查log；参数log可为null，若为null，则取规则的log描述作为检查log。
	 * 增加坐标的传参，减少后期二次查库。
	 * @param geometry
	 * @param obj
	 * @param log 可为null
	 * @throws Exception
	 */
	public void setCheckResult(Geometry geometry, BasicObj obj,int meshId,String log) throws Exception {
		if(log==null || log.isEmpty()){log=this.checkRule.getLog();}
		String targets="["+obj.getMainrow().tableName()+","+obj.objPid()+"]";
		NiValException checkResult=new NiValException(checkRule.getRuleId(), geometry, targets,meshId,log,checkRule.getRuleLevel());
		//splitTargets(targets);
		this.checkResultList.add(checkResult);
	}
	
	public void setCheckResult(String loc, String targets,int meshId,String log){
		if(log==null || log.isEmpty()){log=this.checkRule.getLog();}
		NiValException checkResult=new NiValException(checkRule.getRuleId(), loc, targets, meshId,log,checkRule.getRuleLevel());
		//splitTargets(targets);
		this.checkResultList.add(checkResult);
	}
	
	public void setCheckResult(Geometry loc, String targets,int meshId,String log) throws Exception{
		if(log==null || log.isEmpty()){log=this.checkRule.getLog();}
		NiValException checkResult=new NiValException(checkRule.getRuleId(), loc, targets, meshId,log,checkRule.getRuleLevel());
		//splitTargets(targets);
		this.checkResultList.add(checkResult);
	}
	
	/**
	 * targets拆分后存入list,主要用于poi精编重分类，目前仅支持poi类。
	 * 放在check框架最外层处理，以便排除检查例外数据
	 * @param targets [IX_POI,123];[IX_POI,24]
	 * {IX_POI:{PID,[RULE1,RULE2]}}
	 */
	/*private void splitTargets(String targets){
		GlmObject glmObject = GlmFactory.getInstance().getObjByType(ObjectName.IX_POI);
		String mainTableName=glmObject.getMainTable().getName();
		String value=StringUtils.removeBlankChar(targets);
		if (value != null && value.length() > 2) {
			String subValue = value.substring(1, value.length() - 1);
			for (String table : subValue.split("\\];\\[")) {
				String[] arr = table.split(",");
				String tableName= arr[0];
				String pid=arr[1];
				if(mainTableName.equals(tableName)){
					this.checkRuleCommand.setErrorPidRuleMap(ObjectName.IX_POI, Long.valueOf(pid), 
							this.checkRule.getRuleId());
				}
			}
		}
	}*/

	public CheckRule getCheckRule() {
		return checkRule;
	}

	public void setCheckRule(CheckRule checkRule) {
		this.checkRule = checkRule;
	}

}
