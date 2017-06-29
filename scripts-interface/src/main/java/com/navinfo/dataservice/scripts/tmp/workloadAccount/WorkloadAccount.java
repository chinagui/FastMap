package com.navinfo.dataservice.scripts.tmp.workloadAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.engine.man.region.RegionService;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoService;
import com.navinfo.dataservice.scripts.JobScriptsInterface;
import com.navinfo.dataservice.scripts.refinement.RefinementLogDependent;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceRtException;
import com.navinfo.navicommons.exception.ThreadExecuteException;

/** 
 * @ClassName: WorkloadAccount
 * @author songdongyan
 * @date 2017年6月6日
 * @Description: WorkloadAccount.java
 */
public class WorkloadAccount {
	
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected static VMThreadPoolExecutor threadPoolExecutor;
	private static Map<Integer,Object> result = new HashMap<Integer,Object>();
	private static Map<Integer,Integer> subtaskUserMap = new HashMap<Integer,Integer>();
	protected Map<Integer,String> users = new HashMap<Integer,String>();

	
	public void account() throws Exception{
		try{
//			ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
//			//所有大区库，一个大区库起一个线程
//			List<Integer> dbIdList = manApi.listDayDbIds();
//			users = manApi.getUsers();
//			subtaskUserMap = manApi.getsubtaskUserMap();
			
			//所有大区库，一个大区库起一个线程
			List<Integer> dbIdList = RegionService.getInstance().listDayDbIds();
			users = UserInfoService.getInstance().getUsers();
			subtaskUserMap = SubtaskService.getInstance().getsubtaskUserMap();
			
			//执行导入
			int dbSize = dbIdList.size();
			if(dbSize==0){
				//"无数据需要导入，导入结束");
				return;
			}
			if(dbSize==1){
				int dbId = dbIdList.get(0);
				new WorkloadAccountThread(null, dbId).run();
	
			}else{
				if(dbSize>10){
					initThreadPool(10);
				}else{
					initThreadPool(dbSize);
				}
				final CountDownLatch latch = new CountDownLatch(dbSize);
				threadPoolExecutor.addDoneSignal(latch);
				//执行统计
				for(Integer dbId:dbIdList){
					threadPoolExecutor.execute(new WorkloadAccountThread(latch,dbId));
				}
				latch.await();
				if (threadPoolExecutor.getExceptions().size() > 0) {
					throw new Exception(threadPoolExecutor.getExceptions().get(0));
				}
			}
		}catch(Exception e){
			throw e;
		}
		
	}

	
	private static void initThreadPool(int poolSize)throws Exception{
		//"开始初始化线程池");
        threadPoolExecutor = new VMThreadPoolExecutor(poolSize,
        		poolSize,
				3,
				TimeUnit.SECONDS,
				new LinkedBlockingQueue(),
				new ThreadPoolExecutor.CallerRunsPolicy());
	}
	private void shutDownPoolExecutor(){
		if (threadPoolExecutor != null && !threadPoolExecutor.isShutdown()) {
			log.debug("关闭线程池");
			threadPoolExecutor.shutdownNow();
			try {
				while (!threadPoolExecutor.isTerminated()) {
					log.debug("等待线程结束：线程数为" + threadPoolExecutor.getActiveCount());
					Thread.sleep(2000);
				}
			} catch (InterruptedException e) {
				log.error("关闭线程池失败");
				throw new ServiceRtException("关闭线程池失败", e);
			}
		}
	}
	
	class WorkloadAccountThread implements Runnable{
		CountDownLatch latch = null;
		int dbId=0;
		WorkloadAccountThread(CountDownLatch latch,int dbId){
			this.latch=latch;
			this.dbId=dbId;
		}
		
		@Override
		public void run() {
			Connection conn=null;
			try{
				conn=DBConnector.getInstance().getConnectionById(dbId);
				QueryRunner run = new QueryRunner();
				
				StringBuilder sb = new StringBuilder();
				
				sb.append("SELECT A.US_ID, O.OP_DT, D.OP_ID, D.OB_PID, D.TB_NM, D.OP_TP,S.IS_UPLOAD,S.FRESH_VERIFIED,S.UPLOAD_DATE,S.QUICK_SUBTASK_ID,S.MEDIUM_SUBTASK_ID  ");
				sb.append("  FROM LOG_ACTION A, LOG_OPERATION O, LOG_DETAIL D,POI_EDIT_STATUS S                                                                            ");
				sb.append(" WHERE A.ACT_ID = O.ACT_ID                                                                                                                      ");
				sb.append("   AND O.OP_ID = D.OP_ID                                                                                                                        ");
				sb.append("   AND D.OB_NM = 'IX_POI'                                                                                                                       ");
				sb.append("   AND D.OB_PID = S.PID                                                                                                                         ");
				sb.append(" ORDER BY D.OB_PID, O.OP_DT                                                                                                                     ");
				
				log.info("log sql:" + sb.toString());
				
				Map<String,Map<String,Integer>> result = run.query(conn, sb.toString(), getStatHander());
				//解析入库
				createTable(conn);
				
				List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
				for(Map.Entry<String, Map<String,Integer>> entry:result.entrySet()){
					String[] str = StringUtils.split(entry.getKey(), '_');
					int userId = 0;
					String date = "";
					if(str.length==2){
						date = str[1];
						userId = Integer.parseInt(str[0]);
					}
					if(userId==0&&date.equals("")){
						continue;
					}
					Map<String,Object> cell = new HashMap<String,Object>();
					cell.put("userId", userId);
					cell.put("date", date);
					cell.put("insert", entry.getValue().get("insert"));
					cell.put("update", entry.getValue().get("update"));
					cell.put("delete", entry.getValue().get("delete"));
					cell.put("fresh", entry.getValue().get("fresh"));
					if(users.containsKey(userId)){
						cell.put("userName", users.get(userId));
					}else{
						cell.put("userName", "");
					}
					data.add(cell);
				}
				
				insertData(conn,data);
	
				log.debug("dbId("+dbId+")成功。");
			}catch(Exception e){
				DbUtils.rollbackAndCloseQuietly(conn);
				log.error(e.getMessage(),e);
				throw new ThreadExecuteException("");
			}finally{
				DbUtils.commitAndCloseQuietly(conn);
				if(latch!=null){
					latch.countDown();
				}
			}
		}
		
		
		/**
		 * @param conn
		 * @param data
		 * @throws SQLException 
		 */
		private void insertData(Connection conn, List<Map<String, Object>> data) throws SQLException {
			String sql = "INSERT INTO POI_WORKLOAD_ACCOUNT (USER_ID,USER_NAME,TIME,INSERT_NUM,UPDATE_NUM,DELETE_NUM,FRESHNESS_NUM) VALUES (?,?,?,?,?,?,?)";
			PreparedStatement perstmt = conn.prepareStatement(sql);
			if(data.size()==0){
				return;
			}
			for(Map<String, Object> cell:data){
				perstmt.setInt(1,Integer.parseInt(cell.get("userId").toString()));
				perstmt.setString(2,cell.get("userName").toString());
				perstmt.setString(3,cell.get("date").toString());
				perstmt.setInt(4,Integer.parseInt(cell.get("insert").toString()));
				perstmt.setInt(5,Integer.parseInt(cell.get("update").toString()));
				perstmt.setInt(6,Integer.parseInt(cell.get("delete").toString()));
				perstmt.setInt(7,Integer.parseInt(cell.get("fresh").toString()));
				perstmt.addBatch();
			}
			if(perstmt!=null){
				perstmt.executeBatch();
			}
		}

		private  ResultSetHandler<Map<String,Map<String,Integer>>> getStatHander(){
			return new ResultSetHandler<Map<String,Map<String,Integer>>>() {
				@Override
				public Map<String,Map<String,Integer>> handle(ResultSet rs) throws SQLException {
					Map<String,Map<String,Integer>> result = new HashMap<String,Map<String,Integer>>();
				    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
				    List<Long> pidList = new ArrayList<Long>();
				    String opId = "";
				    Long pid = 0L;
				    int userId = 0;
				    String date = "";
				    int status = 3;//默认为修改
				    int isUpload = 0;
				    String uploadDate = "";
				    int freshVerified = 0;
				    int quickSubtaskId = 0;
				    int mediumSubtaskId = 0;
					while (rs.next()) {
						if(pidList.contains(rs.getLong("OB_PID"))){
							continue;
						}
						if(!opId.equals(rs.getString("OP_ID"))){
							pidList.add(pid);
							//用户Id+日期构成KEY
							String key = Integer.toString(userId) + "_" + date;
							if(result.containsKey(key)){
								Map<String,Integer> tmp = result.get(key);
								int insert = tmp.get("insert");
								int update = tmp.get("update");
								int delete = tmp.get("delete");
								int fresh = tmp.get("fresh");

								if(status==1){
									tmp.put("insert", insert+1);
								}else if(status==3){
									int subtaskId = (quickSubtaskId>mediumSubtaskId?mediumSubtaskId:quickSubtaskId);
									if(isUpload!=0&&uploadDate.equals(date)&&freshVerified!=0&&subtaskUserMap.containsKey(subtaskId)&&subtaskUserMap.get(subtaskId)==userId){
										tmp.put("fresh", fresh+1);
									}else{
										tmp.put("update", update+1);
									}
								}else if(status==2){
									tmp.put("delete", delete+1);
								}
							}else{
								Map<String,Integer> tmp = new HashMap<String,Integer>();
								tmp.put("insert", 0);
								tmp.put("delete", 0);
								tmp.put("update", 0);
								tmp.put("fresh", 0);
								if(status==1){
									tmp.put("insert", 1);
								}else if(status==3){
									int subtaskId = (quickSubtaskId>mediumSubtaskId?mediumSubtaskId:quickSubtaskId);
									if(isUpload!=0&&uploadDate.equals(date)&&freshVerified!=0&&subtaskUserMap.containsKey(subtaskId)&&subtaskUserMap.get(subtaskId)==userId){
										tmp.put("fresh", 1);
									}else{
										tmp.put("update", 1);
									}
								}else if(status==2){
									tmp.put("delete", 1);
								}
								result.put(key, tmp);
							}
							userId = rs.getInt("US_ID");
							date = df.format(rs.getTimestamp("OP_DT"));
							isUpload = rs.getInt("IS_UPLOAD");
							if(rs.getTimestamp("UPLOAD_DATE")!=null){
								uploadDate = df.format(rs.getTimestamp("UPLOAD_DATE"));
							}else{
								uploadDate = "";
							}
							freshVerified = rs.getInt("FRESH_VERIFIED");
							quickSubtaskId = rs.getInt("QUICK_SUBTASK_ID");
							mediumSubtaskId = rs.getInt("MEDIUM_SUBTASK_ID");
							pid = rs.getLong("OB_PID");
							opId = rs.getString("OP_ID");
						}
						if(opId.equals(rs.getString("OP_ID"))){
							//主表新增--新增
							if(rs.getString("TB_NM").equals("IX_POI")&&rs.getInt("OP_TP")==1){
								status = 1;
							}
							//主表删除--删除
							else if(rs.getString("TB_NM").equals("IX_POI")&&rs.getInt("OP_TP")==2){
								status = 2;
							}
						}
					}
					return result;
				}
			};
		}
		
		public void createTable(Connection conn) throws SQLException{
			QueryRunner r = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("declare                                                                                              ");
			sb.append("    num   number;                                                                                    ");
			sb.append("begin                                                                                                ");
			sb.append("    select count(1) into num from user_tables where table_name = upper('POI_WORKLOAD_ACCOUNT') ;    ");
			sb.append("    if num > 0 then                                                                                  ");
			sb.append("        execute immediate 'drop table POI_WORKLOAD_ACCOUNT' ;                                       ");
			sb.append("    end if;                                                                                          ");
			sb.append("end;                                                                                                 ");
			
			r.execute(conn, sb.toString());
			sb.delete( 0, sb.length() );
			sb.append("create table POI_WORKLOAD_ACCOUNT               ");
			sb.append("(                                               ");
			sb.append("  user_id   number(10),                         ");
			sb.append("  user_name varchar2(100),                      ");
			sb.append("  time      varchar2(8),                        ");
			sb.append("  insert_num    number(10) default 0 not null,  ");
			sb.append("  update_num    number(10) default 0 not null,  ");
			sb.append("  delete_num    number(10) default 0 not null,  ");
			sb.append("  freshness_num number(10) default 0 not null   ");
			sb.append(")                                               ");
			r.execute(conn, sb.toString());
		}
		
		
	}
	
	
	public static void main(String[] args) throws Exception{
		JobScriptsInterface.initContext();
		WorkloadAccount WorkloadAccount = new WorkloadAccount();
		WorkloadAccount.account();
	}
}
