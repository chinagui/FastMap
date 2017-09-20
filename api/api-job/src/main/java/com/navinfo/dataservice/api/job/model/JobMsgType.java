package com.navinfo.dataservice.api.job.model;

/** 
* @ClassName: JobMsgType 
* @author Xiao Xiaowen 
* @date 2016年3月24日 下午2:06:40 
* @Description: TODO
*/
public enum JobMsgType {
	MSG_CREATE_JOB("create_job")
	,MSG_RUN_JOB("run_job")
	,MSG_RUN_STATICS_JOB("run_statics_job")
	,MSG_RESPONSE_JOB("resp_job")
	,MSG_END_JOB("end_job");
	private String name;
	JobMsgType(String name){
		this.name=name;
	}
	String getName(){
		return name;
	}
	@Override
	public String toString() {
		return name;
	}

	public static void main(String[] args){
		System.out.println("..."+JobMsgType.MSG_CREATE_JOB);
	}
}
