package com.navinfo.dataservice.engine.statics.writer;

/**
 * 统计结果写入类，一般默认使用DefaultWriter
 * @author zhangxiaoyi
 *
 */
public class WriterFactory {
	//子任务统计job
	private static final String subtask_job = "subtaskStat";
	
	public static DefaultWriter createWriter(String jobType){
		if(subtask_job.equals(jobType)){
			return new SubtaskWriter();
		}else{
			return new DefaultWriter();		
		}
	}
}
