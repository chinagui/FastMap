package com.navinfo.dataservice.diff;

import com.navinfo.dataservice.diff.config.DiffConfig;
import com.navinfo.dataservice.datahub.glm.Glm;
import com.navinfo.dataservice.datahub.glm.GlmCache;
import com.navinfo.dataservice.datahub.glm.GlmTable;
import com.navinfo.dataservice.datahub.manager.DbManager;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.dataservice.diff.dataaccess.CrossSchemaDataAccess;
import com.navinfo.dataservice.diff.dataaccess.DataAccess;
import com.navinfo.dataservice.diff.dataaccess.LocalDataAccess;
import com.navinfo.dataservice.diff.exception.InitException;
import com.navinfo.dataservice.diff.scanner.JavaDiffScanner;
import com.navinfo.dataservice.commons.job.AbstractJobResponse;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.PackageExec;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.exception.ThreadExecuteException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 12-3-8 下午4:33
 */
public class DiffEngine 
{
	private  Logger log = Logger
			.getLogger(DiffEngine.class);

	private DiffConfig diffConfig;
	private DataAccess leftAccess;
	private DataAccess rightAccess;
	private Set<GlmTable> diffTables;
	protected VMThreadPoolExecutor diffPoolExecutor;
	protected VMThreadPoolExecutor logPoolExecutor;
	protected JavaDiffScanner diffScanner;


	public DiffEngine(DiffConfig diffConfig) {
		this.diffConfig = diffConfig;
	}

	public void initEngine()throws InitException{
		//glmcache中没发现表的主键，使用row_id做主键
		try{
			//shcema
			DbManager dbMan = new DbManager();
			OracleSchema leftSchema = (OracleSchema)dbMan.getDbById(diffConfig.getLeftDbId());
			OracleSchema rightSchema = (OracleSchema)dbMan.getDbById(diffConfig.getRightDbId());
			//安装EQUALS
			installPcks(leftSchema);
			grantPrivilege(leftSchema);
			//赋权限
			
			//data access
			leftAccess = new LocalDataAccess(leftSchema);
			rightAccess = new CrossSchemaDataAccess(rightSchema);
			
			//diffScanner
			diffScanner = new JavaDiffScanner(leftSchema);
			//diffTables
			diffTables = new HashSet<GlmTable>();
			Glm glm = GlmCache.getInstance().getGlm(diffConfig.getGdbVersion());
			
			List<String> specific = diffConfig.getSpecificTables();
			List<String> excluded = diffConfig.getExcludedTables();
			if(specific!=null&&specific.size()>0){
				for(String name:specific){
					diffTables.add(glm.getTables().get(name));
				}
			}else{
				if(excluded!=null&&excluded.size()>0){
					for(GlmTable table:glm.getTables().values()){
						if(!excluded.contains(table.getName())){
							diffTables.add(table);
						}
					}
				}else{
					diffTables.addAll(glm.getTables().values());
				}
			}
			log.debug("需要差分的表的个数为：" + diffTables.size());
			
			initPoolExecutor();
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new InitException("差分初始化过程出错:"+e.getMessage(),e);
		}
	}
	
	private void installPcks(OracleSchema schema)throws InitException{
		Connection conn = null;
		try{
			conn = schema.getPoolDataSource().getConnection();
			//安装EQUALS
			String afle = "/com/navinfo/dataservice/diff/resources/equals.pck";
			PackageExec packageExec = new PackageExec(conn);
			packageExec.execute(afle);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			try{
				DbUtils.rollback(conn);
			}catch(Exception err){
				log.error(err);
			}
			throw new InitException("安装差分需要的包是发生错误:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	private void grantPrivilege(OracleSchema schema)throws InitException{
		Connection conn= null;
		try{
			OracleSchema superSchema = (OracleSchema)schema.getSuperDb();
			String sql = "GRANT SELECT ANY TABLE TO "+schema.getDbUserName();
			QueryRunner runner = new QueryRunner();
			conn = superSchema.getDriverManagerDataSource().getConnection();
			runner.execute(conn, sql);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new InitException("安装差分需要的包是发生错误:"+e.getMessage(),e);
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}

	public DiffResponse execute() {
		DiffResponse res = new DiffResponse();
		try {
			initEngine();
			diffScan();
			fillLogDetail();
			res.setStatusAndMsg(AbstractJobResponse.STATUS_SUCCESS, "Success");
		}catch(Exception e){
			res.setStatusAndMsg(AbstractJobResponse.STATUS_FAILED,"ERROR:"+e.getMessage());
			log.error(e.getMessage(), e);
		}finally {
			shutDownPoolExecutor();
		}
		return res;
	}


	/**
	 * 执行差分扫描
	 */
	protected void diffScan() {
		final CountDownLatch latch = new CountDownLatch(diffTables.size());
		diffPoolExecutor.addDoneSignal(latch);
		// 执行差分
		log.debug("开始执行差分");
		long t = System.currentTimeMillis();
		for (final GlmTable table : diffTables) {
			log.debug("添加差分线程，表名为：" + table.getName());
			diffPoolExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try{
						diffScanner.scan(table,leftAccess.accessTable(table), rightAccess.accessTable(table));
						latch.countDown();
						log.debug("差分完成，表名为：" + table.getName());
					}catch(Exception e){
						throw new ThreadExecuteException("表名："+table.getName()+"差分失败。",e);
					}
				}
			});
		}
		try {
			log.debug("等待各差分任务执行完成");
			latch.await();
		} catch (InterruptedException e) {
			log.warn("线程被打断");
		}
		if (diffPoolExecutor.getExceptions().size() > 0)
			throw new ServiceException("执行差分时发生异常"+diffPoolExecutor
					.getExceptions().get(0).getMessage(), diffPoolExecutor
					.getExceptions().get(0));
		log.debug("所有表差分完成,用时：" + (System.currentTimeMillis() - t) + "ms");
	}

	protected void fillLogDetail() {
		final CountDownLatch latch4Log = new CountDownLatch(diffTables.size());
		logPoolExecutor.addDoneSignal(latch4Log);
		// 
		log.debug("开始填充履历详细改前改后值");
		long t = System.currentTimeMillis();
		for (final GlmTable table : diffTables) {
			log.debug("添加填充履历执行线程，表名为：" + table.getName());
			logPoolExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try{
						diffScanner.fillLogDetail(table,leftAccess.accessTable(table), rightAccess.accessTable(table));
						latch4Log.countDown();
						log.debug("填充履历完成，表名为：" + table.getName());
					}catch(Exception e){
						throw new ThreadExecuteException("表名："+table.getName()+"差分失败。",e);
					}
				}
			});
		}
		try {
			log.debug("等待各生成履历任务执行完成");
			latch4Log.await();
		} catch (InterruptedException e) {
			log.warn("线程被打断");
		}
		if (logPoolExecutor.getExceptions().size() > 0)
			throw new ServiceException("执行生成履历时发生异常", logPoolExecutor
					.getExceptions().get(0));
		log.debug("各生成履历任务执行完成,用时：" + (System.currentTimeMillis() - t) + "ms");
	}


	protected void initPoolExecutor() {
		// int poolSize = config.getThreadCount();
		int poolSize = 10;
		try {
			diffPoolExecutor = new VMThreadPoolExecutor(poolSize, poolSize, 3,
					TimeUnit.SECONDS, new LinkedBlockingQueue(),
					new ThreadPoolExecutor.CallerRunsPolicy());
			logPoolExecutor = new VMThreadPoolExecutor(poolSize, poolSize, 3,
					TimeUnit.SECONDS, new LinkedBlockingQueue(),
					new ThreadPoolExecutor.CallerRunsPolicy());
		} catch (Exception e) {
			throw new ServiceException("初始化线程池错误:" + e.getMessage(), e);
		}
	}

	protected void shutDownPoolExecutor() {
		log.debug("关闭线程池");
		if (diffPoolExecutor != null && !diffPoolExecutor.isShutdown()) {
			diffPoolExecutor.shutdownNow();
			try {
				while (!diffPoolExecutor.isTerminated()) {
					log.debug("等待线程结束：线程数为" + diffPoolExecutor.getActiveCount());
					Thread.sleep(2000);
				}
			} catch (InterruptedException e) {
				log.error("关闭线程池失败");
				throw new ServiceException("关闭线程池失败", e);
			}
		}
		if (logPoolExecutor != null && !logPoolExecutor.isShutdown()) {
			logPoolExecutor.shutdownNow();
			try {
				while (!logPoolExecutor.isTerminated()) {
					log.debug("等待线程结束：线程数为" + logPoolExecutor.getActiveCount());
					Thread.sleep(2000);
				}
			} catch (InterruptedException e) {
				log.error("关闭线程池失败");
				throw new ServiceException("关闭线程池失败", e);
			}
		}
	}

}
