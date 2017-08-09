package com.navinfo.dataservice.scripts.env.validation;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.scripts.env.validation.model.OracleDbType;
import com.navinfo.dataservice.scripts.env.validation.model.ValidationType;
import org.apache.log4j.Logger;

/**
 * @ClassName: FosEnvValidationExecutor
 * @author xiaoxiaowen4127
 * @date 2017年8月7日
 * @Description: FosEnvValidationExecutor.java
 */
public class FosEnvValidationExecutor {
	private static Logger log = LoggerRepos.getLogger(FosEnvValidationExecutor.class);

	public static void main(String[] args) throws Exception{
		long start = System.currentTimeMillis();

		String type = args[0];
		ValidationType validationType = ValidationType.valueOf(type.toUpperCase());

		FosEnvValidation validation = null;

		switch (validationType){
			case ORACLE:
				validation = new OracleDbValidation();
				break;
			default:
				log.info("validation type not support:"+type);
				System.exit(-1);
		}
		long end = System.currentTimeMillis();


		ValidationResult result = validation.validation();
		log.info("validation result:");
		for(String err:result.getErrs()){
			log.info(err);
		}
		for(String warning:result.getWarnings()){
			log.info(warning);
		}
		log.info("validation end, time "+(end-start) + "ms");
	}
}
