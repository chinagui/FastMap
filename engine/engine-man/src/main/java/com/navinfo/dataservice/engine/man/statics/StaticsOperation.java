package com.navinfo.dataservice.engine.man.statics;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;

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
								if((blockManIdList==null) 
										||((blockManStatusList!=null)
												&& (-1==blockManStatusList.indexOf("1")) 
												&& (-1==blockManStatusList.indexOf("2"))
												)
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
					collectInfo.put("closededInfo", closededInfoCollect);
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
					dailyInfo.put("closededInfo", closededInfoDaily);
					
					//月编
					Map<String,Object> monthlyInfo = new HashMap<String,Object>();
					monthlyInfo.put("totalMonthly", totalMonthly);
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
					monthlyInfo.put("closededInfo", closededInfoMonthly);
					
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


	
}
