package com.navinfo.dataservice.datahub.glm;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;

/** 
* @ClassName: GlmGridCalculatorFactory 
* @author Xiao Xiaowen 
* @date 2016年4月13日 下午6:42:06 
* @Description: TODO
*/
public class GlmGridCalculatorFactory {

	protected Logger log = LoggerRepos.getLogger(this.getClass());
	private static class SingletonHolder{
		private static final GlmGridCalculatorFactory INSTANCE = new GlmGridCalculatorFactory();
	}
	public static GlmGridCalculatorFactory getInstance(){
		return SingletonHolder.INSTANCE;
	}
	private Map<String,GlmGridCalculator> cache = new HashMap<String,GlmGridCalculator>();
	private LockInstance ins = new LockInstance();
	public GlmGridCalculator create(String gdbVersion){
		GlmGridCalculator calc  = cache.get(gdbVersion);
		if(calc==null){
			synchronized(this){
				calc = cache.get(gdbVersion);
				if(calc==null){
					calc = new GlmGridCalculator(gdbVersion,ins);
					cache.put(gdbVersion, calc);
				}
			}
		}
		return calc;
	}
	class LockInstance extends GlmGridCalculatorLock{
		
	}
}
