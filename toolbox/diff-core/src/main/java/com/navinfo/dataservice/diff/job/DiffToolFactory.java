package com.navinfo.dataservice.diff.job;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

/** 
 * @ClassName: DiffToolFactory
 * @author xiaoxiaowen4127
 * @date 2017年8月31日
 * @Description: DiffToolFactory.java
 */
public class DiffToolFactory {
	private  Logger log = LoggerRepos.getLogger(this.getClass());
	private volatile static DiffToolFactory instance = null;
	public static DiffToolFactory getInstance(){
		if(instance==null){
			synchronized(DiffToolFactory.class){
				if(instance==null){
					instance = new DiffToolFactory();
				}
			}
		}
		return instance;
	}
	private DiffToolFactory(){
		
	}
	public DiffTool create(DiffJobRequest req)throws Exception{
		if(req.getDiffType()==1){//
			return new DiffByOracle(req);
		}else if(req.getDiffType()==2){//
			return new DiffByJava(req); 
		}
		return null;
	}
}
