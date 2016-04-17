package com.navinfo.dataservice.expcore.source;

import com.navinfo.dataservice.expcore.exception.ExportException;


/**
 * User: Xiao Xiaowen
 * 数据导出的源(库)接口
 */
public interface ExportSource {

	public void init(String gdbVersion)throws ExportException;
	public String getTempSuffix();
	public void release();

}
