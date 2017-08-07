package com.navinfo.dataservice.bizcommons.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.exception.ServiceException;

/** 
 * @ClassName: PidService
 * @author xiaoxiaowen4127
 * @date 2016年8月20日
 * @Description: PidService.java
 */
public class PidService implements Observer{
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	private volatile static PidService instance;
	public static PidService getInstance(){
		if(instance==null){
			synchronized(PidService.class){
				if(instance==null){
					instance=new PidService();
				}
			}
		}
		return instance;
	}
	private PidService(){
		refreshPidDataSource();
		SystemConfigFactory.getSystemConfig().addObserver(this);
	}
	ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private List<DataSource> pidDataSources=new ArrayList<DataSource>();
	
	private void refreshPidDataSource(){
		//
		rwl.writeLock().lock();
		try{
			pidDataSources.clear();
			String pids= SystemConfigFactory.getSystemConfig().getValue(PropConstant.pidServers);
			if(StringUtils.isEmpty(pids)){
				log.warn("******注意：PID Servers没有配置，PID服务将不可用******");
				return;
			}
			for(String pid:pids.split(";")){
				DbConnectConfig dc = DbConnectConfig.createConnectConfig(pid, "pidServer");
				pidDataSources.add(MultiDataSourceFactory.getInstance().getDataSource(dc));
			}
		}catch(Exception e){
			log.warn("刷新PID数据库服务列表失败，PID服务将不可用");
			log.warn(e.getMessage(),e);
		}finally{
			if(rwl.isWriteLockedByCurrentThread()){
				rwl.writeLock().unlock();
			}
		}
		
	}
	public long applyPid(String tableName,int count)throws ServiceException{
		//加读取锁
		rwl.readLock().lock();
		try{
			//生成随机顺序
			List<Integer> indexes = new ArrayList<Integer>();
			for(int i=0;i<pidDataSources.size();i++){
				indexes.add(i);
			}
			Collections.shuffle(indexes);
			//轮询申请，直到申请成功
			for(Integer i:indexes){
				long pid = 0;
				int total=0;
				while(pid==0&&total<100){
					pid = applyPidFromDb(i,tableName,count);
					total++;
					
				}
				if(pid>0){
					return pid;
				}
			}
			throw new ServiceException("所有的PID Server都不可用。");
		}finally{
			rwl.readLock().unlock();
		}
	}
	/**
	 * 不抛异常
	 * @param pidServerIndex
	 * @param tableName
	 * @param count
	 * @return
	 */
	private long applyPidFromDb(int pidServerIndex,String tableName,int count){
		Connection conn = null;
		CallableStatement cs = null;
		long pid=0L;
		try{
			String sql = "{? = call GLOBAL_ID_MAN.GET(?,?)}";
			conn = pidDataSources.get(pidServerIndex).getConnection();
			cs = conn.prepareCall(sql);
			cs.registerOutParameter(1,Types.BIGINT);
			cs.setString(2, tableName);
			cs.setInt(3, count);
			cs.execute();
			pid=cs.getLong(1);
			conn.commit();
		}catch(Exception e){
			log.error(e);
    		DbUtils.closeQuietly(cs);
			DbUtils.rollbackAndCloseQuietly(conn);
		}finally{
    		DbUtils.closeQuietly(cs);
    		DbUtils.closeQuietly(conn);
		}
		return pid;
	}
	

	@Override
	public void update(Observable o, Object arg) {
		
		refreshPidDataSource();
	}
	
}
