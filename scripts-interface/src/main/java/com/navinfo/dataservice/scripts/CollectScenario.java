package com.navinfo.dataservice.scripts;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.navicommons.database.QueryRunner;

/**
 * 采集场景专题图脚本
 * 总原则见下： 1.道路等级为1-2级的link，全部定义为“车采”
 *          2.道路等级为3-9级，同时满足【步采原则】中的任一条件的数据，定义为“步采”，其它均为“车采”
 *          3.10级以上道路不考虑车采和步采，根据原有道路等级渲染
 *          4.步采原则：
 *          (1)道路属性包含步行街
 *          (2)每100米link，覆盖的POI数量大于等于10个
 *          (3)每100米link，覆盖的POI数量小于10个，且覆盖的重要POI大于等于3个
 *          (4)每100米link，覆盖的POI数量小于10个，且覆盖的地址POI大于等于5个
 *  说明：        1.每根link覆盖POI的计算原则：link两侧各30米范围内，覆盖的POI数量；
 *          2.每100米link覆盖的POI数量：每根link覆盖POI数量除以link长度(单位：米)*100
 *          3.重要POI：A级POI+重要POI一览表【同外业规划原则】
 *          4.地址POI：POI存在地址，即关联IX_POI_ADDRESS表存在记录
 *          
 *          在单例类中定义一个ThreadLocal对象用于存储这个类本身的对象，当线程A调用MyThreadScopeData getThreadInstance()方法获取MyThreadScopeData对象的时候，
 *          首先从ThreadLocal中获取保存在线程A中的MyThreadScopeData对象，如果没有，则创建一个新的MyThreadScopeData对象，将此对象保存在ThreadLocal对象中，
 *          然后返回MyThreadScopeData 对象；当线程B调用MyThreadScopeData getThreadInstance()方法获取MyThreadScopeData 对象的时候,
 *          流程和线程A调用MyThreadScopeData getThreadInstance()方法一样。此时线程A和线程B各自保存和操作自己线程范围内的MyThreadScopeData对象和数据。
 *          这就做到了线程内部的数据是共享的，而线程与线程之间的数据是独立的
 *          
 *          线程池任务缓存队列及排队策略
 *          ArrayBlockingQueue：基于数组的先进先出队列
 *          
 * */
public class CollectScenario{
	
	//默认值
	private static int normalPoiCount = 10;
	private static int importantPoiCount = 3;
	private static int addressPoiCount = 5;
	
	private static Logger log = LoggerRepos.getLogger(CollectScenario.class);
	
	private CollectScenario(){}  
    //单例设计模式  
    //定义ThreadLocal  
    private static ThreadLocal<CollectScenario> map = new ThreadLocal<CollectScenario>();  
    public static CollectScenario getThreadInstance(){  
    	CollectScenario instance = map.get();  
        if(instance ==null){  
            instance = new CollectScenario();  
            map.set(instance);  
        }  
        return instance;  
    }  

	public static void main(String[] args) throws Exception {
		Connection conn = null;
		try{
			JobScriptsInterface.initContext();
			conn = DBConnector.getInstance().getManConnection();
			List<Integer> dailyDBIds = queryDbId(conn);
			if(dailyDBIds.size() == 0){
				throw new Exception("未查询到对应的日库ID");
			}
			//处理传参数据
			convertInputDataToMap(args);
			
	        ThreadPoolExecutor executor = new ThreadPoolExecutor(12, 20, 200, TimeUnit.MILLISECONDS,
	                 new ArrayBlockingQueue<Runnable>(20));
			
			for(int dailyDBId : dailyDBIds){
	        
				MyTask myTask = new MyTask(dailyDBId);
	            executor.execute(myTask);
	            log.info("线程池中线程数目："+executor.getPoolSize()+"，队列中等待执行的任务数目："+
	            executor.getQueue().size()+"，已执行完别的任务数目："+executor.getCompletedTaskCount());
			}
			executor.shutdown();
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}finally{
			DbUtils.closeQuietly(conn);
		}

	}
	
	/**
	 * 处理传参的值,传参有异常按照默认值处理
	 * @param String[] args
	 * @return Map<String, String>
	 * 
	 * */
	public static void convertInputDataToMap(String[] args){
		Map<String, String> inputMap = new HashMap<String, String>();
		try{
			if(args.length > 0){
			if (args.length % 2 != 0) {
				throw new Exception("传参有误");
			}
			for (int i = 0; i < args.length; i += 2) {
				inputMap.put(args[i], args[i + 1]);
			}
			normalPoiCount = Integer.parseInt(inputMap.get("normalPoiNumber"));
			importantPoiCount = Integer.parseInt(inputMap.get("importantPoiNumber"));
			addressPoiCount = Integer.parseInt(inputMap.get("addressPoiNumber"));
		}
		}catch(Exception e){
			log.error("ERROR:传入对应的key_value内容有误，按照默认参数处理");
		}
	}
	
    /**
     * 获取重要POI集合
     * IX_POI等级为A或者重要POI一览表中的poi数据为重要POI数据
     * @throws Exception 
     * 
     * */
	public void CollectScenarioService(int dailyDBId){
		Connection dailyConn = null;
		try{
			dailyConn = DBConnector.getInstance().getConnectionById(dailyDBId);
			
			//开始执行之前先初始化数据表
			initLinkTable(dailyConn);
			//将link表中的所有数据保存到目标表中，且状态为车采
			insertLinkTableFromTempTable(dailyConn);
//			1-2级的直接保存为车采
//			insertEditPreCarColectionLink(dailyConn);
			//10级及以上不考虑采集方式直接保存
			insertEditPreOriginalLink(dailyConn);
			//查询3-9级link附近30米范围内的poi相关数据保存到临时表
			creatTempTableFromLinkTable(dailyConn);
			//从临时表中删除一个link下重复的poi
			deleteSameDataInTempTable(dailyConn);
			//更新临时表中的重要POI为A
			insertImportantPoiToTempTable(dailyConn);
			//3-9保存
			insertEditPreLinkBetweenTrheeAndNineFac(dailyConn);
			//数据处理完成删除临时表
//			deleteTempTable(dailyConn);
		}catch(Exception e){
			try{
				initLinkTable(dailyConn);
			}catch(Exception ex){
				System.out.println(ex);
			}
			log.error(e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(dailyConn);
		}finally{
			DbUtils.commitAndCloseQuietly(dailyConn);
		}
	}
	
	/**
	 * 初始化数据表，先删除数据内的所有内容
	 * @param Connection
	 * @throws Exception
	 * 
	 * */
	public void initLinkTable(Connection dailyConn) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "delete LINK_EDIT_PRE";
			log.info("初始化数据sql：" + sql);
			run.execute(dailyConn, sql);
			String tableSql = "drop table LINK_PRE_TEST";
			log.info("初始化表sql：" + sql);
			run.execute(dailyConn, tableSql);
			dailyConn.commit();
		}catch(Exception e){
			log.error("初始化异常");
		}
	}
	
	/**
	 * 从man库中查询所有的日库id
	 * @param Connection
	 * @return List<Integer>
	 * @throws Exception
	 * 
	 * */
	public static List<Integer> queryDbId(Connection conn) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select distinct t.daily_db_id from REGION t";
			ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>() {
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> result = new ArrayList<Integer>();
					while (rs.next()) {
						result.add(rs.getInt("daily_db_id"));
					}
					return result;
				}
			};
			return run.query(conn, sql, rsHandler);
		}catch(Exception e){
			log.error("从管理库查询对应的dailyDbId异常："+e.getMessage(),e);
			throw e;
		}
	}
	
    /**
     * 从link中查询3-9级道路，地址POI，POI等级的数据保存到临时表中
     * @param Connection
     * @throws Exception
     * 
     * */
	public void creatTempTableFromLinkTable(Connection dailyConn) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "CREATE TABLE LINK_PRE_TEST AS "
					+ "( SELECT T.LINK_PID, T.LENGTH, T.PID, T.RANGE, IA.NAME_ID ADDRESS FROM"
					+ "(SELECT RL.LINK_PID, IP.PID, RL.LENGTH, IP."+"\""+"LEVEL"+"\""+"  RANGE FROM RD_LINK RL, IX_POI IP, "
					+ "TABLE(SDO_JOIN('RD_LINK', 'GEOMETRY', 'IX_POI', 'GEOMETRY', 'DISTANCE=30')) J "
					+ "WHERE J.ROWID1 = RL.ROWID AND J.ROWID2 = IP.ROWID AND RL.KIND >= 3 AND RL.KIND <= 9) T "
					+ "LEFT JOIN IX_POI_ADDRESS IA ON IA.POI_PID = T.PID)";
			log.info("保存数据到临时表sql:" + sql);
			run.execute(dailyConn, sql);
		}catch(Exception e){
			log.error("保存数据到临时表："+e.getMessage(), e);
			throw e;
		}
	}
	
	/**
	 * 从临时表中删除link和poi都重复的数据
	 * 关联地址POI可能会产生重复的数据，地址POI数据包含中文英文两种数据
	 * @param Connection
	 * @throws Exception
	 * 
	 * */
	public void deleteSameDataInTempTable(Connection dailyConn) throws Exception{
		try{
			QueryRunner run = new QueryRunner();

			String sql = "delete from LINK_PRE_TEST a where rowid !=(select max(rowid) "
					+ "from LINK_PRE_TEST b where a.link_pid = b.link_pid and a.pid = b.pid) ";
			
			log.info("从临时表中删除link和poi都重复的数据sql：" + sql);
			run.execute(dailyConn, sql);
			dailyConn.commit();
		}catch(Exception e){
			log.error("从临时表中删除link和poi都重复的数据异常："+e.getMessage(),e);
			throw e;
		}
	}
	
	
	/**
	 * 道路等级10级和以上不考虑车采步采保存到LINK_EDIT_PRE
	 * @param Connection
	 * @throws Exception
	 * 
	 * */
	public void insertEditPreOriginalLink(Connection dailyConn) throws Exception{
		try{
			QueryRunner run = new QueryRunner();

			String sql = "update LINK_EDIT_PRE t set t.scenario = 0 where t.pid in( "
					+ "select r.link_pid from RD_LINK r where r.kind >= 10)";
			
			log.info("10级以上link保存sql：" + sql);
			run.execute(dailyConn, sql);
		}catch(Exception e){
			log.error("保存10级以上link数据到LINK_EDIT_PRE异常："+e.getMessage(),e);
			throw e;
		}
	}
	
	/**
	 * 保存3-9级的数据
	 * @param Connection
	 * @throws Exception 
	 * 
	 * */
	 public void insertEditPreLinkBetweenTrheeAndNineFac(Connection dailyConn) throws Exception{
		try{
			log.info("开始从临时表中查询覆盖的重要POI数据到目标表中");
			updateImportantPoi(dailyConn);
			log.info("开始从临时表中查询覆盖的地址POI数据到目标表中");
			updateAddressPoi(dailyConn);
			log.info("开始从临时表中查询覆盖的普通POI数据到目标表中");
			updateNormalPoi(dailyConn);
			log.info("更新包含步行街属性的数据到目标表中");
			updateWalkPoi(dailyConn);
		}catch(Exception e){
			log.error("保存3-9级的数据异常："+e.getMessage(),e);
			throw e;
		}
	 }
	 
	 /**
	  * 保存重要POI数据
	  * 每100米link，覆盖的POI数量小于10个，且覆盖的重要POI大于等于3个
      * @param Connection
	  * @throws Exception 
	  * 
	  * */
	 public void updateImportantPoi (Connection dailyConn) throws Exception{
		 try{
				QueryRunner run = new QueryRunner();

				StringBuffer sb = new StringBuffer();
				sb.append("update link_edit_pre t set t.scenario = 1 where t.pid in");
				sb.append(" (select p.LINK_PID FROM (select t.LINK_PID, count(1) poicount,t.length, t.range");
				sb.append(" from LINK_PRE_TEST t where t.range = 'A' group by t.LINK_PID,t.length,t.range) p");
				sb.append(" where (p.poicount * 1.000 / p.LENGTH) * 100 >= "+importantPoiCount+")");

				String sql = sb.toString();
				
				log.info("重要POI数据保存sql：" + sql);
				run.execute(dailyConn, sql);
			}catch(Exception e){
				log.error("重要POI数据到LINK_EDIT_PRE异常："+e.getMessage(),e);
				throw e;
			}
	 }
	 
	 /**
	  * 保存地址POI数据
	  * 每100米link，覆盖的POI数量小于10个，且覆盖的地址POI大于等于5个
	  * @param Connection
	  * @throws Exception 
	  * */
	 public void updateAddressPoi(Connection dailyConn) throws Exception{
		 try{
				QueryRunner run = new QueryRunner();

				StringBuffer sb = new StringBuffer();
				sb.append(" update link_edit_pre t set t.scenario = 1 where t.pid in (");
				sb.append(" select t.LINK_PID  FROM (select t.length, t.LINK_PID, count(1) poicount ");
				sb.append(" from LINK_PRE_TEST t where t.address != 0 group by t.LINK_PID, t.length) t");
				sb.append(" where (t.poicount * 1.000 / t.LENGTH) * 100 >= "+addressPoiCount+")");

				String sql = sb.toString();
				
				log.info("地址POI保存sql：" + sql);
				run.execute(dailyConn, sql);
			}catch(Exception e){
				log.error("保存地址POI相关link数据到LINK_EDIT_PRE异常："+e.getMessage(),e);
				throw e;
			}
	 }
	 
	 /**
	  * 保存普通POI数据
	  * 每100米link，覆盖的POI数量大于10个
	  * @param Connection
	  * @throws Exception 
	  * */
	 public void updateNormalPoi(Connection dailyConn) throws Exception {
		 try{
			QueryRunner run = new QueryRunner();
			
			StringBuffer sb = new StringBuffer();
			sb.append("update link_edit_pre t set t.scenario = 1 where t.pid in(");
			sb.append(" select q.LINK_PID  FROM(select t.LINK_PID, count(1) poicount,t.length");
			sb.append(" from LINK_PRE_TEST t group by t.LINK_PID,t.length) q");
			sb.append(" where (q.poicount * 1.000 / q.LENGTH) * 100 >= "+normalPoiCount+")");

			String sql = sb.toString();
			log.info("保存普通POI数据sql：" + sql);
			run.execute(dailyConn, sql);
		}catch(Exception e){
			log.error("保存普通POI数据到LINK_EDIT_PRE异常："+e.getMessage(),e);
			throw e;
		}
	 }
	 
	 /**
	  * 更新包含步行街属性的link
	  * @param Connection
	  * @throws Exception 
	  * */
	 public void updateWalkPoi(Connection dailyConn) throws Exception {
		 try{
				QueryRunner run = new QueryRunner();
				
				StringBuffer sb = new StringBuffer();
				sb.append("update link_edit_pre t set t.scenario = 1 where t.pid in( ");
				sb.append("select distinct pt.LINK_PID FROM RD_LINK_FORM pt where pt.form_of_way = 20)");

				String sql = sb.toString();
				log.info("保存步行街POI数据sql：" + sql);
				run.execute(dailyConn, sql);
			}catch(Exception e){
				log.error("保存步行街POI数据到LINK_EDIT_PRE异常："+e.getMessage(),e);
				throw e;
			}
	 }
	
	/**
	 * 从元数据库中取出重点POI更新到临时表
	 * @param Connection
	 * @throws Exception
	 * 
	 * */
	public void insertImportantPoiToTempTable(Connection dailyConn) throws Exception{
		try{
			//通过api调用元数据库重要POI一览表中的POI数据
			MetadataApi api = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			List<String> importantPids = api.queryImportantPid();

			if(importantPids == null || importantPids.size() == 0){ 
				return; 
			}
			QueryRunner run = new QueryRunner();
			StringBuffer sb = new StringBuffer();
			for(int i = 0; i < importantPids.size(); i++){
				sb.append(importantPids.get(i)+",");
			}
			
			String pids = sb.deleteCharAt(sb.length() - 1).toString();
			//String parameter = "d.LINK_PID";
//			if(importantPids.size() > 900){
//				pids = JdbcSqlUtil.getInParameter(importantPids, parameter);
//			}
			
			StringBuffer sqlSb = new StringBuffer();
			sqlSb.append("update LINK_PRE_TEST d"
					+ "  set d.RANGE = 'A'"
					+ "  where d.PID in (select p.pid"
					+ "                   from ix_poi p,"
					+ "                        (select column_value from table(clob_to_table(?))) t"
					+ "                  where p.poi_num = t.column_value)");
			Clob clob=ConnectionUtil.createClob(dailyConn);
			clob.setString(1, pids);
			
			String sql = sqlSb.toString();
			
			log.info("更新等级A的pids:" + pids);
			log.info("从元数据库中查询出的重点POI更新到临时表sql:"+sql);
			run.update(dailyConn, sql, clob);
			dailyConn.commit();
		}catch(Exception e){
			log.error("元数据库中查询出的重点POI更新到临时表异常:"+e.getMessage(),e);
			throw e;
		}
	}
	
	/**
	 * 将link表中的所有数据保存到目标表中，且状态为车采
	 * @param Connection
	 * @throws Exception
	 * 
	 * */
	public void insertLinkTableFromTempTable(Connection dailyConn) throws Exception{
		 try{
			QueryRunner run = new QueryRunner();

			String sql = "insert into link_edit_pre ep (ep.pid, ep.scenario, ep.operate_date) "
					+ "select p.LINK_PID, 2, CURRENT_TIMESTAMP from  rd_link p";
			
			log.info("将link表中的所有数据保存到目标表中sql：" + sql);
			run.execute(dailyConn, sql);
		}catch(Exception e){
			log.error("将link表中的所有数据保存到目标表异常："+e.getMessage(),e);
			throw e;
		}
	}
	
//	/**
//	 * 数据处理完成删除临时表
//	 * @param
//	 * @throws Exception 
//	 * 
//	 * */
//	public void deleteTempTable(Connection dailyConn) throws Exception{
//		 try{
//				QueryRunner run = new QueryRunner();
//
//				String sql = "DROP TABLE LINK_PRE_TEST";
//				
//				log.info("删除临时表sql：" + sql);
////				run.execute(dailyConn, sql);
//			}catch(Exception e){
//				log.error("删除临时表异常："+e.getMessage(),e);
//			}
//	}
}
	class MyTask implements Runnable {
		   private int dailyDbId;
		    
		   public MyTask(int dailyDbId) {
		       this.dailyDbId = dailyDbId;
		   }
		    
		   @Override
		   public void run() {
		       System.out.println("正在执行库dailyDbId: "+dailyDbId);
		       try {
					CollectScenario.getThreadInstance().CollectScenarioService(dailyDbId);
		            Thread.currentThread().sleep(4000);
		       } catch (InterruptedException e) {
		           e.printStackTrace();
		       }
		       System.out.println("dailyDbId: "+dailyDbId+"执行完毕");
		   }
}
