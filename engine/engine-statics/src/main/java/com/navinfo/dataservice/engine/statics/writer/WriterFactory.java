package com.navinfo.dataservice.engine.statics.writer;

/**
 * 统计结果写入类，一般默认使用DefaultWriter
 * @author zhangxiaoyi
 *
 */
public class WriterFactory {
	//子任务统计job
	private static final String subtask_job = "subtaskStat";
	//day_planjob
	private static final String day_plan_job = "dayPlanStat";
	//任务统计job
	private static final String task_job = "taskStat";
	//项目统计job
	private static final String program_job = "programStat";
	
	public static DefaultWriter createWriter(String jobType){
		if(subtask_job.equals(jobType)){
			return new SubtaskWriter();
		}else if(day_plan_job.equals(jobType)){
			return new DayPlanWriter();
		}else if(task_job.equals(jobType)){
			return new TaskWriter();
		}else if(program_job.equals(jobType)){
			return new ProgramWriter();
		}else{
			return new DefaultWriter();		
		}
	}
}
