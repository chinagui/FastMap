package com.navinfo.dataservice.engine.limit.datahub.chooser;

import com.navinfo.dataservice.engine.limit.datahub.model.DbServer;
import com.navinfo.dataservice.engine.limit.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.engine.limit.datahub.DbServerMonitor;
import com.navinfo.dataservice.engine.limit.datahub.chooser.strategy.DbServerStrategyFactory;
import com.navinfo.dataservice.engine.limit.datahub.exception.DataHubException;
import com.navinfo.dataservice.engine.limit.datahub.service.DbServerService;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** 
 * @ClassName: DbChooser 
 * @author Xiao Xiaowen 
 * @date 2015-11-30 下午3:00:24 
 * @Description: TODO
 */
public class DbServerChooser implements Observer{
	protected Logger log = Logger.getLogger(this.getClass());
	private static class SingletonHolder{
		private static final DbServerChooser INSTANCE = new DbServerChooser();
	}
	public static final DbServerChooser getInstance(){
		return SingletonHolder.INSTANCE;
	}
	private DbServerService dbServerMan;
	private Map<String,List<DbServer>> dbServerMap=new ConcurrentHashMap<String,List<DbServer>>();//key:the use type of a dbserver
	private DbServerChooser(){
		dbServerMan = new DbServerService();
		DbServerMonitor.getInstance().addObserver(this);
	}
	private synchronized void loadDbServers(){
		dbServerMap.clear();
		try{
			List<DbServer> dbServerList = dbServerMan.loadDbServers();
			if(dbServerList!=null){
				for(DbServer server:dbServerList){
					Set<String> types = server.getBizType();
					for(String type:types){
						if(dbServerMap.keySet().contains(type)){
							List<DbServer> dbSerList = dbServerMap.get(type);
							dbSerList.add(server);
						}else{
							List<DbServer> dbSerList = new ArrayList<DbServer>();
							dbSerList.add(server);
							dbServerMap.put(type, dbSerList);
						}
					}
				}
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
			log.warn("******重要：加载DbServer表出现错误，需检查错误，重新启动服务。******");
		}
	}
	public DbServer getOnlyOneDbServer(String bizType)throws DataHubException{
		if("true".equals(SystemConfigFactory.getSystemConfig().getValue("dbserver.cache.enable"))){
			List<DbServer> serList = dbServerMap.get(bizType);
			if(serList==null||serList.isEmpty()){
				throw new DataHubException("db server表中未配置该use type："+bizType+",无法获取该类型的db server");
			}
			if(serList.size()>1){
				throw new DataHubException("useType："+bizType+"所配置的server不唯一，不能使用唯一查询或者是db server表配置错误。");
			}else{
				return serList.get(0);
			}
		}else{
			//暂未实现
			return null;
		}
	}
	public DbServer getPriorDbServer(String bizType,String strategyType,Map<String,Object> strategyParamMap)throws DataHubException{
		if("true".equals(SystemConfigFactory.getSystemConfig().getValue("dbserver.cache.enable"))){
			List<DbServer> serList = dbServerMap.get(bizType);
			if(serList==null||serList.isEmpty()){
				throw new DataHubException("db server表中未配置该use type："+bizType+",无法获取该类型的db server");
			}
			return DbServerStrategyFactory.getInstance().create(strategyType).getPriorDbServer(serList,strategyParamMap);
		}else{
			return DbServerStrategyFactory.getInstance().create(strategyType).getPriorDbServer(bizType,strategyParamMap);
		}
	}
	@Override
	public void update(Observable o, Object arg){
		try{
			loadDbServers();
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
	}
}
