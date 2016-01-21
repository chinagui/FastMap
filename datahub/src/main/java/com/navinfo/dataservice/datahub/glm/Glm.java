package com.navinfo.dataservice.datahub.glm;

import java.util.Map;

/** 
 * @ClassName: Glm 
 * @author Xiao Xiaowen 
 * @date 2016-1-12 下午2:15:06 
 * @Description: 只支持GDB+模型，没有主键的表使用ROW_ID作为主键
 */
public class Glm {
	private String gdbVersion;//240+,...
	private Map<String,GlmTable> tables;
	public Glm(String gdbVersion){
		this.gdbVersion=gdbVersion;
	}
	public String getGdbVersion() {
		return gdbVersion;
	}
	public void setGdbVersion(String gdbVersion) {
		this.gdbVersion = gdbVersion;
	}
	public Map<String,GlmTable> getTables() {
		return tables;
	}
	public void setTables(Map<String,GlmTable> tables) {
		this.tables = tables;
	}
}
