package com.navinfo.dataservice.scripts;

import java.sql.Timestamp;
import java.util.Date;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.job.model.RunJobInfo;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;

import net.sf.json.JSONObject;

/**
 * 按照时间区域执行人天脚本。参数有2个 起始时间，终止时间。
 * 例如：personJobExe.sh 20170906 20170908
 * 则会执行3天的记录：20170906 20170907 20170908
 * @author zhangxiaoyi
 *
 */
public class PersonJobExe {
	public static void main(String[] args) throws Exception{
		System.out.println("start"); 
		JobScriptsInterface.initContext();
		//一次创建执行底层脚本即可，底层脚本执行成功后，会自动调用后续脚本
		//personDay,personTips---等personFcc执行结束，就会自动调用person了
		String startWorkDay=String.valueOf(args[0]);
		String endWorkDay=String.valueOf(args[1]);
		Timestamp start = DateUtils.stringToTimestamp(startWorkDay, DateUtils.DATE_YMD);
		Timestamp end = DateUtils.stringToTimestamp(endWorkDay, DateUtils.DATE_YMD);
		Date startDate = DateUtils.stringToDate(startWorkDay, DateUtils.DATE_YMD);
		long days=DateUtils.diffDay(start, end);
		System.out.println("date:"+startWorkDay+"--"+endWorkDay+";diffDay:"+days); 
		for(int i=0;i<=days;i++){
			String workDay=DateUtils.dateToString(DateUtils.addDay(startDate, i),  DateUtils.DATE_YMD);
			startJob("personDayJob",workDay);
			startJob("personTipsJob",workDay);
		}
		System.out.println("end"); 
		System.exit(0);
	}
	private static void startJob(String jobType,String workDay) throws Exception{
		String timestamp=DateUtils.dateToString(DateUtils.getSysdate(), "yyyyMMddHH0000");
		JSONObject request=new JSONObject();
		request.put("timestamp", timestamp);
		request.put("workDay", workDay);
		RunJobInfo info = new RunJobInfo(jobType,request);
		
		JobApi jobApi = (JobApi)ApplicationContextUtil.getBean("jobApi");
		jobApi.createStaticsJob(info.getJobType(), info.getRequest(), info.getUserId(), info.getTaskId(), info.getDescp());
	}
}
