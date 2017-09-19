package com.navinfo.dataservice.engine.limit.datahub.chooser.strategy;

import com.navinfo.dataservice.engine.limit.datahub.exception.DataHubException;

import java.util.HashMap;
import java.util.Map;

/** 
 * @ClassName: DbServerStrategyFactory 
 * @author Xiao Xiaowen 
 * @date 2015-12-01 上午9:00:24 
 * @Description: TODO
 */
public class DbServerStrategyFactory {
	private static class SingletonHolder{
		private static final DbServerStrategyFactory INSTANCE =new DbServerStrategyFactory();
	}
	public static DbServerStrategyFactory getInstance(){
		return SingletonHolder.INSTANCE;
	}
	private DbServerStrategyFactory(){
		strategyLock = new StrategyLock();
	}
	private StrategyLock strategyLock=null;
	private Map<String,DbServerStrategy> strategyMap=new HashMap<String,DbServerStrategy>();
	public synchronized DbServerStrategy create(String type)throws DataHubException{
		if(strategyMap.containsKey(type)){
			return strategyMap.get(type);
		}else{
			DbServerStrategy strategy = null;
			if(DbServerStrategy.RANDOM.equals(type)){
				strategy = new RandomStrategy(strategyLock);
			}else if(DbServerStrategy.USE_REF_DB.equals(type)){
				strategy = new UseRefDbStrategy(strategyLock);
			}else if(DbServerStrategy.USE_SPEC_SVR.equals(type)){
				strategy = new UseSpecSvrStrategy(strategyLock);
			}
			else{
				throw new DataHubException("不支持的dbServerStrategy类型："+type);
			}
			strategyMap.put(type, strategy);
			return strategy;
		}
	}
	class StrategyLock extends AbstractStrategyLock{
		public void lock(){
			//do nothing
		}
	}
}
