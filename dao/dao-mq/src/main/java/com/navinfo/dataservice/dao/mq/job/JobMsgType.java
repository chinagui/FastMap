package com.navinfo.dataservice.dao.mq.job;

/** 
* @ClassName: JobMsgType 
* @author Xiao Xiaowen 
* @date 2016年3月24日 下午2:06:40 
* @Description: TODO
*/
public enum JobMsgType {
	MSG_CREATE_JOB("create_job"),MSG_RUN_JOB("run_job"),MSG_RESPONSE_JOB("resp_job"),MSG_END_JOB("end_job");
	private String jobMsgType;
	JobMsgType(String jobMsgType){
		this.jobMsgType=jobMsgType;
	}
	String getJobMsgType(){
		return jobMsgType;
	}

	@Override
	public String toString() {
		return jobMsgType;
	}

	public static void main(String[] args){
		System.out.println("..."+JobMsgType.MSG_CREATE_JOB);
	}
}
