package com.navinfo.dataservice.engine.editplus.batchAndCheck.check;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule.BasicBatchRule;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule.BasicCheckRule;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchRule;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.CheckCommand;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.CheckRule;
import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.NiValException;

public class CheckExcuter {

	public CheckExcuter() {
		// TODO Auto-generated constructor stub
	}
	
	public List<NiValException> exeRule(CheckRule checkRule,CheckCommand checkCommand) throws Exception{
		List<NiValException> checkResult=new ArrayList<NiValException>();
		if(checkRule.getAccessorType().equals("JAVA")){
			checkResult=exeJavaRule(checkRule, checkCommand);
		}
		return checkResult;
	}
	
	public List<NiValException> exeJavaRule(CheckRule checkRule,CheckCommand checkCommand) throws Exception{
		BasicCheckRule ruleObj=(BasicCheckRule) checkRule.getAccessorClass().newInstance();
		ruleObj.setCheckCommand(checkCommand);
		ruleObj.run();
		return ruleObj.getCheckResultList();
	}

}
