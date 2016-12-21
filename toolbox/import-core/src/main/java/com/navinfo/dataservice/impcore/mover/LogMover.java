package com.navinfo.dataservice.impcore.mover;

import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.navicommons.database.QueryRunner;

/** 
* @ClassName: LogMover 
* @author Xiao Xiaowen 
* @date 2016年6月23日 下午1:47:12 
* @Description: TODO
*  
*/
public abstract class LogMover {
	protected OracleSchema logSchema;
	protected OracleSchema tarSchema;
	protected QueryRunner run;
	public LogMover(OracleSchema logSchema,OracleSchema tarSchema){
		this.logSchema=logSchema;
		this.tarSchema=tarSchema;
		run = new QueryRunner();
	}
	public abstract LogMoveResult move()throws Exception;
	public void rollbackMove() throws Exception{
		//do nothing
	}
}
