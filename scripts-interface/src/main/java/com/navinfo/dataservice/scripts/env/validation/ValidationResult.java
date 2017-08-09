package com.navinfo.dataservice.scripts.env.validation;

import java.util.ArrayList;
import java.util.List;

/** 
 * @ClassName: ValidationResult
 * @author xiaoxiaowen4127
 * @date 2017年8月7日
 * @Description: ValidationResult.java
 */
public class ValidationResult {
	protected List<String> errs=new ArrayList<>();
	public List<String> getErrs() {
		return errs;
	}
	public void setErrs(List<String> errs) {
		this.errs = errs;
	}
	public List<String> getWarnings() {
		return warnings;
	}
	public void setWarnings(List<String> warnings) {
		this.warnings = warnings;
	}
	protected List<String> warnings=new ArrayList<>();
}
