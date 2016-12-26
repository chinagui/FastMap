package com.navinfo.dataservice.engine.editplus.batchAndCheck.check;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule.BasicBatchRule;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule.BasicCheckRule;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchRuleCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchRule;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.CheckRuleCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.CheckRule;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.NiValException;

public class CheckExcuter {
	private static Logger log = Logger.getLogger(CheckExcuter.class);

	public CheckExcuter() {
		// TODO Auto-generated constructor stub
	}
	
	public List<NiValException> exeRule(CheckRule checkRule,CheckRuleCommand checkRuleCommand) throws Exception{
		List<NiValException> checkResult=new ArrayList<NiValException>();
		log.info("start run rule="+checkRule.getRuleId());
		try{
			if(checkRule.getAccessorType().equals("JAVA")){
				checkResult=exeJavaRule(checkRule, checkRuleCommand);
			}
		}catch(Exception e){
			log.error("error run rule="+checkRule.getRuleId(),e);
		}
		log.info("end run rule="+checkRule.getRuleId());
		return checkResult;
	}
	
	public List<NiValException> exeJavaRule(CheckRule checkRule,CheckRuleCommand checkRuleCommand) throws Exception{
		BasicCheckRule ruleObj=(BasicCheckRule) checkRule.getAccessorClass().newInstance();
		ruleObj.setCheckRuleCommand(checkRuleCommand);
		ruleObj.setCheckRule(checkRule);
		ruleObj.run();
		return ruleObj.getCheckResultList();
	}

}
