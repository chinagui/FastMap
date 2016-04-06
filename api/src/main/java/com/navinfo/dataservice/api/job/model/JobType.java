package com.navinfo.dataservice.api.job.model;

/** 
* @ClassName: JobType 
* @author Xiao Xiaowen 
* @date 2016年3月30日 上午10:50:34 
* @Description: TODO
*/
public enum JobType {
	GDB_EXP("gdb_exp"),
	GDB_IMP("gdb_imp");
	private String name;
	JobType(String name){
		this.name=name;
	}
	public String getName(){
		return name;
	}
	@Override
	public String toString(){
		return name;
	}
}
