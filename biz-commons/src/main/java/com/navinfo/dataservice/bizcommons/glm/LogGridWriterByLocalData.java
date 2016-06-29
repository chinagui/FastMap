package com.navinfo.dataservice.bizcommons.glm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceRtException;
import com.navinfo.navicommons.exception.ThreadExecuteException;

/** 
* @ClassName: LogGridWrite 
* @author Xiao Xiaowen 
* @date 2016年6月28日 下午4:18:26 
* @Description: TODO
*  
*/
public class LogGridWriterByLocalData {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected OracleSchema logSchema;

	protected LogGridCalculatorByLocalData calc;
	public LogGridWriterByLocalData(OracleSchema logSchema){
		this.logSchema=logSchema;
		this.calc = new LogGridCalculatorByLocalData(logSchema);
	}
	public void write(final boolean fillOldGrids,final boolean fillNewGrids)throws Exception{
		//1. 确定tables
		Collection<GlmTable> logTables = new ArrayList<GlmTable>();
		Connection conn = null;
		try{
			conn = logSchema.getPoolDataSource().getConnection();
			String sql = "SELECT DISTINCT TB_NM FROM LOG_DETAIL";
			Collection<String> tables = new QueryRunner().query(conn, sql, new ResultSetHandler<Collection<String>>(){

				@Override
				public Collection<String> handle(ResultSet rs) throws SQLException {
					Collection<String> results = new HashSet<String>();
					while(rs.next()){
						results.add(rs.getString("TB_NM"));
					}
					return results;
				}
				
			});
			String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
			Glm glm = GlmCache.getInstance().getGlm(gdbVersion);
			for(String key:tables){
				logTables.add(glm.getGlmTable(key));
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			DbUtils.closeQuietly(conn);
		}
		write(logTables,fillOldGrids,fillNewGrids);
	}
	public void write(Collection<GlmTable> logTables,final boolean fillOldGrids,final boolean fillNewGrids)throws Exception{
		if(logTables==null)return;
		if(logTables.size()>1){
			//多线程
			VMThreadPoolExecutor logGridPoolExecutor;
			//初始线程池
			int poolSize = 10;
			try {
				logGridPoolExecutor = new VMThreadPoolExecutor(poolSize, poolSize, 3,
						TimeUnit.SECONDS, new LinkedBlockingQueue(),
						new ThreadPoolExecutor.CallerRunsPolicy());
			} catch (Exception e) {
				throw new ServiceRtException("初始化线程池错误:" + e.getMessage(), e);
			}
			
			//计算
			final CountDownLatch latch4LogGrid = new CountDownLatch(logTables.size());
			logGridPoolExecutor.addDoneSignal(latch4LogGrid);
			// 
			log.debug("开始填充履历grid号");
			long t = System.currentTimeMillis();
			for (final GlmTable table : logTables) {
				log.debug("添加填充履历grid号执行线程，表名为：" + table.getName());
				logGridPoolExecutor.execute(new Runnable() {
					@Override
					public void run() {
						try{
							calc.calc(table,fillOldGrids,fillNewGrids);
							latch4LogGrid.countDown();
							log.debug("填充履历grid号完成，表名为：" + table.getName());
						}catch(Exception e){
							throw new ThreadExecuteException("表名："+table.getName()+"差分失败。",e);
						}
					}
				});
			}
			try {
				log.debug("等待各计算履历grid号任务执行完成");
				latch4LogGrid.await();
			} catch (InterruptedException e) {
				log.warn("线程被打断");
			}
			if (logGridPoolExecutor.getExceptions().size() > 0)
				throw new ServiceRtException("计算履历grid号时发生异常", logGridPoolExecutor
						.getExceptions().get(0));
			log.debug("各计算履历grid号任务执行完成,用时：" + (System.currentTimeMillis() - t) + "ms");
			//关闭线程池
			log.debug("关闭计算履历grid号的线程池");
			if (logGridPoolExecutor != null && !logGridPoolExecutor.isShutdown()) {
				logGridPoolExecutor.shutdownNow();
				try {
					while (!logGridPoolExecutor.isTerminated()) {
						log.debug("等待线程结束：线程数为" + logGridPoolExecutor.getActiveCount());
						Thread.sleep(2000);
					}
				} catch (InterruptedException e) {
					log.error("关闭计算履历grid号的线程池");
					throw new ServiceRtException("关闭计算履历grid号的线程池", e);
				}
			}
		}else{
			for(GlmTable table:logTables){
				calc.calc(table, fillOldGrids, fillNewGrids);
			}
		}
	}
}
