package com.navinfo.navicommons.database.sql;

import java.util.ArrayList;
import java.util.List;

/** 
* @ClassName: RunnableSQL 
* @author Xiao Xiaowen 
* @date 2016年4月13日 下午6:24:28 
* @Description: TODO
*/
public class RunnableSQL {
    private String sql;
    private List<Object> args = new ArrayList<Object>();
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public List<Object> getArgs() {
		return args;
	}
	public void setArgs(List<Object> args) {
		this.args = args;
	}
	/**
	 * 非线程安全
	 * @param arg
	 */
	public void addArg(Object arg){
		args.add(arg);
	}
}
