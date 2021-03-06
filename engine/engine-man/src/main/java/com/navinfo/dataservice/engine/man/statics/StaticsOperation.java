package com.navinfo.dataservice.engine.man.statics;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

/** 
 * @ClassName: StaticsOperation
 * @author songdongyan
 * @date 2016年10月12日
 * @Description: StaticsOperation.java
 */
public class StaticsOperation {

	/**
	 * 
	 */
	public StaticsOperation() {
		// TODO Auto-generated constructor stub
	}

	private static Logger log = LoggerRepos.getLogger(StaticsOperation.class);

	/**
	 * @param conn 
	 * @param selectSql 
	 * @return
	 * @throws Exception 
	 */
	public static Map<String, Object> queryTaskOverView(Connection conn, String selectSql) throws Exception {
		// TODO Auto-generated method stub
		try {
			QueryRunner run = new QueryRunner();

			ResultSetHandler<Map<String, Object>> rsHandler = new ResultSetHandler<Map<String, Object>>() {
				public Map<String, Object> handle(ResultSet rs) throws SQLException {
					Map<String,Object> result = new HashMap<String,Object>();
					int	unPlanned = 0;
					int planned = 0;
					int planClosed = 0;
					
					int draft = 0;
					int ongoing = 0;
					int finished = 0;
					
					int ongoingRegularCollect = 0;
					int ongoingUnexpectedCollect = 0;
					int ongoingFinishedCollect = 0;
					
					int ongoingRegularDaily = 0;
					int ongoingUnexpectedDaily = 0;
					int ongoingFinishedDaily = 0;
					
					int ongoingRegularMonthly = 0;
					int ongoingUnexpectedMonthly = 0;
					int ongoingFinishedMonthly = 0;
					
					int finishedRegular = 0;
					int finishedOverdue = 0;
					int finishedAdvanced = 0;

					int closedRegular = 0;
					int closedOverdue = 0;
					int closedAdvanced = 0;
					
					while (rs.next()) {
						String blockManIdList = rs.getString("BLOCK_MAN_ID_LIST");
						String blockManStatusList = rs.getString("BLOCK_MAN_STATUS_LIST");
						if(0 == rs.getInt("PLAN_STATUS")){
							unPlanned += 1;
						}else if(1 == rs.getInt("PLAN_STATUS")){
							planned += 1;
							if(2 == rs.getInt("STATUS")){
								draft += 1;
							}else if(1 == rs.getInt("STATUS")){
//								if((blockManIdList==null) 
//										||((blockManStatusList!=null)
//												&& (-1==blockManStatusList.indexOf("1")) 
//												&& (-1==blockManStatusList.indexOf("2"))
//												)
//										){
								if((blockManStatusList!=null)
												&& (-1==blockManStatusList.indexOf("1")) 
												&& (-1==blockManStatusList.indexOf("2"))
										){
									finished += 1;
									//完成情况
									if(0 == rs.getInt("DIFF_DATE")){
										finishedRegular += 1;
									}else if(0 > rs.getInt("DIFF_DATE")){
										finishedOverdue += 1;
									}else if(0 < rs.getInt("DIFF_DATE")){
										finishedAdvanced += 1;
									}
								}else{
									ongoing += 1;
									//采集作业中情况
									if(100 == rs.getInt("COLLECT_PERCENT")){
										ongoingFinishedCollect += 1;
									}else{
										if(2 == rs.getInt("COLLECT_PROGRESS")){
											ongoingUnexpectedCollect += 1;
										}else{
											ongoingRegularCollect += 1;
										}
									}
									//日编作业中情况
									if(100 == rs.getInt("DAILY_PERCENT")){
										ongoingFinishedDaily += 1;
									}else{
										if(2 == rs.getInt("DAILY_PROGRESS")){
											ongoingUnexpectedDaily += 1;
										}else{
											ongoingRegularDaily += 1;
										}
									}
									//月编作业中情况
									if(100 == rs.getInt("MONTHLY_PERCENT")){
										ongoingFinishedMonthly += 1;
									}else{
										if(2 == rs.getInt("MONTHLY_PROGRESS")){
											ongoingUnexpectedMonthly += 1;
										}else{
											ongoingRegularMonthly += 1;
										}
									}
								}
							}
						}else if(2 == rs.getInt("PLAN_STATUS")){
							planClosed += 1;
							//完成情况
							if(0 == rs.getInt("DIFF_DATE")){
								closedRegular += 1;
							}else if(0 > rs.getInt("DIFF_DATE")){
								closedOverdue += 1;
							}else if(0 < rs.getInt("DIFF_DATE")){
								closedAdvanced += 1;
							}
						}
					}
					
					Map<String,Integer> taskInfo = new HashMap<String,Integer>();
					taskInfo.put("unPlanned", unPlanned);
					taskInfo.put("draft", draft);
					taskInfo.put("unreleased", unPlanned + draft);
					taskInfo.put("ongoing", ongoing);
					taskInfo.put("finished", finished);
					taskInfo.put("closed", planClosed);
					taskInfo.put("total", unPlanned + draft + ongoing + finished + planClosed);
					result.put("taskInfo", taskInfo);
					
					Map<String,Integer> cityInfo = new HashMap<String,Integer>();
					cityInfo.put("unPlanned", unPlanned);
					cityInfo.put("planned", planned);
					cityInfo.put("planClosed", planClosed);
					cityInfo.put("total", unPlanned + planned + planClosed);
					result.put("cityInfo", cityInfo);
					
					Map<String,Object> ongoingInfo = new HashMap<String,Object>();
					
					Map<String,Integer> ongoingCollectInfo = new HashMap<String,Integer>();
					ongoingCollectInfo.put("ongoingRegularCollect", ongoingRegularCollect);
					ongoingCollectInfo.put("ongoingUnexpectedCollect", ongoingUnexpectedCollect);
					ongoingCollectInfo.put("ongoingFinishedCollect", ongoingFinishedCollect);
					ongoingCollectInfo.put("total", ongoingFinishedCollect + ongoingRegularCollect + ongoingUnexpectedCollect);
					
					Map<String,Integer> ongoingDailyInfo = new HashMap<String,Integer>();
					ongoingDailyInfo.put("ongoingRegularDaily", ongoingRegularDaily);
					ongoingDailyInfo.put("ongoingUnexpectedDaily", ongoingUnexpectedDaily);
					ongoingDailyInfo.put("ongoingFinishedDaily", ongoingFinishedDaily);
					ongoingDailyInfo.put("total", ongoingRegularDaily + ongoingUnexpectedDaily + ongoingFinishedDaily);
					
					Map<String,Integer> ongoingMonthlyInfo = new HashMap<String,Integer>();
					ongoingMonthlyInfo.put("ongoingRegularMonthly", ongoingRegularMonthly);
					ongoingMonthlyInfo.put("ongoingUnexpectedMonthly", ongoingUnexpectedMonthly);
					ongoingMonthlyInfo.put("ongoingFinishedMonthly", ongoingFinishedMonthly);
					ongoingMonthlyInfo.put("total", ongoingRegularMonthly + ongoingUnexpectedMonthly + ongoingFinishedMonthly);
					
					ongoingInfo.put("ongoingCollectInfo", ongoingCollectInfo);
					ongoingInfo.put("ongoingDailyInfo", ongoingDailyInfo);
					ongoingInfo.put("ongoingMonthlyInfo", ongoingMonthlyInfo);
					
					result.put("ongoingInfo", ongoingInfo);
					
					Map<String,Integer> finishedInfo = new HashMap<String,Integer>();
					finishedInfo.put("finishedRegular", finishedRegular);
					finishedInfo.put("finishedAdvanced", finishedAdvanced);
					finishedInfo.put("finishedOverdue", finishedOverdue);
					finishedInfo.put("total", finished);
					result.put("finishedInfo", finishedInfo);
					
					Map<String,Integer> closedInfo = new HashMap<String,Integer>();
					closedInfo.put("closedRegular", closedRegular);
					closedInfo.put("closedOverdue", closedOverdue);
					closedInfo.put("closedAdvanced", closedAdvanced);
					closedInfo.put("total", planClosed);
					result.put("closedInfo", closedInfo);

					return result;
				}

			};

			return run.query(conn, selectSql, rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询grid失败:" + e.getMessage(), e);
		}
	}
	
	/**
	 * @param conn 
	 * @param selectSql 
	 * @return
	 * @throws Exception 
	 */
	public static Map<String, Object> queryProgramOverView(Connection conn, String selectSql) throws Exception {
		// TODO Auto-generated method stub
		try {
			QueryRunner run = new QueryRunner();

			ResultSetHandler<Map<String, Object>> rsHandler = new ResultSetHandler<Map<String, Object>>() {
				public Map<String, Object> handle(ResultSet rs) throws SQLException {
					Map<String,Object> result = new HashMap<String,Object>();
					int unPush=0;//未发布
					int ongoing=0;//作业中
			        int unClosed=0;//待关闭
			        int closed=0;//已关闭
			        
			        int unPlanned=0;//未规划
					int draft=0;//草稿
					
					int ongoingRegularCollect = 0;
					int ongoingUnexpectedCollect = 0;
					int ongoingFinishedCollect = 0;
					
					int ongoingRegularDaily = 0;
					int ongoingUnexpectedDaily = 0;
					int ongoingFinishedDaily = 0;
					
					int ongoingRegularMonthly = 0;
					int ongoingUnexpectedMonthly = 0;
					int ongoingFinishedMonthly = 0;
					
					int unClosedRegular = 0;
					int unClosedOverdue = 0;
					int unClosedAdvanced = 0;

					int closedRegular = 0;
					int closedOverdue = 0;
					int closedAdvanced = 0;
					
					while (rs.next()) {
						//未规划，草稿，已关闭，进行中
						int planStatus=rs.getInt("PLAN_STATUS");
						//1未规划2进行中3待关闭4已关闭
						int taskStat=rs.getInt("TASK_STAT");
						//COLLECT_STAT,DAILY_STAT,MONTHLY_STAT 3待关闭2进行中						
						if(0==planStatus){//未规划
							unPush+=1;
							unPlanned+=1;
						}else if (1==planStatus) {//草稿
							unPush+=1;
							draft+=1;
						}else if (2==planStatus) {//已关闭
							closed+=1;
							//完成情况
							if(0 == rs.getInt("DIFF_DATE")){
								closedRegular += 1;
							}else if(0 > rs.getInt("DIFF_DATE")){
								closedOverdue += 1;
							}else if(0 < rs.getInt("DIFF_DATE")){
								closedAdvanced += 1;
							}
						}else if (2==taskStat) {//进行中
							ongoing+=1;
							//采集作业中情况
							if(3==rs.getInt("COLLECT_STAT")){ongoingFinishedCollect+=1;}
							else if(2 == rs.getInt("COLLECT_PROGRESS")){
								ongoingUnexpectedCollect += 1;
							}else{
								ongoingRegularCollect += 1;
							}
							//日编作业中情况
							if(3==rs.getInt("DAILY_STAT")){ongoingFinishedCollect+=1;}
							else if(2 == rs.getInt("DAILY_PROGRESS")){
								ongoingUnexpectedDaily += 1;
							}else{
								ongoingRegularDaily += 1;
							}
							//月编作业中情况
							if(3==rs.getInt("MONTHLY_STAT")){ongoingFinishedCollect+=1;}
							else if(2 == rs.getInt("MONTHLY_PROGRESS")){
								ongoingUnexpectedMonthly += 1;
							}else{
								ongoingRegularMonthly += 1;
							}
						}else if (3==taskStat) {//待关闭
							unClosed+=1;
							//完成情况
							if(0 == rs.getInt("DIFF_DATE")){
								unClosedRegular += 1;
							}else if(0 > rs.getInt("DIFF_DATE")){
								unClosedOverdue += 1;
							}else if(0 < rs.getInt("DIFF_DATE")){
								unClosedAdvanced += 1;
							}
						}
					}
					Map<String,Integer> cityInfo = new HashMap<String,Integer>();
					cityInfo.put("unPush", unPush);
					cityInfo.put("ongoing", ongoing);
					cityInfo.put("unClosed", unClosed);
					cityInfo.put("closed", closed);
					cityInfo.put("total", unPush + ongoing + unClosed+closed);
					result.put("cityInfo", cityInfo);
					
					Map<String,Integer> unPushInfo = new HashMap<String,Integer>();
					unPushInfo.put("unPlanned", unPlanned);
					unPushInfo.put("draft", draft);
					result.put("unPushInfo", unPushInfo);
					
					Map<String,Object> ongoingInfo = new HashMap<String,Object>();
					
					Map<String,Integer> ongoingCollectInfo = new HashMap<String,Integer>();
					ongoingCollectInfo.put("ongoingRegularCollect", ongoingRegularCollect);
					ongoingCollectInfo.put("ongoingUnexpectedCollect", ongoingUnexpectedCollect);
					ongoingCollectInfo.put("ongoingFinishedCollect", ongoingFinishedCollect);
					ongoingCollectInfo.put("total", ongoingFinishedCollect + ongoingRegularCollect + ongoingUnexpectedCollect);
					
					Map<String,Integer> ongoingDailyInfo = new HashMap<String,Integer>();
					ongoingDailyInfo.put("ongoingRegularDaily", ongoingRegularDaily);
					ongoingDailyInfo.put("ongoingUnexpectedDaily", ongoingUnexpectedDaily);
					ongoingDailyInfo.put("ongoingFinishedDaily", ongoingFinishedDaily);
					ongoingDailyInfo.put("total", ongoingRegularDaily + ongoingUnexpectedDaily + ongoingFinishedDaily);
					
					Map<String,Integer> ongoingMonthlyInfo = new HashMap<String,Integer>();
					ongoingMonthlyInfo.put("ongoingRegularMonthly", ongoingRegularMonthly);
					ongoingMonthlyInfo.put("ongoingUnexpectedMonthly", ongoingUnexpectedMonthly);
					ongoingMonthlyInfo.put("ongoingFinishedMonthly", ongoingFinishedMonthly);
					ongoingMonthlyInfo.put("total", ongoingRegularMonthly + ongoingUnexpectedMonthly + ongoingFinishedMonthly);
					
					ongoingInfo.put("ongoingCollectInfo", ongoingCollectInfo);
					ongoingInfo.put("ongoingDailyInfo", ongoingDailyInfo);
					ongoingInfo.put("ongoingMonthlyInfo", ongoingMonthlyInfo);
					
					result.put("ongoingInfo", ongoingInfo);
					
					Map<String,Integer> unClosedInfo = new HashMap<String,Integer>();
					unClosedInfo.put("unClosedRegular", unClosedRegular);
					unClosedInfo.put("unClosedAdvanced", unClosedAdvanced);
					unClosedInfo.put("unClosedOverdue", unClosedOverdue);
					result.put("unClosedInfo", unClosedInfo);
					
					Map<String,Integer> closedInfo = new HashMap<String,Integer>();
					closedInfo.put("closedRegular", closedRegular);
					closedInfo.put("closedOverdue", closedOverdue);
					closedInfo.put("closedAdvanced", closedAdvanced);
					result.put("closedInfo", closedInfo);
					return result;
				}
			};

			return run.query(conn, selectSql, rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询grid失败:" + e.getMessage(), e);
		}
	}

	/**
	 * @param conn
	 * @param selectSql
	 * @return
	 * @throws Exception 
	 */
	public static Map<String, Object> querySubtaskOverView(Connection conn, String selectSql) throws Exception {
		// TODO Auto-generated method stub
		try {
			QueryRunner run = new QueryRunner();

			ResultSetHandler<Map<String,Object>> rsHandler = new ResultSetHandler<Map<String,Object>>() {
				public Map<String,Object> handle(ResultSet rs) throws SQLException {
					Map<String,Object> result = new HashMap<String,Object>();
					int totalCollect = 0;
					int totalDaily = 0;
					int totalMonthly = 0;
					
					int totalCollect_Type0 = 0;
					int totalCollect_Type1 = 0;
					int totalCollect_Type2 = 0;
					
					int totalDaily_Type0 = 0;
					int totalDaily_Type3 = 0;
					int totalDaily_Type4 = 0;
					int totalDaily_Type5 = 0;
					
					int totalMonthly_Type6 = 0;
					int totalMonthly_Type7 = 0;
					int totalMonthly_Type8 = 0;
					int totalMonthly_Type9 = 0;
					int totalMonthly_Type10 = 0;
					
					int ongoingCollect = 0;
					int ongoingDaily = 0;
					int ongoingMonthly = 0;
					
					int draftCollect = 0;
					int draftDaily = 0;
					int draftMonthly = 0;
					
					int finishedCollect = 0;
					int finishedDaily = 0;
					int finishedMonthly = 0;
					
					int closedCollect = 0;
					int closedDaily = 0;
					int closedMonthly = 0;
					
					int ongoingRegularCollect = 0;
					int ongoingUnexpectedCollect = 0;
					int ongoingRegularDaily = 0;
					int ongoingUnexpectedDaily = 0;
					int ongoingRegularMonthly = 0;
					int ongoingUnexpectedMonthly = 0;
					
					int closedRegularCollect = 0;
					int closedAdvancedCollect = 0;
					int closedOverdueCollect = 0;
					int closedRegularDaily = 0;
					int closedAdvancedDaily = 0;
					int closedOverdueDaily = 0;
					int closedRegularMonthly = 0;
					int closedAdvancedMonthly = 0;
					int closedOverdueMonthly = 0;
					
					while (rs.next()) {
						if(0 == rs.getInt("STAGE")){//采集
							totalCollect += 1;
							if(0==rs.getInt("TYPE")){
								totalCollect_Type0 += 1;
							}else if(1 == rs.getInt("TYPE")){
								totalCollect_Type1 += 1;
							}else if(2 == rs.getInt("TYPE")){
								totalCollect_Type2 += 1;
							}
							
							if(2 == rs.getInt("STATUS")){//草稿
								draftCollect += 1;
							}else if(0 == rs.getInt("STATUS")){//已关闭
								closedCollect += 1;
								if(0 == rs.getInt("DIFF_DATE")){
									closedRegularCollect += 1;
								}else if(0 > rs.getInt("DIFF_DATE")){
									closedOverdueCollect += 1;
								}else if(0 < rs.getInt("DIFF_DATE")){
									closedAdvancedCollect += 1;
								}
							}else if(1 == rs.getInt("STATUS")){//作业中+已完成
								if(100 == rs.getInt("PERCENT")){
									finishedCollect += 1;
								}else{
									ongoingCollect += 1;
									if(2 == rs.getInt("PROGRESS")){
										ongoingUnexpectedCollect += 1;
									}else{
										ongoingRegularCollect += 1;
									}
								}
							}
							
						}else if(1 == rs.getInt("STAGE")){//日编
							totalDaily += 1;
							if(0==rs.getInt("TYPE")){
								totalDaily_Type0 += 1;
							}else if(3 == rs.getInt("TYPE")){
								totalDaily_Type3 += 1;
							}else if(4 == rs.getInt("TYPE")){
								totalDaily_Type4 += 1;
							}else if(5 == rs.getInt("TYPE")){
								totalDaily_Type5 += 1;
							}
							
							if(2 == rs.getInt("STATUS")){//草稿
								draftDaily += 1;
							}else if(0 == rs.getInt("STATUS")){//已关闭
								closedDaily += 1;
								if(0 == rs.getInt("DIFF_DATE")){
									closedRegularDaily += 1;
								}else if(0 > rs.getInt("DIFF_DATE")){
									closedOverdueDaily += 1;
								}else if(0 < rs.getInt("DIFF_DATE")){
									closedAdvancedDaily += 1;
								}
							}else if(1 == rs.getInt("STATUS")){//作业中+已完成
								if(100 == rs.getInt("PERCENT")){
									finishedDaily += 1;
								}else{
									ongoingDaily += 1;
									if(2 == rs.getInt("PROGRESS")){
										ongoingUnexpectedDaily += 1;
									}else{
										ongoingRegularDaily += 1;
									}
								}
							}
							
						}else if(2 == rs.getInt("STAGE")){//月编
							totalMonthly += 1;
							if(6 == rs.getInt("TYPE")){
								totalMonthly_Type6 += 1;
							}else if(7 == rs.getInt("TYPE")){
								totalMonthly_Type7 += 1;
							}else if(8 == rs.getInt("TYPE")){
								totalMonthly_Type7 += 1;
							}else if(9 == rs.getInt("TYPE")){
								totalMonthly_Type7 += 1;
							}else if(10 == rs.getInt("TYPE")){
								totalMonthly_Type7 += 1;
							}
							
							if(2 == rs.getInt("STATUS")){//草稿
								draftMonthly += 1;
							}else if(0 == rs.getInt("STATUS")){//已关闭
								closedMonthly += 1;
								if(0 == rs.getInt("DIFF_DATE")){
									closedRegularMonthly += 1;
								}else if(0 > rs.getInt("DIFF_DATE")){
									closedOverdueMonthly += 1;
								}else if(0 < rs.getInt("DIFF_DATE")){
									closedAdvancedMonthly += 1;
								}
							}else if(1 == rs.getInt("STATUS")){//作业中+已完成
								if(100 == rs.getInt("PERCENT")){
									finishedMonthly += 1;
								}else{
									ongoingMonthly += 1;
									if(2 == rs.getInt("PROGRESS")){
										ongoingUnexpectedMonthly += 1;
									}else{
										ongoingRegularMonthly += 1;
									}
								}
							}
						}
						
					}	
					//采集
					Map<String,Object> collectInfo = new HashMap<String,Object>();
					collectInfo.put("total", totalCollect);
					collectInfo.put("totalCollect_Type0", totalCollect_Type0);
					collectInfo.put("totalCollect_Type1", totalCollect_Type1);
					collectInfo.put("totalCollect_Type2", totalCollect_Type2);
					
					collectInfo.put("ongoing", ongoingCollect);
					collectInfo.put("draft", draftCollect);
					collectInfo.put("finished", finishedCollect);
					collectInfo.put("closed", closedCollect);
					
					Map<String,Integer> ongoingInfoCollect = new HashMap<String,Integer>();
					ongoingInfoCollect.put("ongoingRegular", ongoingRegularCollect);
					ongoingInfoCollect.put("ongoingUnexpected", ongoingUnexpectedCollect);
					collectInfo.put("ongoingInfo", ongoingInfoCollect);
					
					Map<String,Integer> closededInfoCollect = new HashMap<String,Integer>();
					closededInfoCollect.put("closedRegular", closedRegularCollect);
					closededInfoCollect.put("closedAdvanced", closedAdvancedCollect);
					closededInfoCollect.put("closedOverdue", closedOverdueCollect);
					collectInfo.put("closedInfo", closededInfoCollect);
					//日编
					Map<String,Object> dailyInfo = new HashMap<String,Object>();
					dailyInfo.put("total", totalDaily);
					dailyInfo.put("totalDaily_Type0", totalDaily_Type0);
					dailyInfo.put("totalDaily_Type3", totalDaily_Type3);
					dailyInfo.put("totalDaily_Type4", totalDaily_Type4);
					dailyInfo.put("totalDaily_Type5", totalDaily_Type5);
					
					dailyInfo.put("ongoing", ongoingDaily);
					dailyInfo.put("draft", draftDaily);
					dailyInfo.put("finished", finishedDaily);
					dailyInfo.put("closed", closedDaily);
					
					Map<String,Integer> ongoingInfoDaily = new HashMap<String,Integer>();
					ongoingInfoDaily.put("ongoingRegular", ongoingRegularDaily);
					ongoingInfoDaily.put("ongoingUnexpected", ongoingUnexpectedDaily);
					dailyInfo.put("ongoingInfo", ongoingInfoDaily);
					
					Map<String,Integer> closededInfoDaily = new HashMap<String,Integer>();
					closededInfoDaily.put("closedRegular", closedRegularDaily);
					closededInfoDaily.put("closedAdvanced", closedAdvancedDaily);
					closededInfoDaily.put("closedOverdue", closedOverdueDaily);
					dailyInfo.put("closedInfo", closededInfoDaily);
					
					//月编
					Map<String,Object> monthlyInfo = new HashMap<String,Object>();
					monthlyInfo.put("total", totalMonthly);
					monthlyInfo.put("totalMonthly_Type6", totalMonthly_Type6);
					monthlyInfo.put("totalMonthly_Type7", totalMonthly_Type7);
					monthlyInfo.put("totalMonthly_Type8", totalMonthly_Type8);
					monthlyInfo.put("totalMonthly_Type9", totalMonthly_Type9);
					monthlyInfo.put("totalMonthly_Type10", totalMonthly_Type10);
					
					monthlyInfo.put("ongoing", ongoingMonthly);
					monthlyInfo.put("draft", draftMonthly);
					monthlyInfo.put("finished", finishedMonthly);
					monthlyInfo.put("closed", closedMonthly);
					
					Map<String,Integer> ongoingInfoMonthly = new HashMap<String,Integer>();
					ongoingInfoMonthly.put("ongoingRegular", ongoingRegularMonthly);
					ongoingInfoMonthly.put("ongoingUnexpected", ongoingUnexpectedMonthly);
					monthlyInfo.put("ongoingInfo", ongoingInfoMonthly);
					
					Map<String,Integer> closededInfoMonthly = new HashMap<String,Integer>();
					closededInfoMonthly.put("closedRegular", closedRegularMonthly);
					closededInfoMonthly.put("closedAdvanced", closedAdvancedMonthly);
					closededInfoMonthly.put("closedOverdue", closedOverdueMonthly);
					monthlyInfo.put("closedInfo", closededInfoMonthly);
					
					result.put("collectInfo", collectInfo);
					result.put("dailyInfo", dailyInfo);
					result.put("monthlyInfo", monthlyInfo);

					return result;
				}
	
			};

			return run.query(conn, selectSql,rsHandler);
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询grid失败:" + e.getMessage(), e);
		}
	}

	/**
	 * @param conn
	 * @param selectSql
	 * @return
	 * @throws Exception 
	 */
	public static Map<String, Object> queryBlockOverViewByGroup(Connection conn, String selectSql) throws Exception {
		// TODO Auto-generated method stub
		try {
			QueryRunner run = new QueryRunner();

			ResultSetHandler<Map<String,Object>> rsHandler = new ResultSetHandler<Map<String,Object>>() {
				public Map<String,Object> handle(ResultSet rs) throws SQLException {
					Map<String,Object> result = new HashMap<String,Object>();
					int total = 0;
					int unAssigned = 0;
					int ongoing = 0;
					int finished = 0;
					int ongoingRegular = 0;
					int ongoingUnexpected = 0;
					int finishedRegular = 0;
					int finishedAdvanced = 0;
					int finishedOverdue = 0;
					while (rs.next()) {
//						int blockManId = rs.getInt("BLOCK_MAN_ID");
						String subtaskIdList = rs.getString("SUBTASK_ID_lIST");
						String subtaskStatusList = rs.getString("SUBTASK_STATUS_LIST");
						
						if(null==subtaskIdList){
							//未分配
							unAssigned += 1;
							continue;
						}else{
							//0关闭，1开启,2草稿
							if((-1 == subtaskStatusList.indexOf("1"))&&(-1 == subtaskStatusList.indexOf("2"))){
								//全部都是关闭，block完成
								if(0 == rs.getInt("DIFF_DATE")){
									//按时完成
									finishedRegular += 1;
								}else if(0 < rs.getInt("DIFF_DATE")){
									//提前完成
									finishedAdvanced += 1;
								}else if(0 > rs.getInt("DIFF_DATE")){
									//逾期完成
									finishedOverdue += 1;
								}	
							}else if((-1 == subtaskStatusList.indexOf("1"))&&(-1 == subtaskStatusList.indexOf("0"))){
								//全部都是草稿,block未分配
								unAssigned += 1;
							}
							else{
								//未完成
								//progress:1 //进度 1正常(实际作业小于预期)，2异常(实际作业等于或大于预期)
								if(2 == rs.getInt("PROGRESS")){
									ongoingUnexpected += 1;
								}else{
									ongoingRegular += 1;
								}
							}
						}
					}
					ongoing = ongoingRegular + ongoingUnexpected;
					finished = finishedRegular + finishedAdvanced + finishedOverdue;
					total = unAssigned + ongoing + finished;
					
					result.put("unAssigned", unAssigned);
					result.put("ongoing", ongoing);
					result.put("finished", finished);
					result.put("total", total);
					
					Map<String,Integer> ongoingInfo = new HashMap<String,Integer>();
					ongoingInfo.put("ongoingRegular", ongoingRegular);
					ongoingInfo.put("ongoingUnexpected", ongoingUnexpected);
					result.put("ongoingInfo", ongoingInfo);
					
					Map<String,Integer> finishedInfo = new HashMap<String,Integer>();
					finishedInfo.put("finishedRegular", finishedRegular);
					finishedInfo.put("finishedAdvanced", finishedAdvanced);
					finishedInfo.put("finishedOverdue", finishedOverdue);
					result.put("finishedInfo", finishedInfo);

					return result;
				}
	
			};

			return run.query(conn, selectSql,rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询grid失败:" + e.getMessage(), e);
		}
	}

	
	/**
	 * @Title: queryGroupOverView
	 * @Description: 查询统计数据,处理查询结果
	 * @param conn
	 * @param selectSql
	 * @return
	 * @throws Exception  Map<String,Object>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年10月19日 上午11:04:45 
	 */
	public static Map<String, Object> queryGroupOverView(Connection conn, String selectSql) throws Exception {
		try {
			QueryRunner run = new QueryRunner();

			ResultSetHandler<Map<String,Object>> rsHandler = new ResultSetHandler<Map<String,Object>>() {
				public Map<String,Object> handle(ResultSet rs) throws SQLException {
					Map<String,Object> result = new HashMap<String,Object>();
					int percent = 0;
					int planDate = 0;
					int diffDate = 0;
					String planStartDate = null;
					String planEndDate = null;
					String actualStartDate = null;
					String actualEndDate = null;
					int poiPlanTotal = 0;
					int roadPlanTotal = 0;
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					while (rs.next()) {
						percent = rs.getInt("percent");
						planDate = rs.getInt("planDate");
						diffDate = rs.getInt("diffDate");
						planStartDate = df.format(rs.getTimestamp("planStartDate"));
						planEndDate = df.format(rs.getTimestamp("planEndDate"));
						actualStartDate = df.format(rs.getTimestamp("actualStartDate"));
						actualEndDate = df.format(rs.getTimestamp("actualEndDate"));
						poiPlanTotal = rs.getInt("poiPlanTotal");
						roadPlanTotal = rs.getInt("roadPlanTotal");
						
					}
				
					result.put("percent", percent);
					result.put("planDate", planDate);
					result.put("diffDate", diffDate);
					result.put("planStartDate", planStartDate);
					result.put("planEndDate", planEndDate);
					result.put("actualStartDate", actualStartDate);
					result.put("actualEndDate", actualEndDate);
					result.put("poiPlanTotal", poiPlanTotal);
					result.put("roadPlanTotal", roadPlanTotal);
					return result;
				}
	
			};

			return run.query(conn, selectSql,rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询group失败:" + e.getMessage(), e);
		}
	}
	
	/**
	 * @param conn
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	public static Map<String, Object> queryBlockOverViewByTask(Connection conn, int taskId) throws Exception {
		// TODO Auto-generated method stub
		try {
			QueryRunner run = new QueryRunner();
			
			String selectSql = "SELECT DISTINCT BM.BLOCK_MAN_ID, BM.STATUS"
					+ ", FSOB.DIFF_DATE, FSOB.PROGRESS, FSOB.PERCENT"
					+ ", FSOB.COLLECT_PROGRESS, FSOB.COLLECT_PERCENT, FSOB.COLLECT_DIFF_DATE"
					+ ", FSOB.DAILY_PROGRESS, FSOB.DAILY_PERCENT, FSOB.DAILY_DIFF_DATE"
					+ ", listagg(S.SUBTASK_ID, ',') within GROUP(order by BM.BLOCK_MAN_ID) over(partition by BM.BLOCK_MAN_ID) SUBTASK_ID_LIST"
					+ ", listagg(S.STATUS, ',') within GROUP(order by BM.BLOCK_MAN_ID) over(partition by BM.BLOCK_MAN_ID) SUBTASK_STATUS_LIST"
					+ " FROM BLOCK_MAN BM, SUBTASK S, FM_STAT_OVERVIEW_BLOCKMAN FSOB"
					+ " WHERE BM.LATEST = 1"
					+ " AND S.BLOCK_MAN_ID(+) = BM.BLOCK_MAN_ID"
					+ " AND FSOB.BLOCK_MAN_ID(+) = BM.BLOCK_MAN_ID"
					+ " AND BM.TASK_ID = " + taskId;

			ResultSetHandler<Map<String,Object>> rsHandler = new ResultSetHandler<Map<String,Object>>() {
				public Map<String,Object> handle(ResultSet rs) throws SQLException {
					Map<String,Object> result = new HashMap<String,Object>();
					
					int draft = 0;
					int closed = 0;
					int ongoing = 0;
					int finished = 0;
					
					int finishedRegular = 0;
					int finishedAdvanced = 0;
					int finishedOverdue = 0;
					
					int finishedOverdueCollect = 0;
					int finishedOverdueDaily = 0;

					while (rs.next()) {
						String subtaskIdList = rs.getString("SUBTASK_ID_LIST");
						String subtaskStatusList = rs.getString("SUBTASK_STATUS_LIST");
						
						if(2 == rs.getInt("STATUS")){
							draft += 1;
						}else if(0 == rs.getInt("STATUS")){
							closed += 1;
							if(0 == rs.getInt("DIFF_DATE")){
								finishedRegular += 1;
							}else if(0 > rs.getInt("DIFF_DATE")){
								finishedOverdue += 1;
							}else if(0 < rs.getInt("DIFF_DATE")){
								finishedAdvanced += 1;
							}
							
							if(0 > rs.getInt("COLLECT_DIFF_DATE")){
								finishedOverdueCollect += 1;
							}
							if(0 > rs.getInt("DAILY_DIFF_DATE")){
								finishedOverdueDaily += 1;
							}
						}else{
							if((null==subtaskIdList)||(0 < subtaskStatusList.indexOf("1"))||(0 < subtaskStatusList.indexOf("2"))){
								ongoing += 1;
							}else{
								finished += 1;
							}
						}
					}
					
					result.put("draft", draft);
					result.put("ongoing", ongoing);
					result.put("finished", finished);
					result.put("closed", closed);
					result.put("total", draft + ongoing + finished + closed);

					result.put("finishedRegular", finishedRegular);
					result.put("finishedAdvanced", finishedAdvanced);
					result.put("finishedOverdue", finishedOverdue);
					
					result.put("finishedOverdueCollect", finishedOverdueCollect);
					result.put("finishedOverdueDaily", finishedOverdueDaily);

					return result;
				}
	
			};

			return run.query(conn, selectSql,rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询grid失败:" + e.getMessage(), e);
		}
	}

	/**
	 * @param conn
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	public static Map<String, Object> queryBlockOverViewByTaskCollect(Connection conn, int taskId) throws Exception {
		// TODO Auto-generated method stub
		try {
			QueryRunner run = new QueryRunner();
			
			String selectSql = "SELECT DISTINCT BM.BLOCK_MAN_ID, BM.STATUS"
					+ ", FSOB.DIFF_DATE, FSOB.PROGRESS, FSOB.PERCENT"
					+ ", FSOB.COLLECT_PROGRESS, FSOB.COLLECT_PERCENT, FSOB.COLLECT_DIFF_DATE"
					+ ", FSOB.DAILY_PROGRESS, FSOB.DAILY_PERCENT, FSOB.DAILY_DIFF_DATE"
					+ ", listagg(S.SUBTASK_ID, ',') within GROUP(order by BM.BLOCK_MAN_ID) over(partition by BM.BLOCK_MAN_ID) SUBTASK_ID_LIST"
					+ ", listagg(S.STATUS, ',') within GROUP(order by BM.BLOCK_MAN_ID) over(partition by BM.BLOCK_MAN_ID) SUBTASK_STATUS_LIST"
					+ " FROM BLOCK_MAN BM, SUBTASK S, FM_STAT_OVERVIEW_BLOCKMAN FSOB"
					+ " WHERE BM.LATEST = 1"
					+ " AND S.STAGE = 0"
					+ " AND BM.STATUS = 1"
					+ " AND S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID"
					+ " AND FSOB.BLOCK_MAN_ID(+) = BM.BLOCK_MAN_ID"
					+ " AND BM.TASK_ID = " + taskId;

			ResultSetHandler<Map<String,Object>> rsHandler = new ResultSetHandler<Map<String,Object>>() {
				public Map<String,Object> handle(ResultSet rs) throws SQLException {
					Map<String,Object> result = new HashMap<String,Object>();

					int ongoing = 0;
					int finished = 0;
					
					int ongoingUnexpected = 0;
					int ongoingRegular = 0;

					while (rs.next()) {
						String subtaskIdList = rs.getString("SUBTASK_ID_LIST");
						String subtaskStatusList = rs.getString("SUBTASK_STATUS_LIST");
						
						if(null!=subtaskIdList){
							if(0 < subtaskStatusList.indexOf("1")){
								ongoing += 1;
								if(2 == rs.getInt("COLLECT_PROGRESS")){
									ongoingUnexpected += 1;
								}else{
									ongoingRegular += 1;
								}
							}else if((-1==subtaskStatusList.indexOf("1")) && (-1==subtaskStatusList.indexOf("1"))){
								finished += 1;
							}
								
						}
					}
					
					result.put("ongoing", ongoing);
					result.put("finished", finished);
					result.put("ongoingUnexpected", ongoingUnexpected);
					result.put("ongoingRegular", ongoingRegular);
					
					return result;
				}
	
			};

			return run.query(conn, selectSql,rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询grid失败:" + e.getMessage(), e);
		}
	}

	/**
	 * @param conn
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	public static Map<String, Object> queryBlockOverViewByTaskDaily(Connection conn, int taskId) throws Exception {
		// TODO Auto-generated method stub
		try {
			QueryRunner run = new QueryRunner();
			
			String selectSql = "SELECT DISTINCT BM.BLOCK_MAN_ID, BM.STATUS"
					+ ", FSOB.DIFF_DATE, FSOB.PROGRESS, FSOB.PERCENT"
					+ ", FSOB.COLLECT_PROGRESS, FSOB.COLLECT_PERCENT, FSOB.COLLECT_DIFF_DATE"
					+ ", FSOB.DAILY_PROGRESS, FSOB.DAILY_PERCENT, FSOB.DAILY_DIFF_DATE"
					+ ", listagg(S.SUBTASK_ID, ',') within GROUP(order by BM.BLOCK_MAN_ID) over(partition by BM.BLOCK_MAN_ID) SUBTASK_ID_LIST"
					+ ", listagg(S.STATUS, ',') within GROUP(order by BM.BLOCK_MAN_ID) over(partition by BM.BLOCK_MAN_ID) SUBTASK_STATUS_LIST"
					+ " FROM BLOCK_MAN BM, SUBTASK S, FM_STAT_OVERVIEW_BLOCKMAN FSOB"
					+ " WHERE BM.LATEST = 1"
					+ " AND S.STAGE = 1"
					+ " AND BM.STATUS = 1"
					+ " AND S.BLOCK_MAN_ID = BM.BLOCK_MAN_ID"
					+ " AND FSOB.BLOCK_MAN_ID(+) = BM.BLOCK_MAN_ID"
					+ " AND BM.TASK_ID = " + taskId;

			ResultSetHandler<Map<String,Object>> rsHandler = new ResultSetHandler<Map<String,Object>>() {
				public Map<String,Object> handle(ResultSet rs) throws SQLException {
					Map<String,Object> result = new HashMap<String,Object>();

					int ongoing = 0;
					int finished = 0;
					
					int ongoingUnexpected = 0;
					int ongoingRegular = 0;

					while (rs.next()) {
						String subtaskIdList = rs.getString("SUBTASK_ID_LIST");
						String subtaskStatusList = rs.getString("SUBTASK_STATUS_LIST");
						
						if(null!=subtaskIdList){
							if(0 < subtaskStatusList.indexOf("1")){
								ongoing += 1;
								if(2 == rs.getInt("DAILY_PROGRESS")){
									ongoingUnexpected += 1;
								}else{
									ongoingRegular += 1;
								}
							}else if((-1==subtaskStatusList.indexOf("1")) && (-1==subtaskStatusList.indexOf("1"))){
								finished += 1;
							}
								
						}
					}
					
					result.put("ongoing", ongoing);
					result.put("finished", finished);
					result.put("ongoingUnexpected", ongoingUnexpected);
					result.put("ongoingRegular", ongoingRegular);
					
					return result;
				}
	
			};

			return run.query(conn, selectSql,rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询grid失败:" + e.getMessage(), e);
		}
	}

	/**
	 * @param conn
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	public static int queryBlockOverViewByTaskUnplanned(Connection conn, int taskId) throws Exception {
		// TODO Auto-generated method stub
		try {
			QueryRunner run = new QueryRunner();
			
			String selectSql = "SELECT COUNT(1) AS NUM FROM TASK T ,BLOCK B"
					+ " WHERE T.CITY_ID = B.CITY_ID"
					+ " AND B.PLAN_STATUS = 0"
					+ " AND T.TASK_ID = " + taskId;;

			ResultSetHandler<Integer> rsHandler = new ResultSetHandler<Integer>() {
				public Integer handle(ResultSet rs) throws SQLException {
					int num = 0;
					if (rs.next()) {
						num = rs.getInt("NUM");
					}
					return num;
				}
	
			};

			return run.query(conn, selectSql,rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询grid失败:" + e.getMessage(), e);
		}
	}

	/**
	 * 若通过web界面进行了无任务转中，则在统计表中进行标识，表示此次执行过无任务转中操作了。
	 * @param conn
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	public static void changeTaskConvertFlagToOK(Connection conn, int taskId) throws Exception {
		// TODO Auto-generated method stub
		try {
			QueryRunner run = new QueryRunner();			
			String selectSql = "UPDATE FM_STAT_OVERVIEW_TASK SET CONVERT_FLAG = 1 WHERE TASK_ID = " + taskId;
			run.update(conn, selectSql);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询grid失败:" + e.getMessage(), e);
		}
	}
	/**
	 * timestamp:yyyymmdd
	 * 获取按照人天任务进行统计的管理列表
	 * @return Map<String, Object>:	map.put("subtaskIds", subtaskSet);
									map.put("userId", userId);
									map.put("taskId", taskId);
									map.put("taskName", rs.getString("TASK_NAME"));
									map.put("cityName", rs.getString("CITY_NAME"));
									map.put("leaderName", rs.getString("LEADER_NAME"));
									map.put("userName", rs.getString("USER_NAME"));	
	 * @throws Exception
	 */
	public static List<Map<String, Object>> staticsPersionJob(String timestamp) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();

			String selectSql = "SELECT S.EXE_USER_ID,"
					+ "       T.TASK_ID,"
					+ "       S.SUBTASK_ID,"
					+ "       T.NAME             TASK_NAME,"
					+ "       C.CITY_NAME,"
					+ "       L.USER_REAL_NAME   LEADER_NAME,"
					+ "       E.USER_REAL_NAME   USER_NAME"
					+ "  FROM TASK                     T,"
					+ "       SUBTASK                  S,"
					+ "       FM_STAT_OVERVIEW_SUBTASK FS,"
					+ "       PROGRAM                  P,"
					+ "       CITY                     C,"
					+ "       USER_GROUP               G,"
					+ "       USER_INFO                L,"
					+ "       USER_INFO                E"
					+ " WHERE T.TASK_ID = S.TASK_ID"
					+ "   AND S.STATUS IN (0, 1)"
					+ "   AND T.PROGRAM_ID = P.PROGRAM_ID"
					+ "   AND P.TYPE = 1"
					+ "   AND P.CITY_ID = C.CITY_ID"
					+ "   AND T.GROUP_ID = G.GROUP_ID"
					+ "   AND G.LEADER_ID = L.USER_ID"
					+ "   AND S.EXE_USER_ID = E.USER_ID"
					+ "   AND T.TYPE = 0"
					+ "   AND S.WORK_KIND = 1"
					+ "   AND S.SUBTASK_ID = FS.SUBTASK_ID"
					+ "   AND (FS.ACTUAL_END_DATE >= TO_DATE('"+timestamp+"', 'yyyymmdd') OR"
					+ "       FS.ACTUAL_END_DATE IS NULL)";
			
			return run.query(conn, selectSql, new ResultSetHandler<List<Map<String,Object>>>() {

				@Override
				public List<Map<String,Object>> handle(ResultSet rs) throws SQLException {
					List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
					
					while(rs.next()) labal:{	
						Set<Long> subtaskSet = new HashSet<>();
						Map<String,Object> map = new HashMap<String,Object>();
					    long userId = rs.getLong("EXE_USER_ID");
					    long taskId = rs.getLong("TASK_ID");
						subtaskSet.add(rs.getLong("subtask_id"));
						map.put("userId", userId);
						map.put("taskId", taskId);
						for(int i = 0; i < list.size(); i++){
							Map<String, Object> taskMap = list.get(i);
							if(Long.valueOf(taskMap.get("taskId").toString()) == taskId && Long.valueOf(taskMap.get("userId").toString()) == userId){
								subtaskSet = (Set<Long>) taskMap.get("subtaskIds");
								subtaskSet.add(rs.getLong("subtask_id"));
								taskMap.put("subtaskIds", subtaskSet);
//								list.remove(i);
//								list.add(taskMap);
								break labal;
							}
						}
						map.put("subtaskIds", subtaskSet);
						map.put("taskName", rs.getString("TASK_NAME"));
						map.put("cityName", rs.getString("CITY_NAME"));
						map.put("leaderName", rs.getString("LEADER_NAME"));
						map.put("userName", rs.getString("USER_NAME"));	
						list.add(map);
					}
					return list;
				}
			});
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
}
